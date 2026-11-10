package checker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import exceptions.MissingEmailException;
import exceptions.MissingNameException;
import filehandling.FileHandlings;

public class MissingNameOrEmail {
    // The header comment has the name on the 2nd line and the email on the
    // 3rd line (index 1 and 2)
    private static final int NAME_LINE = 1;
    private static final int EMAIL_LINE = 2;

    // A line this short or shorter only has the "Name:" / "Email:" label
    // and nothing after it
    private static final int MIN_NAME_LINE_LENGTH = 10;
    private static final int MIN_EMAIL_LINE_LENGTH = 15;

    public static ArrayList<Exception> catchMissingNames(String filePath)
            throws MissingNameException, MissingEmailException {
        ArrayList<Exception> arrayOfExceptions = new ArrayList<>();

        try {
            Path start = Paths.get(filePath);
            Files.walk(start)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> checkHeader(path, arrayOfExceptions));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return arrayOfExceptions;
    }

    // Adds an exception if the file's name or email line is empty
    private static void checkHeader(Path path,
            ArrayList<Exception> arrayOfExceptions) {
        ArrayList<String> stringsOfRows =
                FileHandlings.readFiles("\n", path.toString());

        // e.g. "Q3/Q3.java", so the message says which file
        String fileName = path.getParent().getFileName() + "/"
                + path.getFileName();

        if (stringsOfRows.size() > NAME_LINE
                && stringsOfRows.get(NAME_LINE).length()
                        <= MIN_NAME_LINE_LENGTH) {
            arrayOfExceptions.add(new MissingNameException(
                    fileName + " does not have name inside the file"));
        }

        if (stringsOfRows.size() > EMAIL_LINE
                && stringsOfRows.get(EMAIL_LINE).length()
                        <= MIN_EMAIL_LINE_LENGTH) {
            arrayOfExceptions.add(new MissingEmailException(
                    fileName + " does not have email inside the file"));
        }
    }
}
