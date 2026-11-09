package fixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

// Fixes submissions where Q1/Q2/Q3 are buried in extra folders, e.g.
//
//   root/MyExam/ping.lee.2023/Q1      ->  root/ping.lee.2023/Q1
//   root/ping.lee.2023/ping.lee.2023/Q1  ->  root/ping.lee.2023/Q1
//   root/ping.lee.2023/src/Q1         ->  root/ping.lee.2023/Q1
//
// The grader expects the question folders directly inside the username
// folder, so everything in the folder that holds them is moved up into
// the username folder, and the empty folders left behind are deleted.
//
// Does nothing if the questions are already in the right place, or are
// at the root (HierarchyFixer handles that case).
public class NestedFolderFixer {
    private static final String QUESTION_FOLDER = "Q\\d+";
    private static final String MAC_FOLDER = "__MACOSX";

    public static void fix(File root, File usernameFolder,
            ArrayList<String> messages, ArrayList<Exception> problems) {
        if (hasQuestionFolder(usernameFolder) || hasQuestionFolder(root)) {
            return;
        }

        File holder = findQuestionHolder(root);

        if (holder == null) {
            return;
        }

        if (usernameFolder.exists() && !usernameFolder.isDirectory()) {
            problems.add(new IOException("Cannot create username folder: "
                    + "a file has that name."));
            return;
        }

        if (!usernameFolder.isDirectory() && !usernameFolder.mkdirs()) {
            problems.add(new IOException("Cannot create username folder "
                    + usernameFolder + "."));
            return;
        }

        String from = relativePath(root, holder);
        boolean movedAll = true;

        for (File item : holder.listFiles()) {
            File destination = new File(usernameFolder, item.getName());

            // e.g. moving root/u/u/Q1 up: "item" is never the username
            // folder itself, but a same-named file may already be there
            if (destination.exists()) {
                problems.add(new IOException("Cannot move " + from + "/"
                        + item.getName() + ": " + item.getName()
                        + " already exists in the username folder."));
                movedAll = false;
            } else if (!item.renameTo(destination)) {
                problems.add(new IOException("Cannot move " + from + "/"
                        + item.getName() + "."));
                movedAll = false;
            }
        }

        if (movedAll) {
            messages.add("Moved the question folders out of " + from
                    + " into the username folder.");
        }

        deleteEmptyFolders(holder, root, usernameFolder);
    }

    // Breadth-first, so the shallowest folder holding Q1/Q2/... wins
    private static File findQuestionHolder(File root) {
        LinkedList<File> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            File folder = queue.removeFirst();
            File[] items = folder.listFiles();

            if (items == null) {
                continue;
            }

            if (folder != root && hasQuestionFolder(folder)) {
                return folder;
            }

            for (File item : items) {
                if (item.isDirectory() && !item.getName().equals(MAC_FOLDER)
                        && !item.getName().matches(QUESTION_FOLDER)) {
                    queue.add(item);
                }
            }
        }

        return null;
    }

    private static boolean hasQuestionFolder(File folder) {
        File[] items = folder.listFiles();

        if (items == null) {
            return false;
        }

        for (File item : items) {
            if (item.isDirectory() && item.getName().matches(QUESTION_FOLDER)) {
                return true;
            }
        }

        return false;
    }

    // Deletes the folder and then each empty parent, stopping at the root
    // and never deleting the username folder
    private static void deleteEmptyFolders(File folder, File root,
            File usernameFolder) {
        File rootPath = root.getAbsoluteFile();
        File keep = usernameFolder.getAbsoluteFile();
        File current = folder.getAbsoluteFile();

        while (current != null && !current.equals(rootPath)
                && !current.equals(keep)) {
            String[] left = current.list();

            if (left == null || left.length > 0 || !current.delete()) {
                return;
            }

            current = current.getParentFile();
        }
    }

    // e.g. "MyExam/ping.lee.2023"
    private static String relativePath(File root, File folder) {
        return root.getAbsoluteFile().toPath()
                .relativize(folder.getAbsoluteFile().toPath()).toString()
                .replace(File.separatorChar, '/');
    }
}
