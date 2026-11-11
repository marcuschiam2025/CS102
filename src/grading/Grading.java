package grading;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.StudentResult;
import model.Student;
import model.CaseResultStatus;

// Co-authored with Claude

public class Grading {
    public static File errorFile = new File("error.txt");
    public static File outputFile = new File("output.txt");
    // The compiled app (made by compile.bat / compile.sh), which holds
    // model/Tester.class that every tester extends
    private static final String CLASSES_FOLDER = "src/classes";

    private static void copyTesterFiles(File testerFolder, File questionFolder) throws IOException {
        for (File testerFile : testerFolder.listFiles()) {
            File questionFile = new File(questionFolder, testerFolder.getName().replaceAll("(?<=Q\\d+).+", "") + "/" + testerFile.getName());
            
            if (testerFile.getName().endsWith(".java") || testerFile.getName().endsWith(".class")) {
                Files.copy(testerFile.toPath(), questionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static ArrayList<String> readErrorFile() {
        ArrayList<String> output = new ArrayList<>();

        try (Scanner sc = new Scanner(errorFile);) {
            while (sc.hasNext()) {
                output.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return output;
    }

    // A result line is: question,case,expected,actual,passed,points
    // e.g. "Q1a,2,[alice, ben],[alice],false,0.0". The question and case are
    // read from the start and passed/points from the end, so commas inside
    // expected/actual (e.g. "{a=1, b=2}") do not matter. Anything the
    // student's code printed before the result on the same line is skipped.
    private static final Pattern RESULT_LINE = Pattern.compile(
            "^.*?(Q\\d+[a-z]?),(\\d+),(.*),(true|false),(-?\\d+(?:\\.\\d+)?)$");
    private static final String RUNTIME_ERROR_TEXT = "(Runtime Error)";

    // Reads the tester output and returns how many results it found
    private static int readOutputFile(StudentResult studentResult) {
        int resultCount = 0;

        try (Scanner sc = new Scanner(outputFile);) {
            while (sc.hasNextLine()) {
                Matcher result = RESULT_LINE.matcher(sc.nextLine().trim());

                if (!result.matches()) {
                    continue;
                }

                CaseResultStatus status;
                if (Boolean.parseBoolean(result.group(4))) {
                    status = CaseResultStatus.PASSED;
                } else if (result.group(3).endsWith(RUNTIME_ERROR_TEXT)) {
                    status = CaseResultStatus.RUNTIME_ERROR;
                } else {
                    status = CaseResultStatus.FAILED;
                }

                studentResult.addCaseResult(result.group(1), result.group(2), Double.parseDouble(result.group(5)), status, "");
                resultCount++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resultCount;
    }

    // javac's error formats: "Q1a.java:5: error: ';' expected" or a line
    // starting with "error:", e.g. "error: compilation failed". An exception
    // message such as "parse error: bad input" does not match either.
    private static final Pattern COMPILE_ERROR_LINE = Pattern.compile(
            "^error: .*|.*\\.java:\\d+: error: .*");

    // True if the error output shows the code did not compile
    private static boolean hasCompileError(ArrayList<String> errorText) {
        for (String line : errorText) {
            if (COMPILE_ERROR_LINE.matcher(line).matches()) {
                return true;
            }
        }

        return false;
    }

    // Compiles each student file (e.g. Q2a.java) on its own, so one file that
    // does not compile (e.g. Q3.java) does not stop the others from compiling
    private static void compileStudentFiles(File[] files, int timeLimitSeconds) {
        for (File file : files) {
            String fileName = file.getName();

            if (!fileName.endsWith(".java") || fileName.endsWith("Tester.java")) {
                continue;
            }

            ArrayList<String> commands = new ArrayList<>();
            commands.add("javac");
            commands.add("-cp");
            commands.add(CLASSES_FOLDER +File.pathSeparator + file.getParent());
            commands.add("-d");
            commands.add(file.getParent());
            commands.add(file.getPath());

            ProcessBuilder compilePb = new ProcessBuilder(commands);
            compilePb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            compilePb.redirectError(ProcessBuilder.Redirect.DISCARD);

            try {
                Process process = compilePb.start();
                if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Keeps a copy of the tester output and errors in the question folder,
    // e.g. Q1/Q1aTester_output.txt and Q1/Q1a_compile.txt, because
    // output.txt and error.txt are deleted after grading.
    // The plagiarism check for shared wrong answers reads these copies.
    private static void saveOutput(File folder, String outputName, String questionId) {
        try {
            if (outputFile.exists()) {
                Files.copy(outputFile.toPath(), new File(folder, outputName + "_output.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Also marks the question as graded, even if nothing compiled
            File compileFile = new File(folder, questionId + "_compile.txt");
            if (errorFile.exists()) {
                Files.copy(errorFile.toPath(), compileFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                compileFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTesterFiles(File[] testerFiles, File questionFolder, int timeLimitSeconds, StudentResult studentResult) {
        // the testers below are run as source files, which does not compile the
        // student's code, so compile it first
        compileStudentFiles(testerFiles, timeLimitSeconds);

        for (File testerFile : testerFiles) {
            if (testerFile.getName().endsWith("Tester.java")) {
                ProcessBuilder questionPb = new ProcessBuilder();

                String testerName = testerFile.getName();

                ArrayList<String> commands = new ArrayList<>();
                commands.add("java");
                commands.add("-cp");
                commands.add(CLASSES_FOLDER +File.pathSeparator + testerFile.getParent());
                commands.add(testerFile + "");

                questionPb.command(commands);

                questionPb.redirectOutput(outputFile);
                questionPb.redirectError(errorFile);

                boolean success = false;

                try {
                    Process process = questionPb.start();
                    // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
                    success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);

                    if (!success) {
                        // Still running after the time limit, so stop it
                        process.destroyForcibly();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // e.g. Q1aPart2Tester.java -> Q1/Q1aPart2Tester_output.txt
                saveOutput(testerFile.getParentFile(), testerName.replaceAll("\\.java$", ""), testerName.replaceAll("Part\\d+Tester.java$", ""));

                String questionId = testerName.replaceAll("Part\\d+Tester.java$", "");
                String caseId = testerName.replaceAll("^Q.+Part(?=\\d+Tester\\.java)|Tester\\.java", "");

                if (!success) {
                    studentResult.addCaseResult(questionId, caseId, 0.0, CaseResultStatus.TIMEOUT, "");
                    continue;
                }

                // Use the tester's result if it printed one, even if the
                // student's code also printed to the error output (e.g.
                // e.printStackTrace() for a missing file). With no result,
                // the code either did not compile or threw an exception.
                if (readOutputFile(studentResult) == 0) {
                    CaseResultStatus status = hasCompileError(readErrorFile()) ? CaseResultStatus.COMPILE_ERROR : CaseResultStatus.RUNTIME_ERROR;
                    studentResult.addCaseResult(questionId, caseId, 0.0, status, "");
                }
            }
        }
    }

    public static boolean runAllTesterFiles(HashMap<String, ArrayList<String>> questionAndCaseIds, File questionFolders, String testerFolder, int timeLimitSeconds) {
        File[] questionTesterFolders = new File(testerFolder).listFiles();
        Path questionTesterPath = FileSystems.getDefault().getPath(questionFolders.getPath(), "QuestionTester.java");
        // what happens when a question folder is missing? give 0 points
        // The PrintStream below creates this file, or overwrites one left
        // behind by an earlier run that crashed
        File questionTester = questionTesterPath.toFile();

        for (File questionTesterFolder : questionTesterFolders) {
            String questionId = questionTesterFolder.getName();

            if (questionAndCaseIds.containsKey(questionId)) {
                try {
                    copyTesterFiles(questionTesterFolder, questionFolders);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

        try (PrintStream ps = new PrintStream(questionTester);) {
            ps.println("public class QuestionTester { public static void main(String[] args) {");

            for (Map.Entry<String, ArrayList<String>> question : questionAndCaseIds.entrySet()) {
                for (String caseId : question.getValue()) {
                    // Not every tester catches the student's exceptions, so
                    // print a runtime error result here (Throwable also
                    // catches errors such as StackOverflowError).
                    // A LinkageError (e.g. NoClassDefFoundError) means the
                    // student's class did not compile, so it is printed to
                    // the error output instead, which makes gradePaper run
                    // each tester on its own and mark it as a compile error.
                    ps.println("try { new " + question.getKey() + "Part" + caseId + "Tester().grade(); } catch (LinkageError e) { e.printStackTrace(); } catch (Throwable e) { System.out.println(); System.out.println(\"" + question.getKey() + "," + caseId + ",-," + RUNTIME_ERROR_TEXT + ",false,0.0\"); }");
                }
            }

            ps.println("} }");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ProcessBuilder questionPb = new ProcessBuilder();

        ArrayList<String> commands = new ArrayList<>();

        String classPath = "";
        for (File questionFolder : Arrays.asList(questionFolders.listFiles())) {
            classPath += File.pathSeparator + questionFolder;
        }

        commands.add("javac");
        commands.add("-cp");
        commands.add(CLASSES_FOLDER +classPath);
        commands.add(questionFolders + "/QuestionTester.java");

        questionPb.command(commands);
        
        try {
            Process process = questionPb.start();
            // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
            if (!process.waitFor(timeLimitSeconds, TimeUnit.SECONDS)) {
                // Still running after the time limit, so stop it
                process.destroyForcibly();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        questionPb = new ProcessBuilder();

        commands = new ArrayList<>();

        commands.add("java");
        commands.add("-cp");
        commands.add(CLASSES_FOLDER +classPath);
        commands.add(questionFolders + "/QuestionTester.java");

        questionPb.command(commands);

        questionPb.redirectOutput(outputFile);
        questionPb.redirectError(errorFile);

        boolean success = false;

        try {
            Process process = questionPb.start();
            // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
            success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);

            if (!success) {
                // Still running after the time limit, so stop it
                process.destroyForcibly();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        questionTester.delete();
        new File(questionTester.getPath().replaceAll("\\.java", "\\.class")).delete();

        return success;
    }

    public static StudentResult gradePaper(Student student, File questionFolder, String testerFolder, int timeLimitSeconds) {
        StudentResult studentResult = new StudentResult(student);
        HashMap<String, ArrayList<String>> questionAndCaseIds = studentResult.getQuestionAndCaseIds();

        boolean success = false;
        try {
            success = runAllTesterFiles(questionAndCaseIds, questionFolder, testerFolder, timeLimitSeconds);
        } catch (Exception e) {}

        ArrayList<File> existingQuestionFiles = new ArrayList<File>();
        for (File questionTesterFolder : questionFolder.listFiles()) {
            for (File questionFile : questionTesterFolder.listFiles()) {
                if (questionFile.getName().endsWith("Tester.java")) {
                    existingQuestionFiles.add(questionFile);
                    existingQuestionFiles.add(new File(questionFile.getPath().replaceAll("\\.java", "\\.class")));
                }
            }
        }

        // if compilation error, individually mark each file
        ArrayList<String> errorText = readErrorFile();
        // Checks every line for the text, instead of a whole line equal to
        // "Error"
        boolean hasError = hasCompileError(errorText);
        for (String line : errorText) {
            if (line.contains("Error")) {
                hasError = true;
            }
        }

        if (!success || hasError) {
            for (File questionTesterFolder : questionFolder.listFiles()) {
                runTesterFiles(questionTesterFolder.listFiles(), questionFolder, timeLimitSeconds, studentResult);
            }
        } else {
            readOutputFile(studentResult);

            // One output file holds every question, so give each question
            // a copy, e.g. Q1/Q1aTester_output.txt
            for (String questionId : questionAndCaseIds.keySet()) {
                File folder = new File(questionFolder, questionId.replaceAll("(?<=Q\\d+).+", ""));
                if (folder.isDirectory()) {
                    saveOutput(folder, questionId + "Tester", questionId);
                }
            }
        }

        for (File existingQuestionFile : existingQuestionFiles) {
            existingQuestionFile.delete();
        }
        
        for (String questionId : questionAndCaseIds.keySet()) {
            new File(questionFolder + "/" + questionId.replaceAll("(?<=Q\\d+).+", "") + "/" + questionId + ".class").delete();
        }

        outputFile.delete();
        errorFile.delete();

        return studentResult;
    }
}
