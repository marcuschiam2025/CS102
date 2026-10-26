package fixer;

import java.io.File;

public class UsernameResolver {
    private static final String ZIP_EXTENSION = ".zip";
    private static final String SEPARATOR = "-";
    private static final int NOT_FOUND = -1;

    // e.g. 2023-2024-fen.lai.2022.zip -> fen.lai.2022,
    // or null if the name has no username part
    public static String getUsername(String zipFilePath) {
        String zipName = new File(zipFilePath).getName();

        if (zipName.toLowerCase().endsWith(ZIP_EXTENSION)) {
            int nameLength = zipName.length() - ZIP_EXTENSION.length();
            zipName = zipName.substring(0, nameLength);
        }

        // The username comes after the second "-"
        int firstDash = zipName.indexOf(SEPARATOR);
        if (firstDash == NOT_FOUND) {
            return null;
        }

        int secondDash = zipName.indexOf(SEPARATOR, firstDash + 1);
        if (secondDash == NOT_FOUND) {
            return null;
        }

        String username = zipName.substring(secondDash + 1).trim();
        if (username.isEmpty()) {
            return null;
        }

        return username;
    }

    // e.g. fen.lai.2022 -> Fen Lai (the last part, the year, is left out)
    public static String getStudentName(String username) {
        // "\\." means split on a real dot
        String[] nameParts = username.split("\\.");
        String studentName = "";

        for (int i = 0; i < nameParts.length - 1; i++) {
            if (studentName.length() > 0) {
                studentName += " ";
            }

            String word = nameParts[i];

            if (!word.isEmpty()) {
                studentName += word.substring(0, 1).toUpperCase()
                        + word.substring(1).toLowerCase();
            }
        }

        return studentName;
    }
}
