package grader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import model.StudentResult;

public class Grader {

    private static final String[][] QUESTIONS = {
            // Part | Question | Filename | score
            { "Q1a", "Q1", "Q1aTester", "3" },
            { "Q1b", "Q1", "Q1bTester", "3" },
            { "Q2a", "Q2", "Q2aTester", "5" },
            { "Q2b", "Q2", "Q2bTester", "5" },
            { "Q3", "Q3", "Q3Tester", "4" },
    };

    public static StudentResult gradeSubmission(File usernameFolder, File testerFolder, int timeLimit) {
        for (String[] question : QUESTIONS) {

            String testerName = question[2]; // "Q1aTester"
            File questionFolder = new File(usernameFolder, question[1]); // ...\ping.lee.2023\Q1

            System.out.println("Copying " + testerName + " into " + questionFolder);
            try {
                copyTester(testerFolder, questionFolder, testerName);
                File outputFile = new File(questionFolder, testerName + "_compile.txt");
                boolean compiled = compile(questionFolder, testerName, outputFile);
                System.out.println(testerName + " compiled: " + compiled);
                boolean finished = runTester(questionFolder, testerName, outputFile, timeLimit);
                System.out.println(testerName + "Finished in time: " + finished);
            } catch (IOException | InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return null;
    }

    private static void copyTester(File testerFolder, File questionFolder, String testerName) throws IOException {
        File sourceFile = new File(testerFolder, testerName + ".java");
        File targetFile = new File(questionFolder, testerName + ".java");
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean compile(File questionFolder, String testerName, File outputFile)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac", testerName + ".java"); // prep to run this code
        pb.directory(questionFolder); // which directory to run it
        pb.redirectErrorStream(true); // put error and normal output tgt
        pb.redirectOutput(outputFile); // write all output to this file

        Process process = pb.start(); // run the javac code
        int exitCode = process.waitFor(); // wait till code finish running

        return exitCode == 0; // if 0 means compiled. else its an error
    }

    private static boolean runTester(File questionFolder, String testerName, File outputFile, int timeLimit)throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", testerName);
        pb.directory(questionFolder);
        pb.redirectErrorStream(true); // put error and normal output tgt
        pb.redirectOutput(outputFile); // write all output to this file
        Process process = pb.start(); // run the java code
        boolean finished = process.waitFor(timeLimit, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly(); // still running after the time limit, so stop it
        }
        return finished;
    }
}
