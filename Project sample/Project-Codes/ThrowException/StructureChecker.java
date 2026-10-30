package ThrowException;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import fileHandling.fileHandlings;

public class StructureChecker {

    public static ArrayList<Exception> Structurecheck(String FilePath) {

        ArrayList<Exception> exceptions = new ArrayList<>();

        // Step 1: Unzip the correct structure
        try {
            fileHandlings.unzip(
                "../RenameToYourUsername.zip",
                "correctStructure"
            );
        } catch (Exception e) {
            exceptions.add(new Exception(
                "Unable to unzip the correct structure: "
                + e.getMessage()
            ));
            return exceptions;
        }

        // Step 2: Set the reference root
        Path referenceRoot = Paths.get(
            "correctStructure/RenameToYourUsername"
        );

        Path studentLocation = Paths.get(FilePath)
                                    .toAbsolutePath()
                                    .normalize();

        if (!Files.isDirectory(referenceRoot)) {
            exceptions.add(new Exception(
                "Correct structure folder not found: "
                + referenceRoot
            ));
            return exceptions;
        }

        if (!Files.isDirectory(studentLocation)) {
            exceptions.add(new Exception(
                "MISSING ROOT FOLDER: " + studentLocation
            ));
            return exceptions;
        }

        // Step 3: Automatically detect the student's root
        Path studentRoot;

        Path placeholder = studentLocation.resolve(
            "RenameToYourUsername"
        );

        if (Files.isDirectory(placeholder)) {

            // Student C: kept the placeholder folder name
            studentRoot = placeholder;

            System.out.println(
                "WARNING: Student did not rename the folder."
            );

        } else if (Files.isDirectory(
                studentLocation.resolve("Q3"))) {

            // Student B: Q3 is directly inside the submission
            studentRoot = studentLocation;

        } else {

            // Student A: username folder is inside submission
            List<Path> possibleRoots;

            try (Stream<Path> paths = Files.list(studentLocation)) {
                possibleRoots = paths
                    .filter(Files::isDirectory)
                    .filter(path ->
                        Files.isDirectory(path.resolve("Q3")))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                exceptions.add(new Exception(
                    "Unable to inspect student folders: "
                    + e.getMessage()
                ));
                return exceptions;
            }

            if (possibleRoots.size() != 1) {
                exceptions.add(new Exception(
                    "Unable to identify a unique student root "
                    + "folder inside: " + studentLocation
                ));
                return exceptions;
            }

            studentRoot = possibleRoots.get(0);
        }

        System.out.println(
            "\n========== Checking Student: "
            + studentRoot.getFileName()
            + " =========="
        );

        // Step 4: Collect expected paths relative to reference root
        Set<String> expected = new HashSet<>();

        try (Stream<Path> paths = Files.walk(referenceRoot)) {

            paths.filter(path -> !path.equals(referenceRoot))
                 .forEach(path -> {
                     String relative = referenceRoot
                         .relativize(path)
                         .toString()
                         .replace("\\", "/");

                     expected.add(relative);
                 });

        } catch (Exception e) {
            exceptions.add(new Exception(
                "Unable to read the correct structure: "
                + e.getMessage()
            ));
            return exceptions;
        }

        // Step 5: Collect actual paths relative to student root
        Set<String> actual = new HashSet<>();

        try (Stream<Path> paths = Files.walk(studentRoot)) {

            paths.filter(path -> !path.equals(studentRoot))
                 .forEach(path -> {
                     String relative = studentRoot
                         .relativize(path)
                         .toString()
                         .replace("\\", "/");

                     actual.add(relative);
                 });

        } catch (Exception e) {
            exceptions.add(new Exception(
                "Unable to read the student's structure: "
                + e.getMessage()
            ));
            return exceptions;
        }

        // Step 6: Check missing files and folders
        for (String path : expected) {

            if (!actual.contains(path)) {
                System.out.println("MISSING: " + path);

                exceptions.add(new Exception(
                    "MISSING: " + path
                ));
            }
        }

        // Step 7: Check extra files and folders
        for (String path : actual) {

            if (!expected.contains(path)) {
                System.out.println("EXTRA: " + path);

                exceptions.add(new Exception(
                    "EXTRA: " + path
                ));
            }
        }

        return exceptions;
    }
}
