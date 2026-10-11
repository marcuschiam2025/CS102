package fileHandlings;

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
        } catch (Exception e){
            e.printStackTrace();
        }
        return  values;
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

}
