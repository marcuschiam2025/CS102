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
// Words used in this file:
//   answer       one wrong answer to one test case, as a String[]:
//                { questionId, caseId, "expected,actual" }
//                e.g. { "Q2b", "3", "LEE Teck Yang-4.0,Jason LEE" }
//   answer key   the same answer as one String, so it can be looked up:
//                "Q2b,3,LEE Teck Yang-4.0,Jason LEE"
//   answer group one answer and every student who gave it
//                (a SharedWrongOutput)
//   pair         two students and the answers they share
//                (a SharedWrongOutputPair)
public class WrongOutputChecker {
    // Where the blank exam template is graded, inside the unzip folder
    private static final String TEMPLATE_COPY_FOLDER = "_template";
    private static final String TEMPLATE_USERNAME = "template";
    private static final String OUTPUT_FILE_ENDING = "Tester_output.txt";
    private static final String COMPILE_FILE_ENDING = "_compile.txt";
    private static final String RUNTIME_ERROR_TEXT = "(Runtime Error)";
    // Fewer students than this and there is nothing to compare
    private static final int MIN_STUDENTS = 2;

    // e.g. "Q2bTester_output.txt" or "Q2bPart3Tester_output.txt".
    // Group 1 is the question, e.g. "Q2b".
    private static final Pattern OUTPUT_FILE_NAME_PATTERN = Pattern.compile(
            "(Q\\d+[a-z]?)(Part\\d+)?" + Pattern.quote(OUTPUT_FILE_ENDING));

    // Returns the pairs of students who share at least one unusual wrong
    // answer, highest score first.
    // unzipFolder holds one folder per zip, e.g.
    // unzippedFolder/2023-2024-ping.lee.2023/ping.lee.2023/Q1/...
    // templateFolder is the blank exam (RenameToYourUsername).
    // The question template must be loaded first
    // (StudentResult.loadStudentResultTemplate).
    public static ArrayList<SharedWrongOutputPair> findSharedWrongOutputPairs(
            File unzipFolder, File templateFolder, String testerFolder,
            int timeLimitSeconds) throws IOException {
        // 1. Answers the blank template gives, e.g. Q1b's "return -1;".
        // A student who left the template unchanged gives these too,
        // which is not copying, so they are ignored.
        HashMap<String, Boolean> templateAnswers = gradeTemplate(unzipFolder,
                templateFolder, testerFolder, timeLimitSeconds);

        // 2. Group identical wrong answers, so each one knows which
        // students gave it: answer key -> answer group
        HashMap<String, SharedWrongOutput> answerGroups = new HashMap<>();
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

            ArrayList<String[]> studentWrongAnswers = readWrongAnswers(
                    usernameFolder);
            studentCount++;

            for (String[] wrongAnswer : studentWrongAnswers) {
                String answerKey = toAnswerKey(wrongAnswer);

                if (templateAnswers.containsKey(answerKey)) {
                    continue;
                }

                // The first student with this answer starts its group
                if (!answerGroups.containsKey(answerKey)) {
                    answerGroups.put(answerKey, new SharedWrongOutput(
                            wrongAnswer[0], wrongAnswer[1], wrongAnswer[2]));
                }

                // Add this student to the group
                answerGroups.get(answerKey).addUsername(username);
            }
        }

        if (studentCount < MIN_STUDENTS) {
            return new ArrayList<>();
        }

        // 3. Rarer answers count for more. An answer shared by 2 of 100
        // students is strong evidence, while one shared by 60 of them
        // is a common mistake. Shared by everyone gives log(1) = 0.
        // 4. Add each shared answer to every pair of students who gave it:
        // pair key -> pair
        HashMap<String, SharedWrongOutputPair> pairs = new HashMap<>();

        for (SharedWrongOutput answerGroup : answerGroups.values()) {
            ArrayList<String> usernames = answerGroup.getUsernames();
            double weight = Math.log((double) studentCount
                    / usernames.size());

            if (usernames.size() < MIN_STUDENTS || weight <= 0) {
                continue;
            }

            answerGroup.setWeight(weight);

            for (int i = 0; i < usernames.size(); i++) {
                for (int j = i + 1; j < usernames.size(); j++) {
                    addToPair(pairs, usernames.get(i), usernames.get(j),
                            answerGroup);
                }
            }
        }

        // 5. Highest score first
        ArrayList<SharedWrongOutputPair> sortedPairs = new ArrayList<>(
                pairs.values());
        sortedPairs.sort((pair1, pair2) -> Double.compare(
                pair2.getScore(), pair1.getScore()));

        // Show each pair's answers in question order (Q1a case 1, ...)
        for (SharedWrongOutputPair pair : sortedPairs) {
            pair.getSharedWrongOutputs().sort((answer1, answer2) -> {
                int questionOrder = answer1.getQuestionId().compareTo(
                        answer2.getQuestionId());

                if (questionOrder != 0) {
                    return questionOrder;
                }

                return Integer.compare(Integer.parseInt(answer1.getCaseId()),
                        Integer.parseInt(answer2.getCaseId()));
            });
        }

        return sortedPairs;
    }

    // Grades a copy of the blank template and returns its wrong answers,
    // as answer keys. Only the keys are used: the value is always true.
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

        HashMap<String, Boolean> templateAnswers = new HashMap<>();

        for (String[] wrongAnswer : readWrongAnswers(templateCopy)) {
            templateAnswers.put(toAnswerKey(wrongAnswer), true);
        }

        return templateAnswers;
    }

    // Returns every case the student got wrong, as
    // { questionId, caseId, "expected,actual" }
    private static ArrayList<String[]> readWrongAnswers(File usernameFolder)
            throws IOException {
        // The newest result for each case, by "questionId,caseId".
        // A folder can hold both kinds of output file (e.g. one left from
        // an older grading run), so the newest file wins.
        HashMap<String, String[]> latestResults = new HashMap<>();
        HashMap<String, Long> latestResultTimes = new HashMap<>();

        for (File questionFolder : listFolders(usernameFolder)) {
            File[] files = questionFolder.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                Matcher fileNameMatch = OUTPUT_FILE_NAME_PATTERN.matcher(
                        file.getName());

                // Not a tester output file, e.g. Q2b.java
                if (!fileNameMatch.matches()) {
                    continue;
                }

                String questionId = fileNameMatch.group(1);
                long fileTime = file.lastModified();

                for (String[] result : readResults(file, questionId)) {
                    String caseKey = result[0] + "," + result[1];

                    if (!latestResultTimes.containsKey(caseKey)
                            || fileTime >= latestResultTimes.get(caseKey)) {
                        latestResults.put(caseKey, result);
                        latestResultTimes.put(caseKey, fileTime);
                    }
                }
            }
        }

        // Keep only the wrong ones: readResults leaves "expected,actual"
        // null for a case that passed or crashed
        ArrayList<String[]> wrongAnswers = new ArrayList<>();

        for (String[] result : latestResults.values()) {
            if (result[2] != null) {
                wrongAnswers.add(result);
            }
        }

        return wrongAnswers;
    }

    // Returns { questionId, caseId, "expected,actual" } for every case in
    // the file. "expected,actual" is null if the case passed or threw an
    // exception, so only real wrong answers are compared.
    private static ArrayList<String[]> readResults(File outputFile,
            String questionId) throws IOException {
        // A result line is: question,case,expected,actual,passed,marks
        // Only lines for this question are read, so anything else the
        // student's code printed is ignored.
        Pattern resultLinePattern = Pattern.compile(
                Pattern.quote(questionId + ",")
                        + "(\\d+),(.*),(true|false),(\\d+(\\.\\d+)?)");

        // ISO-8859-1 can read any bytes, so unusual characters printed by
        // the student's code cannot make reading fail
        List<String> lines = Files.readAllLines(outputFile.toPath(),
                StandardCharsets.ISO_8859_1);
        ArrayList<String[]> results = new ArrayList<>();

        for (String line : lines) {
            Matcher lineMatch = resultLinePattern.matcher(line.trim());

            if (!lineMatch.matches()) {
                continue;
            }

            String caseId = lineMatch.group(1);
            String expectedAndActual = lineMatch.group(2);
            double marks = Double.parseDouble(lineMatch.group(4));

            // Got marks (right) or crashed: not a wrong answer
            if (marks > 0 || expectedAndActual.endsWith(RUNTIME_ERROR_TEXT)) {
                expectedAndActual = null;
            }

            results.add(new String[] { questionId, caseId,
                    expectedAndActual });
        }

        return results;
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
            SharedWrongOutput answerGroup) {
        // Alphabetical order, so (a, b) and (b, a) give the same pair key
        if (firstUsername.compareTo(secondUsername) > 0) {
            String temp = firstUsername;
            firstUsername = secondUsername;
            secondUsername = temp;
        }

        String pairKey = firstUsername + "|" + secondUsername;

        if (!pairs.containsKey(pairKey)) {
            pairs.put(pairKey, new SharedWrongOutputPair(firstUsername,
                    secondUsername));
        }

        pairs.get(pairKey).addSharedWrongOutput(answerGroup);
    }

    // e.g. { "Q1b", "1", "6,-1" } -> "Q1b,1,6,-1"
    private static String toAnswerKey(String[] answer) {
        return answer[0] + "," + answer[1] + "," + answer[2];
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
