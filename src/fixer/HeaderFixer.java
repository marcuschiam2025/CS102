package fixer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderFixer {
    // A line like " * Name: Ping Lee": optional spaces, optional *,
    // then "Name" and ":" (any case)
    private static final Pattern NAME_LINE =
            Pattern.compile("(?im)^\\s*\\*?\\s*Name\\s*:.*$");

    // A line like " * Email ID: ping.lee.2023", with at least one space
    // between "Email" and "ID"
    private static final Pattern EMAIL_LINE =
            Pattern.compile("(?im)^\\s*\\*?\\s*Email\\s+ID\\s*:.*$");

    private static final String COMMENT_START = "/*";
    private static final String COMMENT_END = "*/";
    private static final int NOT_FOUND = -1;

    public static void fix(File submissionFolder, String username,
            String studentName, ArrayList<String> messages) {
        ArrayList<File> javaFiles = new ArrayList<>();
        findJavaFiles(submissionFolder, javaFiles);

        for (File javaFile : javaFiles) {
            try {
                String result = repairJavaHeader(javaFile, username,
                        studentName);

                if (result != null) {
                    messages.add(javaFile.getName() + ": " + result);
                }
            } catch (FileNotFoundException exception) {
                messages.add("Failed to repair " + javaFile + ": "
                        + exception.getMessage());
            }
        }
    }

    // Adds every .java file in the folder and its subfolders to the list
    private static void findJavaFiles(File folder,
            ArrayList<File> javaFiles) {
        File[] items = folder.listFiles();

        if (items == null) {
            return;
        }

        for (File item : items) {
            String name = item.getName().toLowerCase();

            if (item.isFile() && name.endsWith(".java")) {
                javaFiles.add(item);
            } else if (item.isDirectory()) {
                findJavaFiles(item, javaFiles);
            }
        }
    }

    // Returns a message describing the fix, or null if nothing was changed
    public static String repairJavaHeader(File javaFile, String username,
            String studentName) throws FileNotFoundException {
        String content = readWholeFile(javaFile);
        String nameLine = " * Name: " + studentName;
        String emailLine = " * Email ID: " + username;

        // Skip blank space at the start, then expect the header comment
        int start = 0;
        while (start < content.length()
                && Character.isWhitespace(content.charAt(start))) {
            start++;
        }

        int end = NOT_FOUND;
        if (content.startsWith(COMMENT_START, start)) {
            end = content.indexOf(COMMENT_END,
                    start + COMMENT_START.length());
        }

        if (end == NOT_FOUND) {
            String header = COMMENT_START + "\n"
                    + nameLine + "\n"
                    + emailLine + "\n"
                    + " " + COMMENT_END;
            writeWholeFile(javaFile, header + "\n\n" + content);
            return "Added missing student header.";
        }

        String comment = content.substring(0, end + COMMENT_END.length());
        String updatedComment = comment;
        boolean changed = false;

        if (NAME_LINE.matcher(updatedComment).find()) {
            updatedComment = NAME_LINE.matcher(updatedComment)
                    .replaceFirst(Matcher.quoteReplacement(nameLine));
        } else {
            updatedComment = insertBeforeClosing(updatedComment, nameLine);
            changed = true;
        }

        if (EMAIL_LINE.matcher(updatedComment).find()) {
            updatedComment = EMAIL_LINE.matcher(updatedComment)
                    .replaceFirst(Matcher.quoteReplacement(emailLine));
        } else {
            updatedComment = insertBeforeClosing(updatedComment, emailLine);
            changed = true;
        }

        if (!updatedComment.equals(comment)) {
            changed = true;
        }

        if (changed) {
            String rest = content.substring(comment.length());
            writeWholeFile(javaFile, updatedComment + rest);
            return "Fixed student name/email header.";
        }

        return null;
    }

    // Adds a line just before the closing */ of the comment
    private static String insertBeforeClosing(String comment,
            String newLine) {
        int closingPosition = comment.lastIndexOf(COMMENT_END);
        return comment.substring(0, closingPosition)
                + newLine + "\n "
                + comment.substring(closingPosition);
    }

    // Reads the file into one String, keeping its line endings exactly
    private static String readWholeFile(File file)
            throws FileNotFoundException {
        Scanner sc = new Scanner(file);

        // "\\A" only matches the start of the file,
        // so next() returns everything
        sc.useDelimiter("\\A");

        String content = "";
        if (sc.hasNext()) {
            content = sc.next();
        }

        sc.close();
        return content;
    }

    private static void writeWholeFile(File file, String content)
            throws FileNotFoundException {
        PrintStream writer = new PrintStream(new FileOutputStream(file,
                false));
        writer.print(content);
        writer.close();
    }
}
