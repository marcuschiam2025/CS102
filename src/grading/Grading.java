package grading;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import model.StudentResult;
import model.Student;
import model.CaseResultStatus;

// Co-authored with Claude

public class Grading {
    public static File errorFile = new File("error.txt");
    public static File outputFile = new File("output.txt");

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

    private static void readOutputFile(StudentResult studentResult) {
        try (Scanner sc = new Scanner(outputFile);) {
            while (sc.hasNext()) {
                String partDataRaw = sc.nextLine();
                String[] partData = partDataRaw.split("(?<!\\[[^\\]]+),");
                System.out.print(partDataRaw);
                if (partData.length == 6) {
                    studentResult.addCaseResult(partData[0], partData[1], Double.parseDouble(partData[5]), (Boolean.parseBoolean(partData[4]) ? CaseResultStatus.PASSED : (partData[3].equals("(Runtime Error)") ? CaseResultStatus.RUNTIME_ERROR : CaseResultStatus.FAILED)), "");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // This function is co-authored with Claude
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
            commands.add("classes" + File.pathSeparator + file.getParent());
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
        // This line is co-authored with Claude
        compileStudentFiles(testerFiles, timeLimitSeconds);

        for (File testerFile : testerFiles) {
            if (testerFile.getName().endsWith("Tester.java")) {
                ProcessBuilder questionPb = new ProcessBuilder();

                String testerName = testerFile.getName();

                ArrayList<String> commands = new ArrayList<>();
                commands.add("java");
                commands.add("-cp");
                // File.pathSeparator co-authored with Claude
                commands.add("classes" + File.pathSeparator + testerFile.getParent());
                commands.add(testerFile + "");

                questionPb.command(commands);

                questionPb.redirectOutput(outputFile);
                questionPb.redirectError(errorFile);

                boolean success = false;

                try {
                    Process process = questionPb.start();
                    // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
                    success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);

                    // This if block is co-authored with Claude
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

                if (!success) {
                    studentResult.addCaseResult(testerName.replaceAll("Part\\d+Tester.java$", ""), testerName.replaceAll("^Q.+Part(?=\\d+Tester\\.java)|Tester\\.java", ""), 0.0, CaseResultStatus.TIMEOUT, "");
                    continue;
                }

                try (Scanner sc = new Scanner(errorFile);) {
                    if (sc.hasNext()) {
                        studentResult.addCaseResult(testerName.replaceAll("Part\\d+Tester.java$", ""), testerName.replaceAll("^Q.+Part(?=\\d+Tester\\.java)|Tester\\.java", ""), 0.0, CaseResultStatus.COMPILE_ERROR, "");
                        continue;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                readOutputFile(studentResult);
            }
        }
    }

    public static boolean runAllTesterFiles(HashMap<String, ArrayList<String>> questionAndCaseIds, File questionFolders, String testerFolder, int timeLimitSeconds) {
        File[] questionTesterFolders = new File(testerFolder).listFiles();
        Path questionTesterPath = FileSystems.getDefault().getPath(questionFolders.getPath(), "QuestionTester.java");
        File questionTester = null;

        // what happens when a question folder is missing? give 0 points
        try {
            questionTester = Files.createFile(questionTesterPath).toFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                    ps.println("try { new " + question.getKey() + "Part" + caseId + "Tester().grade(); } catch (Exception e) { e.printStackTrace(); }");
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
            // File.pathSeparator co-authored with Claude
            classPath += File.pathSeparator + questionFolder;
        }

        commands.add("javac");
        commands.add("-cp");
        commands.add("classes" + classPath);
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
        commands.add("classes" + classPath);
        commands.add(questionFolders + "/QuestionTester.java");

        questionPb.command(commands);

        questionPb.redirectOutput(outputFile);
        questionPb.redirectError(errorFile);

        boolean success = false;

        try {
            Process process = questionPb.start();
            // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
            success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);

            // This if block is co-authored with Claude
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

        ArrayList<String> questionsToCheck = new ArrayList<String>();

        for (String question : questionAndCaseIds.keySet()) {
            if (!questionsToCheck.contains(question)) {
                questionsToCheck.add(question.replaceAll("(?<=Q\\d+).+", ""));
            }
        }

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
        if (!success || errorText.contains("error: compilation failed") || errorText.contains("Error")) {
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

    public static void main(String[] args) {
        StudentResult.loadStudentResultTemplate("Project sample/StudentResultTemplate.csv");
        Student student = new Student("a", "a", "a", "a", "a");
        // make sure it traverses until it reaches Q1/Q2/Q3 etc.
        File questionFolder = new File("src\\unzippedFolder\\2023-2024-fen.lai.2022.zip\\RenameToYourStudentID");
        String testerFolder = "Project sample/TesterFiles";
        int timeLimitSeconds = 5;
        System.out.println(Grading.gradePaper(student, questionFolder, testerFolder, timeLimitSeconds));
    }
}
