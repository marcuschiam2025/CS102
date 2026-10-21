package ThrowException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import fileHandling.fileHandlings;

public class idk {
    public static ArrayList<Exception> Structurecheck(String FilePath){
        ArrayList<Exception> exceptions = new ArrayList<>();
        try {
            fileHandlings.unzip("../RenameToYourUsername.zip", "correctStructure");
        } catch (Exception e) {

        }
        Path root = Paths.get("correctStructure");
        HashMap<String, String[]> expected = new HashMap<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> !path.equals(root))
                    .forEach(path -> {
                        ArrayList<String> pathParts = new ArrayList<>(java.util.Arrays.asList(root.relativize(path).toString().replace("\\", "/").split("/")));
                        String parentFolder = pathParts.get(0);
                        pathParts.remove(0);
                        String str = String.join("/", pathParts);
                        if (!expected.containsKey(parentFolder)) {
                            expected.put(parentFolder, new String[0]);
                        }
                        // Note: This is a simplified approach; you might want to use a more sophisticated data structure to handle multiple files in the same folder
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }


        return exceptions;
    }
}
