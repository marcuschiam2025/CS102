package FileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import fileClasses.FilesClass;
import fileClasses.Folder;

public class fileHandlings {
    public static ArrayList<String> readFiles(String delimeterPattern, String fileName) {
        File f = new File(fileName);
        ArrayList<String> values = new ArrayList<>();
        try (Scanner sc = new Scanner(f);) {
            sc.useDelimiter(delimeterPattern);
            while (sc.hasNext()) {
                String s = sc.next();
                values.add(s);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file is not found");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        Path targetDir = Paths.get(destDirectory).toAbsolutePath().normalize();

        // Create target directory if it doesn't exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (InputStream fis = Files.newInputStream(Paths.get(zipFilePath));
                ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Resolve the target path for the current entry and normalize it
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                // --- ZIP SLIP VULNERABILITY PROTECTION ---
                // Ensure the entry path stays within the intended target directory
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Malicious ZIP entry detected: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    // Create subdirectories if they don't exist
                    Files.createDirectories(newPath);
                } else {
                    // Create any missing parent directories for the file
                    Path parent = newPath.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }

                    // Copy the input stream directly to the target file location
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    public static ArrayList<FilesClass> viewFilesAndFolders(String directoryPath) throws IOException {
        // Path of the specific directory

        // Using File class create an object for specific directory
        File directory = new File(directoryPath);

        // Using listFiles method we get all the files of a directory
        // return type of listFiles is array
        File[] files = directory.listFiles();
        ArrayList<FilesClass> newArray = new ArrayList<>();
        // Print name of the all files present in that path
        if (files != null) {
            for (File file : files) {
                loopThrough(file, newArray, file.getName());
            }
        }
        return newArray;
    }

    public static void loopThrough(File fileParams, ArrayList<FilesClass> fileClassParams, String parentName) {
        File[] filesInside = fileParams.listFiles();
        if (filesInside != null) {
            Folder newFolder = new Folder(fileParams.getName(), parentName);
            ArrayList<FilesClass> newFile = new ArrayList<>();
            for (File filesInsideFolder : filesInside) {
                loopThrough(filesInsideFolder, newFile, fileParams.getName());
            }
            newFolder.setFilesInsideFolder(newFile);
            fileClassParams.add(newFolder);
        } else {
            fileClassParams.add(new FilesClass(fileParams.getName(), parentName));
        }
    }

    public static ArrayList<String> repairSubmission(String zipFilePath, String extractedDirectory)
            throws IOException {

        ArrayList<String> messages = new ArrayList<>();

        String zipName = new File(zipFilePath).getName();
        zipName = zipName.replaceFirst("(?i)\\.zip$", "");

        String[] parts = zipName.split("-", 3);

        if (parts.length != 3 || parts[2].trim().isEmpty()) {
            messages.add("Cannot determine username from ZIP filename.");
            return messages;
        }

        String username = parts[2].trim();

        // Derive student name from email ID, excluding the year.
        String[] nameParts = username.split("\\.");
        StringBuilder nameBuilder = new StringBuilder();

        for (int i = 0; i < nameParts.length - 1; i++) {
            if (nameBuilder.length() > 0) {
                nameBuilder.append(" ");
            }

            String word = nameParts[i];

            if (!word.isEmpty()) {
                nameBuilder.append(
                        word.substring(0, 1).toUpperCase()
                                + word.substring(1).toLowerCase());
            }
        }

        String studentName = nameBuilder.toString();

        if (studentName.isEmpty()) {
            messages.add("Cannot derive student name from ZIP filename.");
            return messages;
        }

        Path root = Paths.get(extractedDirectory).toAbsolutePath().normalize();

        Files.createDirectories(root);

        Path usernameFolder = root.resolve(username);

        // Find any top-level directory that is not named after the student ID.
        try (Stream<Path> paths = Files.list(root)) {
            ArrayList<Path> folders = new ArrayList<>();

            paths.filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString()
                            .equals(username))
                    .forEach(folders::add);

            for (Path folder : folders) {
                if (Files.exists(usernameFolder)) {
                    messages.add("Cannot rename folder "
                            + folder.getFileName()
                            + ": destination folder " + username
                            + " already exists.");
                    continue;
                }

                Files.move(folder, usernameFolder);
                messages.add("Renamed folder "
                        + folder.getFileName()
                        + " to " + username + ".");

                break;
            }
        }

        // 2. Move question folders into the username folder.
        String[] questions = { "Q1", "Q2", "Q3" };

        boolean questionsAtRoot = false;

        for (String question : questions) {
            if (Files.isDirectory(root.resolve(question))) {
                questionsAtRoot = true;
                break;
            }
        }

        if (questionsAtRoot) {
            if (Files.exists(usernameFolder)
                    && !Files.isDirectory(usernameFolder)) {
                messages.add("Cannot create username folder: a file has that name.");
                return messages;
            }

            Files.createDirectories(usernameFolder);

            for (String question : questions) {
                Path source = root.resolve(question);
                Path destination = usernameFolder.resolve(question);

                if (Files.isDirectory(source)) {
                    if (Files.exists(destination)) {
                        messages.add("Cannot move " + question
                                + ": destination already exists.");
                    } else {
                        Files.move(source, destination);
                        messages.add("Moved " + question
                                + " into the username folder.");
                    }
                }
            }
        }

        if (!Files.isDirectory(usernameFolder)) {
            messages.add("Username folder not found. Please check the submission.");
            return messages;
        }
        fixMisplacedFiles(usernameFolder, messages);

        // Use your existing folder traversal function to inspect the result.
        ArrayList<FilesClass> folderContents = viewFilesAndFolders(usernameFolder.toString());

        // 3. Check Java comments inside Q1, Q2 and Q3.
        for (String question : questions) {
            Path questionFolder = usernameFolder.resolve(question);

            if (!Files.isDirectory(questionFolder)) {
                continue;
            }

            try (Stream<Path> paths = Files.walk(questionFolder)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            try {
                                fixJavaHeaders(
                                        usernameFolder,
                                        username,
                                        studentName,
                                        messages);

                            } catch (IOException e) {
                                messages.add("Cannot process " + p.getFileName()
                                        + ": " + e.getMessage());
                            }
                        });
            }
        }

        if (messages.isEmpty()) {
            messages.add("No targeted errors found.");
        }

        return messages;
    }

    public static void fixJavaHeaders(
            Path submissionFolder,
            String username,
            String studentName,
            ArrayList<String> messages) throws IOException {

        try (Stream<Path> paths = Files.walk(submissionFolder)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String result = repairJavaHeader(
                                    path, username, studentName);

                            if (result != null) {
                                messages.add(path.getFileName() + ": " + result);
                            }
                        } catch (IOException e) {
                            messages.add("Failed to repair " + path
                                    + ": " + e.getMessage());
                        }
                    });
        }
    }

    public static String repairJavaHeader(
            Path javaFile,
            String username,
            String studentName) throws IOException {

        String content = Files.readString(javaFile);
        String header = "/*\n"
                + " * Name: " + studentName + "\n"
                + " * Email ID: " + username + "\n"
                + " */";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\\A(\\s*/\\*.*?\\*/)",
                java.util.regex.Pattern.DOTALL);

        java.util.regex.Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
            Files.writeString(javaFile, header + "\n\n" + content);
            return "Added missing student header.";
        }

        String comment = matcher.group(1);
        String updatedComment = comment;
        boolean changed = false;

        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
                "(?im)^\\s*\\*?\\s*Name\\s*:.*$");

        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(
                "(?im)^\\s*\\*?\\s*Email\\s+ID\\s*:.*$");

        if (namePattern.matcher(updatedComment).find()) {
            updatedComment = namePattern.matcher(updatedComment)
                    .replaceFirst(
                            java.util.regex.Matcher.quoteReplacement(
                                    " * Name: " + studentName));
        } else {
            int closingPosition = updatedComment.lastIndexOf("*/");
            updatedComment = updatedComment.substring(0, closingPosition)
                    + " * Name: " + studentName + "\n "
                    + updatedComment.substring(closingPosition);
            changed = true;
        }

        if (emailPattern.matcher(updatedComment).find()) {
            updatedComment = emailPattern.matcher(updatedComment)
                    .replaceFirst(
                            java.util.regex.Matcher.quoteReplacement(
                                    " * Email ID: " + username));
        } else {
            int closingPosition = updatedComment.lastIndexOf("*/");
            updatedComment = updatedComment.substring(0, closingPosition)
                    + " * Email ID: " + username + "\n "
                    + updatedComment.substring(closingPosition);
            changed = true;
        }

        if (!updatedComment.equals(comment)) {
            changed = true;
        }

        if (changed) {
            content = updatedComment
                    + content.substring(comment.length());
            Files.writeString(javaFile, content);
            return "Fixed student name/email header.";
        }

        return null;
    }

    private static void fixMisplacedFiles(
            Path usernameFolder, ArrayList<String> messages)
            throws IOException {

        Path q1Folder = usernameFolder.resolve("Q1");
        Files.createDirectories(q1Folder);

        String[] q1Items = { "Q1a", "Q1b", "Q1a.java", "Q1b.java" };

        for (String itemName : q1Items) {
            Path source = usernameFolder.resolve(itemName);
            Path destination = q1Folder.resolve(itemName);

            if (Files.exists(source)) {
                if (Files.exists(destination)) {
                    messages.add("Cannot move " + itemName
                            + ": it already exists in Q1.");
                } else {
                    Files.move(source, destination);
                    messages.add("Moved " + itemName + " into Q1.");
                }
            }
        }
        String[][] expectedFiles = {
                { "Q2", "DataException.class", "persons.txt", "students.txt", "personstester.txt",
                        "studentstester.txt" },
                { "Q3", "Circle.class", "Rectangle.class", "Shape.class",
                        "ShapeComparator.java" }
        };

        for (String[] group : expectedFiles) {
            Path correctFolder = usernameFolder.resolve(group[0]);

            for (int i = 1; i < group.length; i++) {
                String fileName = group[i];
                Path destination = correctFolder.resolve(fileName);

                // Create the expected question folder if necessary.
                Files.createDirectories(correctFolder);

                // Search within the username folder for this exact filename.
                try (Stream<Path> paths = Files.walk(usernameFolder)) {
                    Path source = paths
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString()
                                    .equalsIgnoreCase(fileName))
                            .filter(p -> !p.normalize()
                                    .equals(destination.normalize()))
                            .findFirst()
                            .orElse(null);

                    if (source == null) {
                        continue;
                    }

                    if (Files.exists(destination)) {
                        messages.add("Cannot move " + fileName
                                + ": destination already exists.");
                        continue;
                    }

                    Files.move(source, destination);

                    messages.add("Moved " + fileName + " to "
                            + group[0] + ".");
                }
            }
        }
    }
}
