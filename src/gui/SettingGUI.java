package gui;

import app.GradingCtrl;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

// Co-authored with Claude

public class SettingGUI {

    private GradingCtrl ctrl;
    private Runnable onBack;

    private JTextField submissionsField;
    private JTextField testerField;
    private JTextField scoreSheetField;
    private JTextField studentResultTemplateField;
    private JTextField modelFolderField;
    private JLabel settingsMessage;

    public SettingGUI(GradingCtrl ctrl, Runnable onBack) {
        this.ctrl = ctrl;
        this.onBack = onBack;
    }

    public void openSettings() {
        submissionsField.setText(ctrl.getSubmissionsFolder());
        testerField.setText(ctrl.getTesterFolder());
        scoreSheetField.setText(ctrl.getScoreSheetFile());
        studentResultTemplateField.setText(ctrl.getStudentResultTemplateFile());
        modelFolderField.setText(ctrl.getModelFolder());
        settingsMessage.setText(" ");
    }

    public JPanel createSettingsScreen() {
        JPanel page = new JPanel(new BorderLayout(GuiStyle.GAP, GuiStyle.GAP));
        page.setBackground(GuiStyle.BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(GuiStyle.GAP,
                GuiStyle.GAP, GuiStyle.GAP, GuiStyle.GAP));

        page.add(GuiStyle.createTitle("Settings"), BorderLayout.NORTH);

        // GridLayout(0, 1) stacks the rows; each row is its own panel.
        JPanel form =
                new JPanel(new GridLayout(0, 1, GuiStyle.GAP, GuiStyle.GAP));
        form.setBackground(GuiStyle.BACKGROUND);

        submissionsField = new JTextField();
        testerField = new JTextField();
        scoreSheetField = new JTextField();
        studentResultTemplateField = new JTextField();
        modelFolderField = new JTextField();

        // Option 1: submission folder path
        form.add(createRow("Submissions folder", submissionsField,
                GuiStyle.createButton("Browse...",
                        () -> chooseFolder(submissionsField))));

        // Option 2: tester folder path
        form.add(createRow("Tester files folder", testerField,
                GuiStyle.createButton("Browse...",
                        () -> chooseFolder(testerField))));

        // Option 3: pick a .csv file for score sheet
        form.add(createRow("Score sheet (CSV)", scoreSheetField,
                GuiStyle.createButton("Browse...",
                        () -> chooseCsvFile(scoreSheetField))));

        // Option 4: pick a .csv file for student result template
        form.add(createRow("Student Result Template (CSV)",
                studentResultTemplateField,
                GuiStyle.createButton("Browse...",
                        () -> chooseCsvFile(studentResultTemplateField))));

        form.add(createRow("Model Folder for Junk File/Misplaced Fixer",
                modelFolderField,
                GuiStyle.createButton("Browse...",
                        () -> chooseFolder(modelFolderField))));
        // Keep the rows at their normal height (same trick as the sidebar)
        JPanel formHolder = new JPanel(new BorderLayout());
        formHolder.setBackground(GuiStyle.BACKGROUND);
        formHolder.add(form, BorderLayout.NORTH);
        page.add(formHolder, BorderLayout.CENTER);

        // Bottom: Save / Back buttons and a message line
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(GuiStyle.BACKGROUND);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(GuiStyle.BACKGROUND);
        buttons.add(GuiStyle.createButton("Save", () -> saveSettings()));
        buttons.add(GuiStyle.createButton("Back", onBack));

        settingsMessage = new JLabel(" ");
        settingsMessage.setForeground(GuiStyle.TEXT);

        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(settingsMessage, BorderLayout.SOUTH);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // One row: [label] [text field..............] [Browse...]
    // browseButton can be null for rows that do not need one
    private JPanel createRow(String labelText, JTextField field,
            JButton browseButton) {
        JPanel row = new JPanel(new BorderLayout(GuiStyle.GAP, 0));
        row.setBackground(GuiStyle.BACKGROUND);

        JLabel label = new JLabel(labelText);
        label.setForeground(GuiStyle.TEXT);
        label.setFont(GuiStyle.LABEL_FONT);
        // Give every label the same width so the text fields line up
        label.setPreferredSize(new java.awt.Dimension(220, 30));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        if (browseButton != null) {
            row.add(browseButton, BorderLayout.EAST);
        }

        return row;
    }

//-----------------------------------HELPER----------------------------------


    // ---------- File choosers ----------

    // Opens a folder picker, starting from the project folder,
    private void chooseFolder(JTextField field) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void chooseCsvFile(JTextField field) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getPath());
        }
    }

    // ---------- Saving ----------

    // Uses the setters in GradingCtrl. Each setter returns false and keeps
    // the old value if the new one is not valid.
    private void saveSettings() {
        ArrayList<String> errors = new ArrayList<>();

        if (!ctrl.setSubmissionsFolder(submissionsField.getText().trim())) {
            errors.add("submissions folder not found");
        }

        if (!ctrl.setTesterFolder(testerField.getText().trim())) {
            errors.add("tester folder not found");
        }

        if (!ctrl.setScoreSheetFile(scoreSheetField.getText().trim())) {
            errors.add("score sheet is not an existing .csv file");
        }

        String templateFile = studentResultTemplateField.getText().trim();
        if (!ctrl.setStudentResultTemplateFile(templateFile)) {
            errors.add("student result template is not an existing .csv file");
        }

        if (!ctrl.setModelFolder(modelFolderField.getText().trim())) {
            errors.add("model folder not found");
        }

        if (errors.isEmpty()) {
            settingsMessage.setForeground(GuiStyle.SUCCESS);
            settingsMessage.setText("Settings saved.");
        } else {
            settingsMessage.setForeground(GuiStyle.ERROR);
            // The valid settings were still saved; only these were not
            settingsMessage.setText("Not changed: "
                    + String.join(", ", errors));
        }
    }

}
