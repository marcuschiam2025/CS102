package plagiarism;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import filehandling.FileHandlings;
import fixer.UsernameResolver;
import grading.Grading;
import model.Student;

// Plagiarism check 1: students who give the same wrong answer.
//
// Two students getting a test case right tells us nothing, because
// everyone who is right gives the same answer. Two students getting it
// wrong in exactly the same way is unusual, so it is worth a closer look.
//
// It reads the tester output that grading leaves in each student's
// folder (e.g. Q1/Q1aPart1Tester_output.txt), so grade first.
public class WrongOutputChecker {
    // Where the blank exam template is graded, inside the unzip folder
    private static final String TEMPLATE_COPY_FOLDER = "_template";
    private static final String TEMPLATE_USERNAME = "template";
    private static final String OUTPUT_FILE_ENDING = "Tester_output.txt";
    private static final String COMPILE_FILE_ENDING = "_compile.txt";
    // The tester prints this when the student's code threw an exception.
    // It says nothing about the answer itself, so it is not compared.
    private static final String RUNTIME_ERROR_TEXT = "(Runtime Error)";
    // Fewer students than this and there is nothing to compare
    private static final int MIN_STUDENTS = 2;

    // e.g. "Q1aPart3Tester_output.txt" -> Q1a and 3
    private static final Pattern OUTPUT_FILE_NAME = Pattern.compile(
            "(Q\\d+[a-z]?)Part(\\d+)" + Pattern.quote(OUTPUT_FILE_ENDING));

    // Returns the pairs of students who share at least one unusual wrong
    // answer, highest score first.
    // unzipFolder holds one folder per zip, e.g.
    // unzippedFolder/2023-2024-ping.lee.2023/ping.lee.2023/Q1/...
    // templateFolder is the blank exam (RenameToYourUsername).
    // The question template must be loaded first
    // (StudentResult.loadQuestionResultTemplate).
    public static ArrayList<SharedWrongOutputPair> findSharedWrongOutputPairs(
            File unzipFolder, File templateFolder, String testerFolder,
            int timeLimitSeconds) throws IOException {
        // 1. Answers the blank template gives, e.g. Q1b's "return -1;".
        //    A student who left the template unchanged gives these too,
        //    which is not copying, so they are ignored.
        HashMap<String, Boolean> templateOutputs = gradeTemplate(unzipFolder,
                templateFolder, testerFolder, timeLimitSeconds);

        // 2. Group identical wrong answers, so each one knows which
        //    students gave it
        HashMap<String, SharedWrongOutput> wrongOutputs = new HashMap<>();
        int studentCount = 0;

        for (File zipFolder : listFolders(unzipFolder)) {
            String username = UsernameResolver.getUsername(
                    zipFolder.getName());
            File usernameFolder = new File(zipFolder,
                    username == null ? "" : username);

            if (username == null || !usernameFolder.isDirectory()) {
                continue;
            }

            if (!isGraded(usernameFolder)) {
                continue;
            }

            ArrayList<String[]> studentOutputs = readWrongOutputs(
                    usernameFolder);
            studentCount++;

            for (String[] wrongOutput : studentOutputs) {
                String key = toKey(wrongOutput);

                if (templateOutputs.containsKey(key)) {
                    continue;
                }

                if (!wrongOutputs.containsKey(key)) {
                    wrongOutputs.put(key, new SharedWrongOutput(
                            wrongOutput[0], wrongOutput[1], wrongOutput[2]));
                }

                wrongOutputs.get(key).addUsername(username);
            }
        }

        if (studentCount < MIN_STUDENTS) {
            return new ArrayList<>();
        }

        // 3. Rarer answers count for more. An answer shared by 2 of 100
        //    students is strong evidence, while one shared by 60 of them
        //    is a common mistake. Shared by everyone gives log(1) = 0.
        // 4. Add each shared answer to every pair of students who gave it
        HashMap<String, SharedWrongOutputPair> pairs = new HashMap<>();

        for (SharedWrongOutput wrongOutput : wrongOutputs.values()) {
            ArrayList<String> usernames = wrongOutput.getUsernames();
            double weight = Math.log((double) studentCount
                    / usernames.size());

            if (usernames.size() < MIN_STUDENTS || weight <= 0) {
                continue;
            }

            wrongOutput.setWeight(weight);

            for (int i = 0; i < usernames.size(); i++) {
                for (int j = i + 1; j < usernames.size(); j++) {
                    addToPair(pairs, usernames.get(i), usernames.get(j),
                            wrongOutput);
                }
            }
        }

        ArrayList<SharedWrongOutputPair> sharedWrongOutputPairs = new ArrayList<>(
                pairs.values());
        sharedWrongOutputPairs.sort((first, second) -> Double.compare(
                second.getScore(), first.getScore()));

        // Show each pair's evidence in question order (Q1a case 1, ...)
        for (SharedWrongOutputPair pair : sharedWrongOutputPairs) {
            pair.getSharedWrongOutputs().sort((first, second) -> {
                int byQuestion = first.getQuestionId().compareTo(
                        second.getQuestionId());

                if (byQuestion != 0) {
                    return byQuestion;
                }

                return Integer.compare(Integer.parseInt(first.getCaseId()),
                        Integer.parseInt(second.getCaseId()));
            });
        }

        return sharedWrongOutputPairs;
    }

    // Grades a copy of the blank template and returns its wrong answers.
    // Only the keys are used: each one is an answer the template gives,
    // and the value is always true.
    // A copy is used so the grader's output files do not end up in the
    // exam resources.
    private static HashMap<String, Boolean> gradeTemplate(File unzipFolder,
            File templateFolder, String testerFolder, int timeLimitSeconds)
            throws IOException {
        File templateCopy = new File(unzipFolder, TEMPLATE_COPY_FOLDER);
        FileHandlings.deleteFolder(templateCopy);
        copyFolder(templateFolder.toPath(), templateCopy.toPath());

        Student templateStudent = new Student("", TEMPLATE_USERNAME, "", "",
                "");
        Grading.gradePaper(templateStudent, templateCopy, testerFolder,
                timeLimitSeconds);

        HashMap<String, Boolean> templateOutputs = new HashMap<>();

        for (String[] wrongOutput : readWrongOutputs(templateCopy)) {
            templateOutputs.put(toKey(wrongOutput), true);
        }

        return templateOutputs;
    }

    // Returns every case the student got wrong, as
    // { questionId, caseId, "expected,actual" }
    private static ArrayList<String[]> readWrongOutputs(File usernameFolder)
            throws IOException {
        ArrayList<String[]> wrongOutputs = new ArrayList<>();

        for (File questionFolder : listFolders(usernameFolder)) {
            File[] files = questionFolder.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                Matcher fileName = OUTPUT_FILE_NAME.matcher(file.getName());

                if (!fileName.matches()) {
                    continue;
                }

                String questionId = fileName.group(1);
                String caseId = fileName.group(2);
                String output = readWrongOutput(file, questionId, caseId);

                if (output != null) {
                    wrongOutputs.add(new String[] { questionId, caseId,
                            output });
                }
            }
        }

        return wrongOutputs;
    }

    // Returns "expected,actual" if the case scored 0, or null if it
    // passed, timed out, crashed or threw an exception
    private static String readWrongOutput(File outputFile,
            String questionId, String caseId) throws IOException {
        // A result line is: question,case,expected,actual,passed,marks
        // Only lines for this exact case are read, so anything else the
        // student's code printed is ignored.
        Pattern resultLine = Pattern.compile(
                Pattern.quote(questionId + "," + caseId + ",")
                        + "(.*),(true|false),(\\d+(\\.\\d+)?)");

        // ISO-8859-1 can read any bytes, so unusual characters printed by
        // the student's code cannot make reading fail
        List<String> lines = Files.readAllLines(outputFile.toPath(),
                StandardCharsets.ISO_8859_1);
        String output = null;

        for (String line : lines) {
            Matcher matcher = resultLine.matcher(line.trim());

            if (!matcher.matches()) {
                continue;
            }

            double marks = Double.parseDouble(matcher.group(3));
            String expectedAndActual = matcher.group(1);

            if (marks > 0 || expectedAndActual.endsWith(RUNTIME_ERROR_TEXT)) {
                output = null;
            } else {
                output = expectedAndActual;
            }
        }

        return output;
    }

    // Grading writes a compile file (e.g. Q1a_compile.txt) for every
    // question it grades, even one that does not compile
    private static boolean isGraded(File usernameFolder) {
        for (File questionFolder : listFolders(usernameFolder)) {
            File[] files = questionFolder.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (file.getName().endsWith(COMPILE_FILE_ENDING)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Keeps one SharedWrongOutputPair per two students, whichever order they
    // come in
    private static void addToPair(HashMap<String, SharedWrongOutputPair> pairs,
            String firstUsername, String secondUsername,
            SharedWrongOutput wrongOutput) {
        if (firstUsername.compareTo(secondUsername) > 0) {
            String temp = firstUsername;
            firstUsername = secondUsername;
            secondUsername = temp;
        }

        String key = firstUsername + "|" + secondUsername;

        if (!pairs.containsKey(key)) {
            pairs.put(key, new SharedWrongOutputPair(firstUsername, secondUsername));
        }

        pairs.get(key).addSharedWrongOutput(wrongOutput);
    }

    // e.g. { "Q1b", "1", "6,-1" } -> "Q1b,1,6,-1"
    private static String toKey(String[] wrongOutput) {
        return wrongOutput[0] + "," + wrongOutput[1] + "," + wrongOutput[2];
    }

    // Returns the sub-folders, skipping the template copy
    private static ArrayList<File> listFolders(File folder) {
        ArrayList<File> folders = new ArrayList<>();
        File[] items = folder.listFiles();

        if (items == null) {
            return folders;
        }

        for (File item : items) {
            if (item.isDirectory()
                    && !item.getName().equals(TEMPLATE_COPY_FOLDER)) {
                folders.add(item);
            }
        }

        return folders;
    }

    private static void copyFolder(Path source, Path target)
            throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                Path copy = target.resolve(source.relativize(path));

                if (Files.isDirectory(path)) {
                    Files.createDirectories(copy);
                } else {
                    Files.copy(path, copy,
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
