import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import fileHandling.fileHandlings;
public class StructureChecker {
    public static ArrayList<Exception> Structurecheck(String FilePath) throws Exception{

        

        ArrayList<Exception> exceptions = new ArrayList<>();
    
        try{
            fileHandlings.unzip("RenameToYourUsername.zip", "correctStructure");
        }catch(Exception e){

        }
        Path root = Paths.get("correctStructure");
        Set<String> expected = new HashSet<>();
         try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> !path.equals(root))
                 .forEach(path -> {
                     expected.add(root.relativize(path)
                                            .toString()
                                            .replace("\\", "/"));
                 });
        }

        Set<String> actual = new HashSet<>();
Path student = Paths.get(FilePath);

        try (Stream<Path> paths = Files.walk(student)) {
            paths.filter(path -> !path.equals(student))
                 .forEach(path -> {
                     String relative = student.relativize(path)
                                            .toString()
                                            .replace("\\", "/");
                     actual.add(relative);
                 });
        }

        // Check missing files/folders
        for (String path : expected) {
            if (!actual.contains(path)) {
                System.out.println("MISSING: " + path);
            }
        }

        // Check extra files/folders
        for (String path : actual) {
            if (!expected.contains(path)) {
                System.out.println("EXTRA: " + path);
            }
        }
        return exceptions;
    }
}