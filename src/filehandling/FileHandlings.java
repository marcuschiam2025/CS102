package file;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import exceptions.InvalidZipException;
import fileClasses.FilesClass;
import fileClasses.Folder;

public class FileHandlings {
    // An empty ZIP file is only an "end of archive" record, which starts
    // with these bytes ("PK" 5 6)
    private static final byte[] EMPTY_ZIP_SIGNATURE = { 'P', 'K', 5, 6 };

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

        String zipName = Paths.get(zipFilePath).getFileName().toString();
        int entryCount = 0;

        try (InputStream fis = Files.newInputStream(Paths.get(zipFilePath));
                ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
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
        } catch (ZipException | EOFException exception) {
            // The file starts like a ZIP but is broken (e.g. cut off)
            throw new InvalidZipException(zipName + " is corrupt: "
                    + exception.getMessage());
        }

        // ZipInputStream finds no entries both in an empty ZIP and in a
        // file that is not a ZIP at all, so look at how the file starts
        if (entryCount == 0) {
            if (startsWith(zipFilePath, EMPTY_ZIP_SIGNATURE)) {
                throw new InvalidZipException(zipName + " is an empty ZIP "
                        + "file.");
            }

            throw new InvalidZipException(zipName + " is not a valid ZIP "
                    + "file (it may be corrupt).");
        }
    }

    // True if the file's first bytes are the given bytes
    private static boolean startsWith(String filePath, byte[] signature)
            throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get(filePath))) {
            byte[] start = in.readNBytes(signature.length);
            return Arrays.equals(start, signature);
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
