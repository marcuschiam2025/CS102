package gui;

import app.GradingCtrl;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GradingGUI {

    private static final String MAIN_SCREEN = "main";
    private static final String SETTINGS_SCREEN = "settings";

    private GradingCtrl ctrl;
    private JFrame frame;

    private CardLayout cards = new CardLayout();
    private JPanel screens = new JPanel(cards);

    private JTextArea outputArea;
    private JLabel statusLabel;

    private SettingGUI settings;

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

    // to create the sidebar with buttons
    private JPanel createSidebar() {

        JPanel buttonList = new JPanel(new GridLayout(0, 1, GuiStyle.GAP, GuiStyle.GAP));
        buttonList.setBackground(GuiStyle.BACKGROUND);

        buttonList.add(GuiStyle.createButton("Grade all", () -> log("Grade all clicked")));
        buttonList.add(GuiStyle.createButton("Grade one", () -> log("Grade one clicked")));
        buttonList.add(GuiStyle.createButton("Check and fix", () -> processAllSubmissions()));
        buttonList.add(GuiStyle.createButton("View results", () -> log("View results clicked")));
        buttonList.add(GuiStyle.createButton("Export CSV", () -> log("Export CSV clicked")));
        buttonList.add(GuiStyle.createButton("Settings", () -> openSettings()));

        // Putting the grid at the NORTH of another panel stops the
        // buttons from stretching to fill the whole height
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(GuiStyle.BACKGROUND);
        sidebar.add(buttonList, BorderLayout.NORTH);

        return sidebar;
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
    private void processAllSubmissions() {
        // 1. Ask ctrl for the zip files
        File[] zipFiles = ctrl.findZipFiles();

        if (zipFiles == null) {
            logError("Submissions folder not found: " + ctrl.getSubmissionsFolder());
            return;
        }

        if (zipFiles.length == 0) {
            logError("No ZIP files found in " + ctrl.getSubmissionsFolder());
            return;
        }

        // 2. Process each zip and show the fixes
        int failed = 0;

        for (int i = 0; i < zipFiles.length; i++) {
            log("\n[" + (i + 1) + "/" + zipFiles.length + "] " + zipFiles[i].getName());

            try {
                ArrayList<String> fixes = ctrl.processSubmission(zipFiles[i]);

                for (String fix : fixes) {
                    log("  - " + fix);
                }
            } catch (Exception exception) {
                // One submission failing must not stop the others
                logError("  Failed: " + exception.getMessage());
                failed++;
            }
        }

        // 3. Show a summary
        int processed = zipFiles.length - failed;
        log("\nDone. " + processed + " of " + zipFiles.length + " submissions processed.");
    }

    // Fill the settings page with the current values, then switch to it.
    // The Back button on that page switches back (see show()).
    private void openSettings() {
        settings.openSettings();
        cards.show(screens, SETTINGS_SCREEN);
    }

    // -----------------------------------HELPER----------------------------------

    // for logging will delete later
    private void log(String message) {
        outputArea.append(message + "\n");
        statusLabel.setForeground(GuiStyle.TEXT);
        statusLabel.setText(message.trim());
    }

    // for errors: red in the status bar, and also kept in the output area
    private void logError(String message) {
        outputArea.append("ERROR: " + message.trim() + "\n");
        statusLabel.setForeground(GuiStyle.ERROR);
        statusLabel.setText(message.trim());
    }

}
