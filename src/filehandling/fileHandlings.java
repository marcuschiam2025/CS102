package filehandling;

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

    // Deletes a folder and everything inside it, if it exists
    public static void deleteFolder(File folder) {
        File[] items = folder.listFiles();

        // A folder must be empty before it can be deleted,
        // so delete what is inside first
        if (items != null) {
            for (File item : items) {
                deleteFolder(item);
            }
        }

        folder.delete();
    }
}
