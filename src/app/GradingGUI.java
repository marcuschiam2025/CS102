package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GradingGUI {
    private static final Color BACKGROUND = new Color(20,28,82);
    private static final Color PANEL = new Color(199,147,29);
    private static final Color BUTTON = new Color(138,112,76);
    private static final Color TEXT = Color.WHITE;
    private static final Color ERROR = new Color(255, 90, 90);

    private static final int GAP = 10;

    private GradingCtrl ctrl;
    private JFrame frame;
    private JTextArea outputArea;
    private JLabel statusLabel;

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
            frame.getContentPane().setBackground(BACKGROUND);

            JPanel main = new JPanel(new BorderLayout(GAP, GAP));
            main.setBackground(BACKGROUND);
            main.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

            JLabel title = new JLabel("Auto Grading System");
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setForeground(TEXT);

            JPanel sidebar = createSidebar();
            JScrollPane output = createOutputArea();
            JLabel status = createStatusBar();

            main.add(title, BorderLayout.NORTH);
            main.add(sidebar, BorderLayout.WEST);
            main.add(output, BorderLayout.CENTER);
            main.add(status, BorderLayout.SOUTH);

            frame.setContentPane(main);
            frame.setVisible(true);
        });
    }

//-----------------------------------Creating the panels----------------------------------

    //to create the sidebar with buttons
    private JPanel createSidebar() {

        JPanel buttonList = new JPanel(new GridLayout(0, 1, GAP, GAP));
        buttonList.setBackground(BACKGROUND);

        buttonList.add(createButton("Grade all", () -> log("Grade all clicked")));
        buttonList.add(createButton("Grade one", () -> log("Grade one clicked")));
        buttonList.add(createButton("Check and fix", () -> processAllSubmissions()));
        buttonList.add(createButton("View results", () -> log("View results clicked")));
        buttonList.add(createButton("Export CSV", () -> log("Export CSV clicked")));
        buttonList.add(createButton("Settings", () -> log("Settings clicked")));

        // Putting the grid at the NORTH of another panel stops the
        // buttons from stretching to fill the whole height
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BACKGROUND);
        sidebar.add(buttonList, BorderLayout.NORTH);

        return sidebar;
    }

    private JScrollPane createOutputArea() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(PANEL);
        outputArea.setForeground(TEXT);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
        outputArea.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        // JScrollPane adds scroll bars when the text gets long
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JLabel createStatusBar() {
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(TEXT);
        return statusLabel;
    }

//-----------------------------------BUTTON ACTIONS----------------------------------
// Each button calls one of these. They ask ctrl (GradingCtrl) to do the
// work, then show the result in the window with log() / logError().

    // GUI version of GradingMenu.processAllSubmissions():
    // unzip, check and fix every submission, and show what was fixed.
    // Note: this runs on Swing's thread, so the window freezes until it
    // is done. Move the loop into a SwingWorker later to fix that.
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

//-----------------------------------HELPER----------------------------------

    //helper for creating a button
    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(event -> action.run());
        return button;
    }


    // for logging will delete later
    private void log(String message) {
        outputArea.append(message + "\n");
        statusLabel.setForeground(TEXT);
        statusLabel.setText(message.trim());
    }

    // for errors: red in the status bar, and also kept in the output area
    private void logError(String message) {
        outputArea.append("ERROR: " + message.trim() + "\n");
        statusLabel.setForeground(ERROR);
        statusLabel.setText(message.trim());
    }

}
