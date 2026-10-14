package ThrowException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import fileHandling.fileHandlings;


public class MissingNameOrEmail{
    public static void catchMissingNames() throws MissingNameException, MissingEmailException {
        fileHandlings fileHandling = new fileHandlings();
        try {
     
            Path start = Paths.get("unzippedFolder");
            Files.walk(start)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        ArrayList<String> stringsOfRows = fileHandling.readFiles("\n", "" + path);
                        for (int i = 0; i < stringsOfRows.size(); i++) {
                            if (i == 1) {
                                String name = stringsOfRows.get(i);
                                if (name.length() <= 10) {
                                    System.out.println(path + " has no name");
                                    // throw new MissingNameException(name + " does not have name inside the file");
                                }
                            } else if (i == 2) {
                                String name = stringsOfRows.get(i);
                                if (name.length() <= 15) {
                                    System.out.println(path +  " has no email");
                                    // throw new MissingEmailException(name + " does not have email inside the file");
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
