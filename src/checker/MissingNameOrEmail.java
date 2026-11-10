package checker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import exceptions.MissingEmailException;
import exceptions.MissingNameException;
// import checker.StructureChecker;
import filehandling.*;;
// Co-authored with ChatGPT

public class MissingNameOrEmail{
    public static ArrayList<Exception> catchMissingNames(String filePath) throws MissingNameException, MissingEmailException {
        FileHandlings fileHandling = new FileHandlings();
        ArrayList<Exception> arrayOfExceptions = new ArrayList<>();
        try {
            Path start = Paths.get(filePath);
            Files.walk(start)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        ArrayList<String> stringsOfRows = fileHandling.readFiles("\n", "" + path);
                        // e.g. "Q3/Q3.java", so the message says which file
                        String fileName = path.getParent().getFileName() + "/" + path.getFileName();
                        for (int i = 0; i < stringsOfRows.size(); i++) {
                            if (i == 1) {
                                String name = stringsOfRows.get(i);
                                if (name.length() <= 10) {
                                    arrayOfExceptions.add(
                                            new MissingNameException(fileName + " does not have name inside the file"));
                                }
                            } else if (i == 2) {
                                String name = stringsOfRows.get(i);
                                if (name.length() <= 15) {
                                    arrayOfExceptions.add(
                                            new MissingEmailException(fileName + " does not have email inside the file"));
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayOfExceptions;

    }
}
