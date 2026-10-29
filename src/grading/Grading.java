package grading;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import model.StudentResult;
import model.Student;
import model.CaseResultStatus;

public class Grading {
    public static File errorFile = new File("error.txt");
    public static File outputFile = new File("output.txt");

    private static void copyQuestionFile(Path questionCopyPath, String questionName) throws IOException {
        File questionCopyFile = Files.createFile(questionCopyPath).toFile();

        PrintStream ps = new PrintStream(questionCopyFile);
        ps.println("package " + questionName + ";");

        File questionFile = new File("Submissions/" + questionName.replaceAll("(?<=Q\\d+).+", "") + "/" + questionName + ".java");
        Scanner sc = new Scanner(questionFile);
        while (sc.hasNext()) {
            ps.println(sc.nextLine());
        }
        
        sc.close();
        ps.close();
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
                studentResult.addCaseResult(partData[0], partData[1], Double.parseDouble(partData[5]), (Boolean.parseBoolean(partData[4]) ? CaseResultStatus.PASSED : (partData[3].equals("(Runtime Error)") ? CaseResultStatus.RUNTIME_ERROR : CaseResultStatus.FAILED)), "");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void runTesterFiles(File[] testerFiles, String testerFolder, int timeLimitSeconds, StudentResult studentResult) {
        for (File testerFile : testerFiles) {
            if (testerFile.getName().endsWith("Tester.java")) {
                ProcessBuilder questionPb = new ProcessBuilder();

                ArrayList<String> commands = new ArrayList<>();
                String testerName = testerFile.getName();
                commands.add("java");
                // commands.add("TesterFiles/" + testerFile.getParent().replaceAll("TesterFiles\\\\", "") + "/" + testerName);
                commands.add(testerFolder + "/" + testerFile.getParent().replaceAll(testerFolder + "\\\\", "") + "/" + testerName);

                questionPb.command(commands);

                questionPb.redirectOutput(outputFile);
                questionPb.redirectError(errorFile);

                boolean success = false;

                try {
                    Process process = questionPb.start();
                    // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
                    success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    studentResult.addCaseResult(testerName.replaceAll("(?!Q)[A-Z].+Tester.java$", ""), testerName.replaceAll("^Q.+(?=[A-Z]Part.+Tester.java)|Tester.java", ""), 0.0, CaseResultStatus.TIMEOUT, "");
                    continue;
                }

                try (Scanner sc = new Scanner(errorFile);) {
                    if (sc.hasNext()) {
                        System.out.println(sc.nextLine());
                        
                        studentResult.addCaseResult(testerName.replaceAll("(?!Q)[A-Z].+Tester.java$", ""), testerName.replaceAll("^Q.+(?=[A-Z]Part.+Tester.java)|Tester.java", ""), 0.0, CaseResultStatus.COMPILE_ERROR, "");
                        continue;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                readOutputFile(studentResult);
            }
        }
    }

    public static boolean runAllTesterFiles(HashMap<String, ArrayList<String>> questionAndCaseIds, File[] questionFolder, String testerFolder, int timeLimitSeconds) {
        File[] questionTesterFolders = new File(testerFolder).listFiles();
        Path questionTesterPath = FileSystems.getDefault().getPath(testerFolder, "QuestionTester.java");
        File questionTester = null;

        try {
            questionTester = Files.createFile(questionTesterPath).toFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintStream ps = new PrintStream(questionTester);) {
            ps.println("package " + testerFolder.replaceAll(".+/", "") + ";");

            for (File questionTesterFolder : questionTesterFolders) {
                String questionId = questionTesterFolder.getName();

                if (questionAndCaseIds.containsKey(questionId)) {
                    Path questionCopyPath = FileSystems.getDefault().getPath(testerFolder, questionId, questionId + ".java");
                    
                    try {
                        copyQuestionFile(questionCopyPath, questionId);
                        ps.println("import " + questionId + ".*;");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("No submission question file exists, skipping");
                        continue;
                    }
                }
            }

            ps.println("public class QuestionTester { public static void main(String[] args) { double score = 0.0;");

            for (Map.Entry<String, ArrayList<String>> question : questionAndCaseIds.entrySet()) {
                for (String caseId : question.getValue()) {
                    ps.println("try { score += new " + question.getKey() + "Case" + caseId + "Tester().grade(); } catch (Exception e) { e.printStackTrace(); }");
                }
            }

            // ps.println("System.out.print(score); } }");
            ps.println("} }");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ProcessBuilder questionPb = new ProcessBuilder();

        ArrayList<String> commands = new ArrayList<>();
        commands.add("java");
        commands.add(testerFolder + "/QuestionTester.java");

        questionPb.command(commands);

        questionPb.redirectOutput(outputFile);
        questionPb.redirectError(errorFile);

        boolean success = false;

        try {
            Process process = questionPb.start();
            // waitFor() idea modified from stack overflow (https://stackoverflow.com/questions/13938695/waiting-for-process-to-end-in-java)
            success = process.waitFor(timeLimitSeconds, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        questionTester.delete();

        return success;
    }

    public static StudentResult gradePaper(Student student, File[] questionFolder, String testerFolder, int timeLimitSeconds) {
        StudentResult studentResult = new StudentResult(student);
        HashMap<String, ArrayList<String>> questionAndCaseIds = studentResult.getQuestionAndCaseIds();

        File testerFolderFile = new File(testerFolder);
        File[] questionTesterFolders = testerFolderFile.listFiles();
        ArrayList<File> existingQuestionFiles = new ArrayList<File>();
        
        boolean success = runAllTesterFiles(questionAndCaseIds, questionFolder, testerFolder, timeLimitSeconds);

        // if compilation error, individually mark each file
        ArrayList<String> errorText = readErrorFile();
        if (!success || errorText.contains("error: compilation failed")) {
            for (File questionTesterFolder : questionTesterFolders) {
                runTesterFiles(questionTesterFolder.listFiles(), testerFolder, timeLimitSeconds, studentResult);
            }
        } else {
            readOutputFile(studentResult);
        }

        for (File existingQuestionFile : existingQuestionFiles) {
            existingQuestionFile.delete();
        }

        outputFile.delete();
        errorFile.delete();

        return studentResult;
    }

    public static void main(String[] args) {
        // System.out.println(Grading.gradePaper());
    }
}
