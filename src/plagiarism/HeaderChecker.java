package plagiarism;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fixer.UsernameResolver;

// Plagiarism check 2: another student's username inside a file.
//
// Reads the original zips, because the fixer rewrites the
// "Email ID:" line with the zip owner's username.
public class HeaderChecker {

    // e.g. [chee.teo.2022, david.2024, fen.lai.2022, ...]
    private static ArrayList<String> getUsernames(File[] zipFiles) {
        ArrayList<String> usernames = new ArrayList<>();

        for (File zip : zipFiles) {

            String username = UsernameResolver.getUsername(zip.getPath());
            if (username != null) {
                usernames.add(username);
            }

        }

        return usernames;
    }

    // Also used by SimilarCodeChecker. Each item is { entry name, text }.
    static ArrayList<String[]> readJavaFiles(File zipFile) throws IOException {
        ArrayList<String[]> javaFiles = new ArrayList<>();
        try (ZipFile zip = new ZipFile(zipFile)) { // open the zip (auto-closed)
            for (ZipEntry entry : Collections.list(zip.entries())) {
                String name = entry.getName(); // FILE NAME - "chee.teo.2022/Q2/Q2b.java"

                if (entry.isDirectory() || !name.endsWith(".java")
                        || name.contains("__MACOSX")) {
                    continue;
                    // ... skip folders / non-.java / __MACOSX, then read it
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    String text = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
                    // read that one file's bytes, then turn them into text
                    javaFiles.add(new String[] { name, text });
                }
            }

        }
        return javaFiles;
    }

    public static ArrayList<ForeignHeader> findForeignHeaders(File[] zipFiles) {
        ArrayList<ForeignHeader> found = new ArrayList<>();
        ArrayList<String> usernames = getUsernames(zipFiles);

        for (File zip : zipFiles) {
            String owner = UsernameResolver.getUsername(zip.getPath());

            if (owner == null) {
                continue;
            }
            ArrayList<String[]> javaFiles;
            try {
                javaFiles = readJavaFiles(zip);
            } catch (IOException exception) {
                continue;
            }
            for (String[] javaFile : javaFiles) {
                String fileName = javaFile[0];
                String text = javaFile[1];

                for (String line : text.split("\n")) {
                    for (String other : usernames) {
                        if (other.equals(owner)) {
                            continue;
                        }
                        if (line.toLowerCase().contains(other.toLowerCase())) {
                            ForeignHeader fh = new ForeignHeader(owner, other, fileName, line.trim());
                            found.add(fh);
                        }
                    }
                }
            }
        }

        return found;
    }

}
