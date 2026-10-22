// package ThrowException;

// import java.nio.file.*;
// import java.util.*;
// import java.util.stream.*;
// import fileHandling.fileHandlings;
// import Validations.FolderNameChecker;

// // // public class StructureChecker {
// //     public static ArrayList<Exception> Structurecheck(String FilePath) {

// //         ArrayList<Exception> exceptions = new ArrayList<>();

// //         try {
// //             fileHandlings.unzip("../RenameToYourUsername.zip", "correctStructure");
// //         } catch (Exception e) {

// //         }
// //         Path root = Paths.get("correctStructure");
// //         Set<String> expected = new HashSet<>();
// //         // try (Stream<Path> paths = Files.walk(root)) {
// //         //     paths.filter(path -> !path.equals(root))
// //         //             .forEach(path -> {
// //         //                 String str = root.relativize(path)
// //         //                         .toString()
// //         //                         .replace("\\", "/");
// //         //                 String normalized = str.replaceFirst(
// //         //                         "^[^/]+",
// //         //                         "{folder}");

// //         //                 expected.add(normalized);

// //         //             });
// //         // } catch (Exception e) {
// //         //     e.printStackTrace();
// //         // }
// // //         try (Stream<Path> paths = Files.walk(root)) {
// // //     paths.filter(path -> !path.equals(root))
// // //          .forEach(path -> {
// // //              String str = root.relativize(path)
// // //                                .toString()
// // //                                .replace("\\", "/");

// // //              String normalized = str.replaceFirst(
// // //                  "^[^/]+",
// // //                  "{folder}"
// // //              );

// // //              expected.add(normalized);
// // //          });
// // // } catch (Exception e) {
// // //     e.printStackTrace();
// // // }
// // try (Stream<Path> paths = Files.walk(root)) {
// //     paths.filter(path -> !path.equals(root))
// //          .forEach(path -> {
// //              String str = root.relativize(path)
// //                               .toString()
// //                               .replace("\\", "/");

// //              // Remove the first folder from the expected path
// //              int index = str.indexOf('/');

// //              if (index != -1) {
// //                  str = str.substring(index + 1);
// //                  expected.add(str);
// //              }
// //          });
// // } catch (Exception e) {
// //     e.printStackTrace();
// // }

// // //         Set<String> actual = new HashSet<>();
// // //         Path student = Paths.get(FilePath);

// // //         // try (Stream<Path> paths = Files.walk(student)) {
// // //         //     paths.filter(path -> !path.equals(student))
// // //         //             .forEach(path -> {
// // //         //                 // String relative = student.relativize(path)
// // //         //                 // .toString()
// // //         //                 // .replace("\\", "/");
// // //         //                 // actual.add(relative);
// // //         //                 String str = student.relativize(path)
// // //         //                         .toString()
// // //         //                         .replace("\\", "/");
// // //         //                 if (FolderNameChecker.FolderName(str)) {
// // //         //                     ArrayList<String> pathParts = new ArrayList<>(Arrays.asList(str.split("/")));
// // //         //                     pathParts.remove(0);
// // //         //                     String normalized = String.join("/", pathParts);
// // //         //                     actual.add(normalized);
// // //         //                 } else {
// // //         //                     actual.add(str);
// // //         //                     exceptions.add(new Exception("Folder name is incorrect: " + str));
// // //         //                 }
// // //         //             });
// // //         // } catch (Exception e) {
// // //         //     e.printStackTrace();
// // //         // }
// // //         try (Stream<Path> paths = Files.walk(student)) {
// // //             Path stud = Paths.get(FilePath);

// // // System.out.println("\n========== Checking Student: "
// // //         + stud.getFileName() + " ==========");
// // //     paths.filter(path -> !path.equals(student))
// // //          .forEach(path -> {
// // //              String str = student.relativize(path)
// // //                                   .toString()
// // //                                   .replace("\\", "/");

// // //              String normalized = str.replaceFirst(
// // //                  "^[^/]+",
// // //                  "{folder}"
// // //              );

// // //              actual.add(normalized);

// // //              // Check the folder name only for directories
// // //              if (Files.isDirectory(path)
// // //                      && path.getParent().equals(student)
// // //                      && !FolderNameChecker.FolderName(
// // //                              path.getFileName().toString())) {

// // //                  exceptions.add(new Exception(
// // //                      "Folder name is incorrect: " + str
// // //                  ));
// // //              }
// // //          });
// // // } catch (Exception e) {
// // //     e.printStackTrace();
// // // }
// // Set<String> actual = new HashSet<>();
// // Path student = Paths.get(FilePath);

// // // Check whether the student's root folder exists
// // if (!Files.isDirectory(student)) {
// //     System.out.println("MISSING ROOT FOLDER: " + FilePath);

// //     exceptions.add(new Exception(
// //             "MISSING ROOT FOLDER: " + FilePath
// //     ));

// //     return exceptions;
// // }

// // // Get the name of the student's root folder
// // String folderName = student.getFileName().toString();

// // System.out.println("\n========== Checking Student: "
// //         + folderName + " ==========");

// // // Check whether the root folder name is valid
// // if (!FolderNameChecker.FolderName(folderName)) {
// //     System.out.println("Folder name is incorrect: " + folderName);

// //     exceptions.add(new Exception(
// //             "Folder name is incorrect: " + folderName
// //     ));
// // }

// // // Check everything inside the root folder
// // try (Stream<Path> paths = Files.walk(student)) {
// //     paths.filter(path -> !path.equals(student))
// //          .forEach(path -> {
// //              String str = student.relativize(path)
// //                                  .toString()
// //                                  .replace("\\", "/");

// //              actual.add(str);
// //          });
// // } catch (Exception e) {
// //     e.printStackTrace();
// // }

// //         // Check missing files/folders
// //         for (String path : expected) {
// //             // ArrayList<String> pathParts = new
// //             // ArrayList<>(Arrays.asList(path.split("/")));

// //             // pathParts.remove(0);
// //             // String str = String.join("/", pathParts);
// //             if (!actual.contains(path)) {
// //                 System.out.println("MISSING: " + path);
// //                 exceptions.add(new Exception("MISSING: " + path));
// //             }
// //         }

// //         // Check extra files/folders
// //         for (String path : actual) {
// //             // ArrayList<String> pathParts = new
// //             // ArrayList<>(Arrays.asList(path.split("/")));
// //             // pathParts.remove(0);
// //             // String str = String.join("/", pathParts);
// //             if (!expected.contains(path)) {
// //                 System.out.println("EXTRA: " + path);
// //                 exceptions.add(new Exception("EXTRA: " + path));
// //             }
// //         }
// //         return exceptions;
// //     }
// // }




// import java.nio.file.*;
// import java.util.*;
// import java.util.stream.*;

// import fileHandling.fileHandlings;
// import Validations.FolderNameChecker;

// public class StructureChecker {

//     public static ArrayList<Exception> Structurecheck(
//             String FilePath) {

//         ArrayList<Exception> exceptions = new ArrayList<>();

//         // Step 1: Unzip the correct template
//         try {
//             fileHandlings.unzip(
//                 "../RenameToYourUsername.zip",
//                 "correctStructure"
//             );
//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to unzip the correct structure: "
//                 + e.getMessage()
//             ));
//             return exceptions;
//         }

//         Path templateRoot = Paths.get("correctStructure");
//         Path studentRoot = Paths.get(FilePath);

//         // Step 2: Check whether the template exists
//         if (!Files.isDirectory(templateRoot)) {
//             exceptions.add(new Exception(
//                 "Correct structure folder not found."
//             ));
//             return exceptions;
//         }

//         // Step 3: Check whether the student's root folder exists
//         if (!Files.isDirectory(studentRoot)) {
//             System.out.println(
//                 "MISSING ROOT FOLDER: " + FilePath
//             );

//             exceptions.add(new Exception(
//                 "MISSING ROOT FOLDER: " + FilePath
//             ));
//             return exceptions;
//         }

//         // Step 4: Validate the student's root folder name
//         String folderName =
//             studentRoot.getFileName().toString();

//         System.out.println(
//             "\n========== Checking Student: "
//             + folderName + " =========="
//         );

//         if (!FolderNameChecker.FolderName(folderName)) {
//             exceptions.add(new Exception(
//                 "Folder name is incorrect: " + folderName
//             ));

//             System.out.println(
//                 "Folder name is incorrect: " + folderName
//             );
//         }

//         // Step 5: Find the template's top-level folder
//         Path templateStudentRoot = null;

//         try (Stream<Path> paths = Files.list(templateRoot)) {
//             templateStudentRoot = paths
//                 .filter(Files::isDirectory)
//                 .findFirst()
//                 .orElse(null);
//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to read template: " + e.getMessage()
//             ));
//             return exceptions;
//         }

//         if (templateStudentRoot == null) {
//     exceptions.add(new Exception(
//         "No top-level folder found in the template."
//     ));
//     return exceptions;
// }

// // Add this line
// final Path expectedRoot = templateStudentRoot;

// // Step 6: Collect expected paths inside the template folder
// Set<String> expected = new HashSet<>();

// try (Stream<Path> paths = Files.walk(expectedRoot)) {

//     paths.filter(path -> !path.equals(expectedRoot))
//          .forEach(path -> {
//              String relative = expectedRoot.relativize(path)
//                                            .toString()
//                                            .replace("\\", "/");

//              expected.add(relative);
//          });

// } catch (Exception e) {
//     exceptions.add(new Exception(
//         "Unable to read expected paths: " + e.getMessage()
//     ));
//     return exceptions;
// }

//         // Step 7: Collect actual paths inside the student folder
//         Set<String> actual = new HashSet<>();

//         try (Stream<Path> paths = Files.walk(studentRoot)) {

//             paths.filter(path -> !path.equals(studentRoot))
//                  .forEach(path -> {
//                      String relative =
//                          studentRoot.relativize(path)
//                                     .toString()
//                                     .replace("\\", "/");

//                      actual.add(relative);
//                  });

//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to read student paths: "
//                 + e.getMessage()
//             ));
//             return exceptions;
//         }

//         // Step 8: Find missing files and folders
//         for (String path : expected) {
//             if (!actual.contains(path)) {
//                 System.out.println("MISSING: " + path);
//                 exceptions.add(new Exception(
//                     "MISSING: " + path
//                 ));
//             }
//         }

//         // Step 9: Find extra files and folders
//         for (String path : actual) {
//             if (!expected.contains(path)) {
//                 System.out.println("EXTRA: " + path);
//                 exceptions.add(new Exception(
//                     "EXTRA: " + path
//                 ));
//             }
//         }

//         return exceptions;
//     }
// }



// package ThrowException;

// import java.nio.file.*;
// import java.util.*;
// import java.util.stream.*;

// import fileHandling.fileHandlings;
// import Validations.FolderNameChecker;

// public class StructureChecker {

//     public static ArrayList<Exception> Structurecheck(String FilePath) {

//         ArrayList<Exception> exceptions = new ArrayList<>();

//         // Step 1: Unzip the correct structure
//         try {
//             fileHandlings.unzip(
//                 "../RenameToYourUsername.zip",
//                 "correctStructure"
//             );
//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to unzip the correct structure: "
//                 + e.getMessage()
//             ));
//             return exceptions;
//         }

//         // Step 2: Collect expected paths
//         Path root = Paths.get("correctStructure/RenameToYourUsername");
//         Set<String> expected = new HashSet<>();

//         try (Stream<Path> paths = Files.walk(root)) {

//             paths.filter(path -> !path.equals(root))
//                  .forEach(path -> {
//                      String relative = root.relativize(path)
//                                            .toString()
//                                            .replace("\\", "/");

//                      expected.add(relative);
//                  });

//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to read the correct structure: "
//                 + e.getMessage()
//             ));
//             return exceptions;
//         }

//         // Step 3: Collect actual student paths
//         Set<String> actual = new HashSet<>();
//         Path student = Paths.get(FilePath);

//         try (Stream<Path> paths = Files.walk(student)) {

//             paths.filter(path -> !path.equals(student))
//                  .forEach(path -> {
//                      String relative = student.relativize(path)
//                                               .toString()
//                                               .replace("\\", "/");

//                      actual.add(relative);
//                  });

//         } catch (Exception e) {
//             exceptions.add(new Exception(
//                 "Unable to read the student's structure: "
//                 + e.getMessage()
//             ));
//             return exceptions;
//         }

//         // Step 4: Check missing files and folders
//         for (String path : expected) {
//             ArrayList<String> pathParts = new ArrayList<>(Arrays.asList(path.split("/")));
//             pathParts.remove(0);
//             String str = String.join("/", pathParts); 
            
//             if (!actual.contains(str)) {

//                 System.out.println("MISSING: " + path);
//                 exceptions.add(new Exception("MISSING: " + path));
//             }
//         }

//         // Step 5: Check extra files and folders
//         for (String path : actual) {
//             ArrayList<String> pathParts = new ArrayList<>(Arrays.asList(path.split("/")));
//             pathParts.remove(0);
//             String str = String.join("/", pathParts); 

//             if (!expected.contains(str)) {
//                 System.out.println("EXTRA: " + str);
//                 exceptions.add(new Exception("EXTRA: " + str));
//             }
//         }

//         return exceptions;
//     }
// }




package ThrowException;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import fileHandling.fileHandlings;

public class StructureChecker {

    public static ArrayList<Exception> Structurecheck(String FilePath) {

        ArrayList<Exception> exceptions = FileStructure.wrongFileStructure(FilePath);

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
                + e.getMessage() + " for " + studentRoot.getFileName()
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
                System.out.println("MISSING: " + path + " for " + FilePath);

                exceptions.add(new Exception(
                    "MISSING: " + path + " for " + FilePath
                ));
            }
        }

        // Step 7: Check extra files and folders
        for (String path : actual) {

            if (!expected.contains(path)) {
                System.out.println("EXTRA: " + path + " for " + FilePath);

                exceptions.add(new Exception(
                    "EXTRA: " + path + " for " + FilePath
                ));
            }
        }

        return exceptions;
    }
}
