package gui;

import app.GradingCtrl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import model.StudentResult;

public class GradingGUI {

    private static final String MAIN_SCREEN = "main";
    private static final String SETTINGS_SCREEN = "settings";

    private static final String GRADING_NOTE = "Grading each test case "
            + "on its own, this can take a few seconds per student.";

    private GradingCtrl ctrl;
    private JFrame frame;

    private CardLayout cards = new CardLayout();
    private JPanel screens = new JPanel(cards);

    private JTextArea outputArea;
    private JLabel statusLabel;

    // Every sidebar button, so they can be turned off while a step runs
    private ArrayList<JButton> buttons = new ArrayList<>();

    // Steps 2 and 3 stay locked until the step before them has run
    // (or "Run all steps" has run, which does all three)
    private JButton fixButton;
    private JButton gradeButton;
    private boolean checkDone = false;
    private boolean fixDone = false;

    private SettingGUI settings;

    // One grading step run on one submission, e.g. ctrl::checkSubmission.
    // Returns the lines to show the user. (Same idea as in GradingMenu.)
    private interface Step {
        ArrayList<String> run(File zipFile) throws IOException;
    }

    public GradingGUI() {
        ctrl = new GradingCtrl();
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {

            frame = new JFrame("Auto Grading System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.getContentPane().setBackground(GuiStyle.BACKGROUND);

            settings = new SettingGUI(ctrl, () -> cards.show(screens, MAIN_SCREEN));

            screens.add(createMainScreen(), MAIN_SCREEN);
            screens.add(settings.createSettingsScreen(), SETTINGS_SCREEN);
            frame.setContentPane(screens);
            frame.setVisible(true);
        });
    }

    // -----------------------------------Creating the
    // panels----------------------------------

    private JPanel createMainScreen() {
        JPanel main = new JPanel(new BorderLayout(GuiStyle.GAP, GuiStyle.GAP));
        main.setBackground(GuiStyle.BACKGROUND);
        main.setBorder(BorderFactory.createEmptyBorder(GuiStyle.GAP, GuiStyle.GAP, GuiStyle.GAP, GuiStyle.GAP));

        JLabel title = GuiStyle.createTitle("Auto Grading System");

        JPanel sidebar = createSidebar();
        JScrollPane output = createOutputArea();
        JLabel status = createStatusBar();

        main.add(title, BorderLayout.NORTH);
        main.add(sidebar, BorderLayout.WEST);
        main.add(output, BorderLayout.CENTER);
        main.add(status, BorderLayout.SOUTH);

        return main;
    }

    // to create the sidebar with buttons, in the same order as the
    // console menu (GradingMenu)
    private JPanel createSidebar() {

        JPanel buttonList = new JPanel(new GridLayout(0, 1, GuiStyle.GAP, GuiStyle.GAP));
        buttonList.setBackground(GuiStyle.BACKGROUND);

        // Grading steps
        addButton(buttonList, "1. Check submissions", () -> checkAllSubmissions());
        fixButton = addButton(buttonList, "2. Fix submissions", () -> fixAllSubmissions());
        gradeButton = addButton(buttonList, "3. Grade submissions", () -> gradeAllSubmissions());
        updateStepButtons();

        // Results
        addButton(buttonList, "View results", () -> viewResults());
        addButton(buttonList, "Export CSV", () -> exportResults());

        // Other
        addButton(buttonList, "Run all steps", () -> runAllStepsOnAll());
        addButton(buttonList, "Run all on one", () -> runAllStepsOnOne());
        addButton(buttonList, "Settings", () -> openSettings());

        // Putting the grid at the NORTH of another panel stops the
        // buttons from stretching to fill the whole height
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(GuiStyle.BACKGROUND);
        sidebar.add(buttonList, BorderLayout.NORTH);

        return sidebar;
    }

    // Makes a styled button, adds it to the panel and remembers it
    private JButton addButton(JPanel panel, String text, Runnable action) {
        JButton button = GuiStyle.createButton(text, action);
        buttons.add(button);
        panel.add(button);
        return button;
    }

    private JScrollPane createOutputArea() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(GuiStyle.PANEL);
        outputArea.setForeground(GuiStyle.TEXT);
        outputArea.setFont(GuiStyle.OUTPUT_FONT);
        outputArea.setBorder(BorderFactory.createEmptyBorder(GuiStyle.GAP, GuiStyle.GAP, GuiStyle.GAP, GuiStyle.GAP));

        // JScrollPane adds scroll bars when the text gets long
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JLabel createStatusBar() {
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(GuiStyle.TEXT);
        return statusLabel;
    }

// -----------------------------------Button actions----------------------------------

    private void checkAllSubmissions() {
        runInBackground(() -> {
            if (processAllSubmissions(ctrl::checkSubmission)) {
                checkDone = true;
            }
        });
    }

    private void fixAllSubmissions() {
        runInBackground(() -> {
            if (processAllSubmissions(ctrl::fixSubmission)) {
                fixDone = true;
            }
        });
    }

    private void gradeAllSubmissions() {
        runInBackground(() -> {
            StudentResult.loadStudentResultTemplate();
            log("\n" + GRADING_NOTE);
            processAllSubmissions(ctrl::gradeSubmission);
        });
    }

    private void runAllStepsOnAll() {
        runInBackground(() -> {
            StudentResult.loadStudentResultTemplate();
            log("\n" + GRADING_NOTE);
            if (processAllSubmissions(ctrl::processSubmission)) {
                checkDone = true;
                fixDone = true;
            }
        });
    }

    private void runAllStepsOnOne() {
        // Choose on the GUI thread, then grade in the background
        File zipFile = chooseSubmission();

        if (zipFile == null) {
            return;
        }

        runInBackground(() -> {
            StudentResult.loadStudentResultTemplate();
            log("\n" + GRADING_NOTE);
            log("\n" + zipFile.getName());

            if (processSubmission(zipFile, ctrl::processSubmission)) {
                log("\nDone.");
            }
        });
    }

    private void viewResults() {
        log("\n" + ctrl.getResultsText());
    }

    private void exportResults() {
        if (!ctrl.hasResults()) {
            logError("Nothing to export yet. Grade the submissions first.");
        } else if (ctrl.exportCSV()) {
            log("\nSuccessfully exported to " + ctrl.getExportFile());
        } else {
            logError("Export failed: could not write " + ctrl.getExportFile()
                    + ". Close it if it is open in another program (e.g. Excel) and try again.");
        }
    }

    // Runs one step (or all of them) on every submission.
    // Returns false if there were no submissions to run it on.
    private boolean processAllSubmissions(Step step) {
        // 1. Ask ctrl for the zip files
        File[] zipFiles = getZipFiles();

        if (zipFiles == null) {
            return false;
        }

        // 2. Process each zip and show what happened
        int failed = 0;

        for (int i = 0; i < zipFiles.length; i++) {
            log("\n[" + (i + 1) + "/" + zipFiles.length + "] " + zipFiles[i].getName());

            if (!processSubmission(zipFiles[i], step)) {
                failed++;
            }
        }

        // 3. Show a summary
        int processed = zipFiles.length - failed;
        log("\nDone. " + processed + " of " + zipFiles.length + " submissions processed.");
        return true;
    }

    // Returns true if the submission was processed, false if it failed.
    // One submission failing must not stop the others.
    private boolean processSubmission(File zipFile, Step step) {
        try {
            ArrayList<String> lines = step.run(zipFile);

            for (String line : lines) {
                log("  - " + line);
            }
            return true;
        } catch (Exception exception) {
            logError("  Failed: " + exception.getMessage());
            return false;
        }
    }

    // Fill the settings page with the current values, then switch to it.
    // The Back button on that page switches back (see show()).
    private void openSettings() {
        settings.openSettings();
        cards.show(screens, SETTINGS_SCREEN);
    }

    // -----------------------------------HELPER----------------------------------

    // Grading runs the testers, which takes a few seconds per student.
    // Doing that on the GUI thread would freeze the window, so it runs on
    // its own thread instead, with the buttons turned off until it is done.
    private void runInBackground(Runnable work) {
        setButtonsEnabled(false);

        Thread worker = new Thread(() -> {
            try {
                work.run();
            } finally {
                SwingUtilities.invokeLater(() -> setButtonsEnabled(true));
            }
        });
        worker.start();
    }

    private void setButtonsEnabled(boolean enabled) {
        for (JButton button : buttons) {
            button.setEnabled(enabled);
        }

        if (enabled) {
            updateStepButtons();
        }
    }

    // Only unlock a step once the step before it has run
    private void updateStepButtons() {
        fixButton.setEnabled(checkDone);
        gradeButton.setEnabled(fixDone);
    }

    // Lets the user pick one submission from a drop-down list.
    // Returns null if they cancel.
    private File chooseSubmission() {
        File[] zipFiles = getZipFiles();

        if (zipFiles == null) {
            return null;
        }

        String[] names = new String[zipFiles.length];
        for (int i = 0; i < zipFiles.length; i++) {
            names[i] = zipFiles[i].getName();
        }

        Object choice = JOptionPane.showInputDialog(frame, "Choose a submission:",
                "Run all steps on one submission", JOptionPane.PLAIN_MESSAGE,
                null, names, names[0]);

        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(choice)) {
                return zipFiles[i];
            }
        }

        return null;
    }

    // Returns the zip files, or null (after telling the user why)
    // if there are none
    private File[] getZipFiles() {
        File[] zipFiles = ctrl.findZipFiles();

        if (zipFiles == null) {
            logError("Submissions folder not found: " + ctrl.getSubmissionsFolder());
            return null;
        }

        if (zipFiles.length == 0) {
            logError("No ZIP files found in " + ctrl.getSubmissionsFolder());
            return null;
        }

        return zipFiles;
    }

    // Shows a message in the output area and the status bar.
    // Swing components must only be changed on the GUI thread, so this
    // hands the change over with invokeLater (it can be called from the
    // background thread in runInBackground).
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            statusLabel.setForeground(GuiStyle.TEXT);
            statusLabel.setText(lastLine(message));
        });
    }

    // for errors: red in the status bar, and also kept in the output area
    private void logError(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append("ERROR: " + message.trim() + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            statusLabel.setForeground(GuiStyle.ERROR);
            statusLabel.setText(message.trim());
        });
    }

    // The status bar is one line, so show the last line of a long message
    // (e.g. the results table)
    private String lastLine(String message) {
        String[] lines = message.trim().split("\n");
        return lines[lines.length - 1];
    }

}
