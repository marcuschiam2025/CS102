package fixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MisplacedFileFixer {
    public static void fix(File usernameFolder, File modelFolder,
            ArrayList<String> messages) {
        ArrayList<Exception> problems = new ArrayList<>();
        misplacedFileFix(usernameFolder, modelFolder, messages, problems);
        for (Exception problem : problems) {
            messages.add(problem.getMessage());
        }
    }

    public static void misplacedFileFix(File usernameFolder, File modelFolder,
            ArrayList<String> messages, ArrayList<Exception> problems) {
        if (!usernameFolder.isDirectory() || !modelFolder.isDirectory()) {
            problems.add(new IOException(
                    "Target folder or model folder is invalid."));
            return;
        }

        List<ModelFile> modelFiles = new ArrayList<>();
        List<String> modelFolders = new ArrayList<>();
        int initialProblemCount = problems.size();
        collectModel(modelFolder, "", modelFiles, modelFolders, problems);

        Set<String> studentFiles = new HashSet<>();
        Map<String, List<File>> filesByName = new HashMap<>();
        collectStudentFiles(usernameFolder, usernameFolder, studentFiles,
                filesByName, problems);
        if (problems.size() > initialProblemCount) {
            return;
        }

        Set<String> expectedPaths = new HashSet<>();
        Map<String, Integer> expectedNames = new HashMap<>();
        for (ModelFile modelFile : modelFiles) {
            expectedPaths.add(normalize(modelFile.relativePath));
            String name = normalize(modelFile.file.getName());
            expectedNames.put(name, expectedNames.getOrDefault(name, 0) + 1);
        }

        for (String relativeFolder : modelFolders) {
            createFolder(new File(usernameFolder, toSystemPath(relativeFolder)),
                    problems);
        }

        for (ModelFile modelFile : modelFiles) {
            File destination = new File(usernameFolder,
                    toSystemPath(modelFile.relativePath));

            if (studentFiles.contains(modelFile.relativePath)) {
                continue;
            }

            List<File> candidates = filesByName.getOrDefault(
                    normalize(modelFile.file.getName()), new ArrayList<>());
            List<File> pathMatches = new ArrayList<>();
            List<File> misplacedFiles = new ArrayList<>();

            for (File candidate : candidates) {
                String candidatePath = relativePath(usernameFolder, candidate);

                if (candidatePath.equalsIgnoreCase(modelFile.relativePath)) {
                    pathMatches.add(candidate);
                } else if (!expectedPaths.contains(normalize(candidatePath))
                        && expectedNames.get(normalize(
                                modelFile.file.getName())) == 1) {
                    misplacedFiles.add(candidate);
                }
            }
            if (!pathMatches.isEmpty()) {
                misplacedFiles = pathMatches;
            }

            if (misplacedFiles.isEmpty()) {
                continue;
            }
            if (misplacedFiles.size() > 1) {
                problems.add(new IOException("Cannot determine which "
                        + modelFile.file.getName() + " to move."));
                continue;
            }

            File source = misplacedFiles.get(0);
            try {
                if (!destination.getParentFile().isDirectory()) {
                    continue;
                }
                moveFile(source, destination);
                int parentSeparator = modelFile.relativePath.lastIndexOf('/');
                String destinationFolder = parentSeparator < 0
                        ? "."
                        : modelFile.relativePath.substring(0, parentSeparator);
                messages.add("Moved " + source.getName() + " to "
                        + destinationFolder + ".");
            } catch (IOException exception) {
                problems.add(new IOException("Cannot move "
                        + source.getName() + ": " + exception.getMessage(),
                        exception));
            }
        }
    }

    private static void collectModel(File folder, String relativeFolder,
            List<ModelFile> modelFiles, List<String> modelFolders,
            ArrayList<Exception> problems) {
        File[] items = folder.listFiles();
        if (items == null) {
            problems.add(new IOException("Cannot inspect model folder "
                    + folder + "."));
            return;
        }

        for (File item : items) {
            String relativePath = relativeFolder.isEmpty()
                    ? item.getName()
                    : relativeFolder + "/" + item.getName();

            if (item.isDirectory()) {
                modelFolders.add(relativePath);
                collectModel(item, relativePath, modelFiles, modelFolders,
                        problems);
            } else if (item.isFile()) {
                modelFiles.add(new ModelFile(item, relativePath));
            }
        }
    }

    private static void collectStudentFiles(File root, File folder,
            Set<String> studentFiles,
            Map<String, List<File>> filesByName,
            ArrayList<Exception> problems) {
        File[] items = folder.listFiles();
        if (items == null) {
            problems.add(new IOException("Cannot inspect folder " + folder
                    + "."));
            return;
        }

        for (File item : items) {
            if (item.isDirectory()) {
                collectStudentFiles(root, item, studentFiles, filesByName,
                        problems);
            } else if (item.isFile()) {
                String relativePath = relativePath(root, item);
                studentFiles.add(relativePath);
                filesByName.computeIfAbsent(normalize(item.getName()),
                        key -> new ArrayList<>()).add(item);
            }
        }
    }

    private static void createFolder(File folder,
            ArrayList<Exception> problems) {
        if (!folder.isDirectory() && !folder.mkdirs()
                && !folder.isDirectory()) {
            problems.add(new IOException("Cannot create folder " + folder
                    + "."));
        }
    }

    private static void moveFile(File source, File destination)
            throws IOException {
        if (destination.exists()) {
            if (!Files.isSameFile(source.toPath(), destination.toPath())) {
                throw new IOException("destination already exists.");
            }
            if (source.getAbsolutePath().equals(destination.getAbsolutePath())) {
                return;
            }

            Path temporary = source.toPath().resolveSibling(
                    source.getName() + ".misplaced-temp");
            int suffix = 1;
            while (Files.exists(temporary)) {
                temporary = source.toPath().resolveSibling(source.getName()
                        + ".misplaced-temp-" + suffix++);
            }
            Files.move(source.toPath(), temporary);
            try {
                Files.move(temporary, destination.toPath());
            } catch (IOException exception) {
                try {
                    Files.move(temporary, source.toPath());
                } catch (IOException restoreException) {
                    exception.addSuppressed(restoreException);
                }
                throw exception;
            }
            return;
        }

        Files.move(source.toPath(), destination.toPath());
    }

    private static String relativePath(File root, File file) {
        return root.toPath().toAbsolutePath().relativize(
                file.toPath().toAbsolutePath()).toString()
                .replace(File.separatorChar, '/');
    }

    private static String normalize(String path) {
        return path.toLowerCase(Locale.ROOT);
    }

    private static String toSystemPath(String path) {
        return path.replace('/', File.separatorChar);
    }

    private static class ModelFile {
        private final File file;
        private final String relativePath;

        private ModelFile(File file, String relativePath) {
            this.file = file;
            this.relativePath = relativePath;
        }
    }
}
