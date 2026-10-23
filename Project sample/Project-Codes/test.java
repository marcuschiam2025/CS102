
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import FileHandling.fileHandlings;

public class test {

    public static void main(String[] args) {
        File submissionsFolder = new File("../student-submission");

        if (!submissionsFolder.isDirectory()) {
            System.out.println("Folder student-submission not found.");
            return;
        }

        File[] zipFiles = submissionsFolder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".zip"));

        if (zipFiles == null || zipFiles.length == 0) {
            System.out.println("No ZIP files found in student-submission.");
            return;
        }

        for (File zipFile : zipFiles) {
            System.out.println("\n================================");
            System.out.println("Processing: " + zipFile.getName());
            System.out.println("================================");

            String zipName = zipFile.getName()
                    .replaceFirst("(?i)\\.zip$", "");

            File extractFolder = new File(
                    submissionsFolder,
                    zipName + "_extracted");

            try {
                fileHandlings.unzip(
                        zipFile.getAbsolutePath(),
                        extractFolder.getAbsolutePath());

                System.out.println("ZIP extracted successfully.");

                ArrayList<String> results =
                        fileHandlings.repairSubmission(
                                zipFile.getAbsolutePath(),
                                extractFolder.getAbsolutePath());

                System.out.println("\nSubmission repair results:");

                for (String result : results) {
                    System.out.println("- " + result);
                }

            } catch (IOException e) {
                System.out.println(
                        "Failed to process " + zipFile.getName()
                        + ": " + e.getMessage());
            }
        }

        System.out.println("\nAll submissions processed.");
    }
}