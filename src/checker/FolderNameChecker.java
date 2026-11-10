package checker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class FolderNameChecker {
    // e.g. "david.2024"
    private static final String SHORT_USERNAME = "^[A-Za-z]+[.][0-9]{4}$";
    // e.g. "chee.teo.2022"
    private static final String LONG_USERNAME =
            "^[A-Za-z]+[.][A-Za-z]+[.][0-9]{4}$";

    // True if the folder's name looks like a username
    public static boolean isValidFolderName(String filepath) {
        Path path = Paths.get(filepath);

        // Get the folder's name directly
        String folderName = path.getFileName().toString();

        return Pattern.matches(SHORT_USERNAME, folderName)
                || Pattern.matches(LONG_USERNAME, folderName);
    }
}
