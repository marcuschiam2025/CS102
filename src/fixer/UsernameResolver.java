package fixer;

import java.io.File;
public class UsernameResolver {

    // e.g. 2023-2024-fen.lai.2022.zip -> fen.lai.2022, or null if the name has no username part
    public static String getUsername(String zipFilePath) {
        String zipName = new File(zipFilePath).getName();

        if (zipName.toLowerCase().endsWith(".zip")) {
            zipName = zipName.substring(0, zipName.length() - 4);
        }

        // The username comes after the second "-"
        int firstDash = zipName.indexOf("-");
        if (firstDash == -1) {
            return null;
        }

        int secondDash = zipName.indexOf("-", firstDash + 1);
        if (secondDash == -1) {
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
        String[] nameParts = username.split("\\."); // "\\." means split on a real dot
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
