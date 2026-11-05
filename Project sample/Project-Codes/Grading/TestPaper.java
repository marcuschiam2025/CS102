import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

// class RunTesterFile extends Thread {
//     ProcessBuilder questionPb;

//     public RunTesterFile(ProcessBuilder questionPb) {
//         this.questionPb = questionPb;
//     }

//     public synchronized void run() {
//         try {
//             questionPb.start();
//         } catch (IOException e) {
//             e.printStackTrace();
//         } catch (ArrayIndexOutOfBoundsException e) {
//             e.printStackTrace();
//         }
//     }
// }

// class ViewErrorTxt extends Thread {
//     File errorFile;
//     boolean error = false;

//     public ViewErrorTxt(File errorFile) {
//         this.errorFile = errorFile;
//     }

//     public synchronized void run() {
//         try (Scanner sc = new Scanner(errorFile);) {
//             while (sc.hasNext()) {
//                 System.out.println(sc.nextLine());
//                 error = true;
//             }
//         } catch (FileNotFoundException e) {
//             e.printStackTrace();
//         }
//     }

//     public boolean getError() {
//         return error;
//     }
// }

// class ViewOutputTxt extends Thread {
//     File outputFile;
//     double score = 0.0;

//     public ViewOutputTxt(File outputFile) {
//         this.outputFile = outputFile;
//     }

//     public synchronized void run() {
//         try (Scanner sc = new Scanner(outputFile);) {
//             String scorePart = "";

//             while (sc.hasNext()) {
//                 scorePart = sc.nextLine();
//                 System.out.println(scorePart);
//             }

//             score += Double.parseDouble(scorePart);
//         } catch (FileNotFoundException e) {
//             e.printStackTrace();
//         }
//     }

//     public double getScore() {
//         return score;
//     }
// }

public class TestPaper {
    public static String testerFolder = "Project sample/TesterFiles";
    public static int timeLimitSeconds = 5;

    private static String getTesterFolderShort() {
        return testerFolder.replaceAll(".+/", "");
    }

    private static void copyQuestionFile(Path questionCopyPath, String questionName) throws IOException {
        File questionCopyFile = Files.createFile(questionCopyPath).toFile();

        PrintStream ps = new PrintStream(questionCopyFile);
        ps.println("package " + getTesterFolderShort() + "." + questionName + ";");

        File questionFile = new File("Submissions/" + questionName.replaceAll("(?<=Q\\d+).+", "") + "/" + questionName + ".java");
        Scanner sc = new Scanner(questionFile);
        while (sc.hasNext()) {
            ps.println(sc.nextLine());
        }
        
        sc.close();
        ps.close();
    }

    // private static void compileQuestion(File testerFilesFolder, String questionName, File[] testerFiles) {
    //     ProcessBuilder questionPb = new ProcessBuilder();
    //     questionPb.directory(testerFilesFolder);

    //     ArrayList<String> commands = new ArrayList<>();
    //     commands.add("javac");
    //     commands.add("Tester.java");
    //     commands.add(questionName + "/" + questionName + ".java");

    //     for (File testerFile : testerFiles) {
    //         commands.add(questionName + "/" + testerFile.getName());
    //     }

    //     questionPb.command(commands);

    //     try {
    //         questionPb.start();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     } catch (ArrayIndexOutOfBoundsException e) {
    //         e.printStackTrace();
    //     }
    // }

    private static ArrayList<String> readErrorFile(File errorFile) {
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

    private static GradingResult readOutputFile(File outputFile) {
        try (Scanner sc = new Scanner(outputFile);) {
            GradingResult gradingResult = new GradingResult();

            while (sc.hasNext()) {
                String partDataRaw = sc.nextLine();
                String[] partData = partDataRaw.split("(?<!\\[[^\\]]+),");
                gradingResult.addPartResult(partData[0], partData[1], partData[2], partData[3], Boolean.parseBoolean(partData[4]), Double.parseDouble(partData[5]));
            }

            return gradingResult;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    // private static double runTesterFiles(String questionName, File[] testerFiles) {
    //     double score = 0.0;

    //     File outputFile = new File("output.txt");
    //     File errorFile = new File("error.txt");

    //     for (File testerFile : testerFiles) {
    //         if (testerFile.getName().endsWith("Tester.java")) {
    //             ProcessBuilder questionPb = new ProcessBuilder();

    //             ArrayList<String> commands = new ArrayList<>();
    //             String testerName = testerFile.getName();
    //             commands.add("java");
    //             commands.add("TesterFiles/" + questionName + "/" + testerName);

    //             questionPb.command(commands);

    //             questionPb.redirectOutput(outputFile);
    //             questionPb.redirectError(errorFile);

    //             try {
    //                 Process process = questionPb.start();
    //                 process.waitFor();
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //             } catch (ArrayIndexOutOfBoundsException e) {
    //                 e.printStackTrace();
    //             } catch (InterruptedException e) {
    //                 e.printStackTrace();
    //             }

    //             try (Scanner sc = new Scanner(errorFile);) {
    //                 while (sc.hasNext()) {
    //                     System.out.println(sc.nextLine());
    //                 }
    //             } catch (FileNotFoundException e) {
    //                 e.printStackTrace();
    //             }

    //             try (Scanner sc = new Scanner(outputFile);) {
    //                 String scorePart = "";

    //                 while (sc.hasNext()) {
    //                     scorePart = sc.nextLine();
    //                     System.out.println(scorePart);
    //                 }

    //                 score += Double.parseDouble(scorePart);
    //             } catch (FileNotFoundException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }

    //     return score;
    // }

    private static GradingResult runTesterFiles(File[] testerFiles) {
        GradingResult gradingResult = new GradingResult();

        File outputFile = new File("output.txt");
        File errorFile = new File("error.txt");

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
                    gradingResult.addPartResult(testerName.replaceAll("(?!Q)[A-Z].+Tester.java$", ""), testerName.replaceAll("^Q.+(?=[A-Z].+Tester.java)|Tester.java", ""), "-", "(Took too long or infinite loop)", false, 0.0);
                    continue;
                }

                try (Scanner sc = new Scanner(errorFile);) {
                    if (sc.hasNext()) {
                        System.out.println(sc.nextLine());
                        
                        gradingResult.addPartResult(testerName.replaceAll("(?!Q)[A-Z].+Tester.java$", ""), testerName.replaceAll("^Q.+(?=[A-Z].+Tester.java)|Tester.java", ""), "-", "(Compilation Error)", false, 0.0);
                        continue;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                gradingResult.addQuestionResult(readOutputFile(outputFile));
            }
        }
        System.out.println(gradingResult);

        return gradingResult;
    }

    public static GradingResult testPaper() {
        GradingResult gradingResult = new GradingResult();

        File testerFilesFolder = new File(testerFolder);
        File[] questionFolders = testerFilesFolder.listFiles();
        List<File> existingQuestionFiles = new ArrayList<>();
        Path questionTesterPath = FileSystems.getDefault().getPath(testerFolder, "QuestionTester.java");
        File questionTester = null;
        List<File[]> questionTesters = new ArrayList<>();

        try {
            questionTester = Files.createFile(questionTesterPath).toFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintStream ps = new PrintStream(questionTester);) {
            List<File[]> questionTesterFolders = new ArrayList<>();
            ps.println("package " + getTesterFolderShort() + ";");

            for (File questionFolder : questionFolders) {
                String questionName = questionFolder.getName();

                if (questionName.matches("Q\\d+.*")) {
                    File[] testerFiles = questionFolder.listFiles();
                    questionTesters.add(testerFiles);

                    // Path questionFile = FileSystems.getDefault().getPath("Submissions", questionName.replaceAll("(?<=Q\\d+).+", ""), questionName + ".java");
                    Path questionCopyPath = FileSystems.getDefault().getPath(testerFolder, questionName, questionName + ".java");
                    
                    try {
                        copyQuestionFile(questionCopyPath, questionName);
                        existingQuestionFiles.add(questionCopyPath.toFile());
                        ps.println("import " + getTesterFolderShort() + "." + questionName + ".*;");
                        questionTesterFolders.add(testerFiles);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("No submission question file exists, skipping");
                        continue;
                    }

                    // compileQuestion(testerFilesFolder, questionName, testerFiles);

                    // really slow, run all tester files at once
                    // score += runTesterFiles(questionName, testerFiles);
                    // System.out.println(score);

                    // questionCopyPath.toFile().delete();

                    // List<String> commands = new ArrayList<>();

                    // for (File testerFile : testerFiles) {
                    //     String fileName = testerFile.getName();

                    //     if (fileName.matches("^[^Q].*\\.java$")) {
                    //         System.out.println(testerFile);
                    //         commands.add(0, "javac " + fileName);
                    //     }
                    // }

                    // System.out.println(commands);
                    // questionPb.command(commands);

                    // try {
                    //     questionPb.start();
                    // } catch (IOException e) {
                    //     e.printStackTrace();
                    // } catch (ArrayIndexOutOfBoundsException e) {
                    //     System.out.println("No non-question java files to compile, skipping");
                    // }
                }
            }

            ps.println("public class QuestionTester { public static void main(String[] args) { double score = 0.0;");

            for (File[] questionTesterFolder : questionTesterFolders) {
                for (File questionTesterFile : questionTesterFolder) {
                    String questionTesterName = questionTesterFile.getName();
                    ps.println("try { score += new " + questionTesterName.substring(0, questionTesterName.length() - 5) + "().grade(); } catch (Exception e) { e.printStackTrace(); }");
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

        File errorFile = new File("error.txt");
        File outputFile = new File("output.txt");

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

        // if compilation error, individually mark each file
        ArrayList<String> errorText = readErrorFile(errorFile);
        if (!success || errorText.contains("error: compilation failed")) {
            for (File[] questionTesterFolder : questionTesters) {
                gradingResult.addQuestionResult(runTesterFiles(questionTesterFolder));
            }
        } else {
            gradingResult.addQuestionResult(readOutputFile(outputFile));
        }

        for (File existingQuestionFile : existingQuestionFiles) {
            existingQuestionFile.delete();
        }

        return gradingResult;

        // Tester[] testers = {new Q1aTester(), new Q1bTester()};
        // double points = 0;

        // for (Tester tester : testers) {
        //     points += tester.grade();
        // }

        // return points;
    }

    public static void main(String[] args) {
        System.out.println(TestPaper.testPaper());
    }
}
