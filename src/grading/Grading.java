package grading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import model.CaseResultStatus;
import model.QuestionResult;
import model.Student;
import model.StudentResult;

// Grades one student's submission, one test case at a time.
//
// For each question, e.g. Q1a, the case testers (Q1aPart1Tester.java,
// Q1aPart2Tester.java, ...) are copied into the student's own question
// folder (Q1) and compiled there together with the student's code. Each
// case then runs as its own program with a time limit, so an infinite
// loop or crash in one case only costs that case.
//
// Everything happens inside the student's folder, so nothing from one
// student can affect another. The output is kept there for checking later,
// e.g. Q1a_compile.txt and Q1aPart1Tester_output.txt.
public class Grading {
    private static final String RUNTIME_ERROR_TEXT = "(Runtime Error)";

    // Grades every case in the question template, which must be loaded
    // first (StudentResult.loadQuestionResultTemplate).
    // usernameFolder is the folder that holds Q1, Q2 and Q3.
    public static StudentResult gradePaper(Student student,
            File usernameFolder, String testerFolder, int timeLimitSeconds) {
        StudentResult studentResult = new StudentResult(student);
        HashMap<String, ArrayList<String>> questionAndCaseIds = studentResult
                .getQuestionAndCaseIds();

        for (Map.Entry<String, ArrayList<String>> question
                : questionAndCaseIds.entrySet()) {
            String questionId = question.getKey();
            ArrayList<String> caseIds = question.getValue();

            try {
                gradeQuestion(studentResult, questionId, caseIds,
                        usernameFolder, new File(testerFolder),
                        timeLimitSeconds);
            } catch (IOException e) {
                setAllCases(studentResult, questionId, caseIds,
                        CaseResultStatus.RUNTIME_ERROR,
                        "Could not grade: " + e.getMessage());
            } catch (InterruptedException e) {
                // Grading was stopped, so leave the rest ungraded
                Thread.currentThread().interrupt();
                break;
            }
        }

        return studentResult;
    }

    private static void gradeQuestion(StudentResult studentResult,
            String questionId, ArrayList<String> caseIds,
            File usernameFolder, File testerFolder, int timeLimitSeconds)
            throws IOException, InterruptedException {
        // e.g. Q1a and Q1b are both in the Q1 folder
        String folderName = questionId.replaceAll("(?<=Q\\d+).+", "");
        File questionFolder = new File(usernameFolder, folderName);
        File studentFile = new File(questionFolder, questionId + ".java");

        if (!studentFile.isFile()) {
            setAllCases(studentResult, questionId, caseIds,
                    CaseResultStatus.MISSING_FILE,
                    studentFile.getName() + " was not submitted");
            return;
        }

        // 1. Copy the case testers next to the student's code
        ArrayList<String> testerNames = new ArrayList<>();

        for (String caseId : caseIds) {
            String testerName = questionId + "Part" + caseId + "Tester";
            File source = new File(new File(testerFolder, questionId),
                    testerName + ".java");
            File target = new File(questionFolder, testerName + ".java");

            Files.copy(source.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            testerNames.add(testerName);
        }

        // Also copy the class files given with the exam (e.g. Shape.class,
        // DataException.class), so a student who deleted them is not
        // marked down for it
        File[] testerFiles = new File(testerFolder, questionId).listFiles();

        if (testerFiles != null) {
            for (File testerFile : testerFiles) {
                if (testerFile.getName().endsWith(".class")) {
                    File target = new File(questionFolder,
                            testerFile.getName());
                    Files.copy(testerFile.toPath(), target.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        // 2. Generate question tester folder for faster marking
        boolean generateSuccess = generateQuestionTester(questionId, questionFolder, testerNames);

        // 3. Compile them once, together with the student's code
        File compileFile = new File(questionFolder,
                questionId + "_compile.txt");

        if (!compile(questionFolder, testerNames, compileFile,
            questionId, generateSuccess)) {
            setAllCases(studentResult, questionId, caseIds,
                    CaseResultStatus.COMPILE_ERROR,
                    firstErrorLine(compileFile));
            return;
        }

        // 4. Run all cases in the question folder (to make the program faster)
        boolean allCasesSuccess = false;
        if (generateSuccess) {
            allCasesSuccess = runAllCases(studentResult, questionId, caseIds, testerNames, questionFolder, timeLimitSeconds);
        }

        // 5. If step 4 fails, run each case on its own
        if (!allCasesSuccess) {
            for (int i = 0; i < caseIds.size(); i++) {
                runCase(studentResult, questionId, caseIds.get(i),
                        testerNames.get(i), questionFolder, timeLimitSeconds);
            }
        }
    }

    private static boolean generateQuestionTester(String questionId,
        File questionFolder, ArrayList<String> testerNames) {
        // generate a tester file for the question
        String questionTesterName = questionId + "Tester.java";
        File questionTester = new File(questionFolder, questionTesterName);
        try (PrintStream ps = new PrintStream(questionTester)) {
            // generate public class, psvm
            ps.println("public class " + questionId + "Tester {");
            ps.println("public static void main(String[] args) {");
            // run tester grade
            for (String testerName : testerNames) {
                ps.println("new " + testerName + "().grade();");
            }
            // close
            ps.println("}}");
        } catch (FileNotFoundException e) {
            // skip marking all questions at once if it fails
            return false;
        }

        return true;
    }

    // Returns true if everything compiled without errors
    private static boolean compile(File questionFolder,
            ArrayList<String> testerNames, File compileFile,
            String questionId, boolean generateSuccess)
            throws IOException, InterruptedException {
        ArrayList<String> command = new ArrayList<>();
        command.add("javac");
        // "." lets javac find the student's code next to the testers
        command.add("-cp");
        command.add("." + File.pathSeparator + getGraderClassPath());

        for (String testerName : testerNames) {
            command.add(testerName + ".java");
        }

        // also compile question tester if generating was successful
        if (generateSuccess) {
            command.add(questionId + "Tester.java");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(questionFolder);
        pb.redirectErrorStream(true);
        pb.redirectOutput(compileFile);

        return pb.start().waitFor() == 0;
    }

    private static boolean runAllCases(StudentResult studentResult,
            String questionId, ArrayList<String> caseIds, ArrayList<String> testerNames,
            File questionFolder, int timeLimitSeconds)
            throws IOException, InterruptedException {
        File outputFile = new File(questionFolder,
                questionId + "Tester_output.txt");

        // Runs from the project folder (not the question folder), because
        // the testers give their data files as paths from there,
        // e.g. "Project sample/TesterFiles/personstester.txt"
        String questionTesterName = questionId + "Tester";
        ProcessBuilder pb = new ProcessBuilder("java", "-cp",
                questionFolder.getAbsolutePath() + File.pathSeparator
                        + getGraderClassPath(),
                questionTesterName);
        pb.redirectErrorStream(true);
        pb.redirectOutput(outputFile);

        Process process = pb.start();

        if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
            // Still running after the time limit, so stop it
            process.destroyForcibly();
            process.waitFor();
            return false;
        }

        List<String> resultLines = findResultLines(outputFile, questionId);

        System.out.println(resultLines);

        if (resultLines.size() == 0) {
            // e.g. the student's code threw an exception the tester
            // did not catch, so no result was printed
            return false;
        }

        // A result line is: question,case,expected,actual,passed,marks
        // The expected and actual values can contain commas themselves,
        // so the fields are read from the end of the line.
        // data is split between lines, so first split by \n
        for (String resultLine : resultLines) {
            String[] fields = resultLine.split(",");
            String caseId = fields[1];
            double marks = Double.parseDouble(fields[fields.length - 1]);
            String actual = fields[fields.length - 3];
            CaseResultStatus status;

            if (marks > 0) {
                status = CaseResultStatus.PASSED;
            } else if (actual.equals(RUNTIME_ERROR_TEXT)) {
                status = CaseResultStatus.RUNTIME_ERROR;
            } else {
                status = CaseResultStatus.FAILED;
            }

            System.out.println(caseId);
            studentResult.addCaseResult(questionId, caseId, marks, status, "");
        }
        return true;
    }

    private static void runCase(StudentResult studentResult,
            String questionId, String caseId, String testerName,
            File questionFolder, int timeLimitSeconds)
            throws IOException, InterruptedException {
        File outputFile = new File(questionFolder,
                testerName + "_output.txt");

        // Runs from the project folder (not the question folder), because
        // the testers give their data files as paths from there,
        // e.g. "Project sample/TesterFiles/personstester.txt"
        ProcessBuilder pb = new ProcessBuilder("java", "-cp",
                questionFolder.getAbsolutePath() + File.pathSeparator
                        + getGraderClassPath(),
                testerName);
        pb.redirectErrorStream(true);
        pb.redirectOutput(outputFile);

        Process process = pb.start();

        if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
            // Still running after the time limit, so stop it
            process.destroyForcibly();
            process.waitFor();
            studentResult.addCaseResult(questionId, caseId, 0.0,
                    CaseResultStatus.TIMEOUT, "Took longer than "
                            + timeLimitSeconds + " seconds");
            return;
        }

        String resultLine = findResultLine(outputFile, questionId, caseId);

        if (resultLine == null) {
            // e.g. the student's code threw an exception the tester
            // did not catch, so no result was printed
            studentResult.addCaseResult(questionId, caseId, 0.0,
                    CaseResultStatus.RUNTIME_ERROR,
                    "No result was printed, see " + outputFile.getName());
            return;
        }

        // A result line is: question,case,expected,actual,passed,marks
        // The expected and actual values can contain commas themselves,
        // so the fields are read from the end of the line.
        String[] fields = resultLine.split(",");
        double marks = Double.parseDouble(fields[fields.length - 1]);
        String actual = fields[fields.length - 3];
        CaseResultStatus status;

        if (marks > 0) {
            status = CaseResultStatus.PASSED;
        } else if (actual.equals(RUNTIME_ERROR_TEXT)) {
            status = CaseResultStatus.RUNTIME_ERROR;
        } else {
            status = CaseResultStatus.FAILED;
        }

        studentResult.addCaseResult(questionId, caseId, marks, status, "");
    }

    // Returns the tester's result line, or null if there is none.
    // Anything else in the output (e.g. the student's own println
    // messages) is ignored.
    private static String findResultLine(File outputFile, String questionId,
            String caseId) throws IOException {
        Pattern resultPattern = Pattern.compile(
                Pattern.quote(questionId + "," + caseId + ",")
                        + ".*,.*,(true|false),\\d+(\\.\\d+)?");

        // ISO-8859-1 can read any bytes, so unusual characters printed by
        // the student's code cannot make reading fail
        List<String> lines = Files.readAllLines(outputFile.toPath(),
                StandardCharsets.ISO_8859_1);
        String resultLine = null;

        for (String line : lines) {
            if (resultPattern.matcher(line.trim()).matches()) {
                resultLine = line.trim();
            }
        }

        return resultLine;
    }

    // questionId only
    private static List<String> findResultLines(File outputFile, String questionId)
    throws IOException {
        Pattern resultPattern = Pattern.compile(
                Pattern.quote(questionId + ",")
                        + "\\d+,.*,.*,(true|false),\\d+(\\.\\d+)?");

        // ISO-8859-1 can read any bytes, so unusual characters printed by
        // the student's code cannot make reading fail
        List<String> lines = Files.readAllLines(outputFile.toPath(),
                StandardCharsets.ISO_8859_1);
        List<String> resultLines = new ArrayList<>();

        for (String line : lines) {
            System.out.println(line);
            if (resultPattern.matcher(line.trim()).matches()) {
                resultLines.add(line.trim());
            }
        }

        return resultLines;
    }

    // Returns the first compile error, e.g.
    // "Q1a.java:18: error: cannot find symbol"
    private static String firstErrorLine(File compileFile)
            throws IOException {
        List<String> lines = Files.readAllLines(compileFile.toPath(),
                StandardCharsets.ISO_8859_1);

        for (String line : lines) {
            if (line.contains("error")) {
                return line.trim();
            }
        }

        return "Compile error, see " + compileFile.getName();
    }

    private static void setAllCases(StudentResult studentResult,
            String questionId, ArrayList<String> caseIds,
            CaseResultStatus status, String details) {
        for (String caseId : caseIds) {
            studentResult.addCaseResult(questionId, caseId, 0.0, status,
                    details);
        }
    }

    // The grader's own classpath with every entry made absolute, so the
    // testers can find model.Tester whichever folder they run from
    private static String getGraderClassPath() {
        String[] entries = System.getProperty("java.class.path")
                .split(File.pathSeparator);
        ArrayList<String> absoluteEntries = new ArrayList<>();

        for (String entry : entries) {
            if (!entry.isEmpty()) {
                absoluteEntries.add(new File(entry).getAbsolutePath());
            }
        }

        return String.join(File.pathSeparator, absoluteEntries);
    }

    public static void main(String[] args) {
        StudentResult.loadStudentResultTemplate();
        StudentResult result = gradePaper(
            new Student("#01400002", "chee.teo.2022", "Teo", "Chee", "chee.teo.2022@computing.smu.edu.sg"),
            new File("unzippedFolder\\2023-2024-chee.teo.2022\\chee.teo.2022"),
            "Project sample/TesterFiles",
            5
        );

        for (QuestionResult qr : result.getQuestionResults().values()) {
            System.out.println(qr.getCases());
            System.out.println(qr.getScore());
        }
        
        System.out.println(result);
    }
}
