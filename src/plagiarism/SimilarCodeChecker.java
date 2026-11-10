package plagiarism;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fixer.UsernameResolver;

// Plagiarism check 3: students whose code is very similar.
//
// Each file is turned into tokens with the names, numbers and strings
// replaced (so renaming variables does not hide copying), then cut into
// k-grams: every run of K tokens in a row. Two copies of the same file
// are compared by how many k-grams they share (Jaccard similarity).
//
// Everyone starts from the same exam template, so the template's k-grams
// are taken out first. Only the code the students wrote is compared.
//
// The k-grams of a file are kept in a HashMap<String, Boolean>: the key is
// the k-gram and the value is always true. A HashMap cannot hold the same
// key twice, so each k-gram is only counted once.
//
// Reads the original zips, so it does not need check, fix or grade first.
public class SimilarCodeChecker {
    // How many tokens are in one k-gram
    private static final int K = 5;
    // Pairs at or above this similarity are reported
    private static final double THRESHOLD = 0.8;
    // A file with fewer of the student's own k-grams than this is skipped,
    // e.g. the template left unchanged, as any match would mean nothing
    private static final int MIN_KGRAMS = 10;

    private static final String JAVA_EXTENSION = ".java";
    private static final String TESTER_ENDING = "Tester.java";

    // Java keywords are kept as they are. Every other name becomes "ID".
    private static final String[] KEYWORDS = {
            "abstract", "boolean", "break", "byte", "case", "catch", "char",
            "class", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "if",
            "implements", "import", "instanceof", "int", "interface", "long",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "super", "switch", "this", "throw",
            "throws", "try", "void", "while", "true", "false" };

    // A string, a char, a name, a number, or any other single symbol
    private static final Pattern TOKEN = Pattern.compile(
            "\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'"
                    + "|[A-Za-z_$][\\w$]*|\\d+(?:\\.\\d+)?|\\S");

    // True if the word is one of the Java keywords above
    private static boolean isKeyword(String word) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(word)) {
                return true;
            }
        }

        return false;
    }

    // Takes out the comments, then turns the code into tokens,
    // e.g. "int total = 0;" -> [int, ID, =, N, ;]
    static ArrayList<String> tokenize(String source) {
        source = source.replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("//[^\\n]*", " ");

        ArrayList<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(source);

        while (matcher.find()) {
            String token = matcher.group();
            char first = token.charAt(0);

            if (first == '"' || first == '\'') {
                tokens.add("S");
            } else if (Character.isDigit(first)) {
                tokens.add("N");
            } else if (Character.isJavaIdentifierStart(first)) {
                tokens.add(isKeyword(token) ? token : "ID");
            } else {
                tokens.add(token);
            }
        }

        return tokens;
    }

    // Every run of K tokens in a row, e.g. "int ID = N ;"
    static HashMap<String, Boolean> kgrams(ArrayList<String> tokens) {
        HashMap<String, Boolean> kgrams = new HashMap<>();

        for (int i = 0; i + K <= tokens.size(); i++) {
            String kgram = String.join(" ", tokens.subList(i, i + K));
            kgrams.put(kgram, true);
        }

        return kgrams;
    }

    // Shared k-grams / all k-grams, from 0.0 to 1.0
    static double similarity(HashMap<String, Boolean> first,
            HashMap<String, Boolean> second) {
        if (first.isEmpty() && second.isEmpty()) {
            return 1.0;
        }

        // Count the k-grams that are in both files
        int shared = 0;
        for (String kgram : first.keySet()) {
            if (second.containsKey(kgram)) {
                shared++;
            }
        }

        // All the different k-grams in the two files. The shared ones are
        // in both sizes, so take them away once.
        int total = first.size() + second.size() - shared;

        return (double) shared / total;
    }

    // The k-grams of each .java file in the exam template,
    // by file name, e.g. "Q2b.java" -> {...}
    private static HashMap<String, HashMap<String, Boolean>> readTemplate(
            File templateFolder) {
        HashMap<String, HashMap<String, Boolean>> templateKgrams =
                new HashMap<>();
        File[] questionFolders = templateFolder.listFiles();

        if (questionFolders == null) {
            return templateKgrams;
        }

        for (File questionFolder : questionFolders) {
            File[] files = questionFolder.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (!file.getName().endsWith(JAVA_EXTENSION)) {
                    continue;
                }

                try {
                    String source = new String(Files.readAllBytes(
                            file.toPath()), StandardCharsets.ISO_8859_1);
                    templateKgrams.put(file.getName(),
                            kgrams(tokenize(source)));
                } catch (IOException exception) {
                    // No template for this file, so nothing is taken out
                }
            }
        }

        return templateKgrams;
    }

    // e.g. "RenameToYourStudentID/Q2/Q2b.java" -> "Q2b.java"
    private static String getFileName(String zipEntryName) {
        return zipEntryName.substring(zipEntryName.lastIndexOf('/') + 1);
    }

    // Returns the pairs of students with very similar code in one file,
    // most similar first. templateFolder is the blank exam
    // (RenameToYourUsername).
    public static ArrayList<SimilarCode> findSimilarCode(File[] zipFiles,
            File templateFolder) {
        HashMap<String, HashMap<String, Boolean>> templateKgrams =
                readTemplate(templateFolder);

        // 1. Each student's own k-grams, by file name, then username:
        // "Q2b.java" -> { "fen.lai.2022" -> {...}, ... }
        // TreeMaps keep the files and students in the same order each run
        TreeMap<String, TreeMap<String, HashMap<String, Boolean>>> kgramsByFile =
                new TreeMap<>();

        for (File zip : zipFiles) {
            String username = UsernameResolver.getUsername(zip.getPath());

            if (username == null) {
                continue;
            }

            ArrayList<String[]> javaFiles;
            try {
                javaFiles = HeaderChecker.readJavaFiles(zip);
            } catch (IOException exception) {
                continue;
            }

            for (String[] javaFile : javaFiles) {
                String fileName = getFileName(javaFile[0]);

                if (fileName.endsWith(TESTER_ENDING)) {
                    continue;
                }

                HashMap<String, Boolean> kgrams = kgrams(tokenize(javaFile[1]));

                // 2. Take out the code every student was given
                HashMap<String, Boolean> template = templateKgrams.get(fileName);
                if (template != null) {
                    for (String kgram : template.keySet()) {
                        kgrams.remove(kgram);
                    }
                }

                // 3. Too little of their own code to compare
                if (kgrams.size() < MIN_KGRAMS) {
                    continue;
                }

                if (!kgramsByFile.containsKey(fileName)) {
                    kgramsByFile.put(fileName, new TreeMap<>());
                }
                kgramsByFile.get(fileName).put(username, kgrams);
            }
        }

        // 4. Compare every pair of students, one file at a time
        ArrayList<SimilarCode> found = new ArrayList<>();

        for (Map.Entry<String, TreeMap<String, HashMap<String, Boolean>>> file
                : kgramsByFile.entrySet()) {
            ArrayList<String> usernames = new ArrayList<>(
                    file.getValue().keySet());

            for (int i = 0; i < usernames.size(); i++) {
                for (int j = i + 1; j < usernames.size(); j++) {
                    double similarity = similarity(
                            file.getValue().get(usernames.get(i)),
                            file.getValue().get(usernames.get(j)));

                    if (similarity >= THRESHOLD) {
                        found.add(new SimilarCode(usernames.get(i),
                                usernames.get(j), file.getKey(),
                                similarity));
                    }
                }
            }
        }

        // 5. Most similar first
        found.sort((first, second) -> Double.compare(
                second.getSimilarity(), first.getSimilarity()));

        return found;
    }
}
