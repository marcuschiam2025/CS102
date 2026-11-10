package gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

// Shared look for every GUI screen: colours, fonts, sizes and
// helpers for making styled components.
// Change a value here and every screen updates.
public class GuiStyle {
    // Colours
    public static final Color BACKGROUND = new Color(20, 28, 82);
    public static final Color PANEL = new Color(199, 147, 29);
    public static final Color BUTTON = new Color(138, 112, 76);
    public static final Color TEXT = Color.WHITE;
    public static final Color ERROR = new Color(255, 90, 90);
    public static final Color SUCCESS = new Color(120, 220, 120);

    // Fonts
    public static final String FONT_NAME = "Arial";
    public static final Font TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 24);
    public static final Font BUTTON_FONT = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font LABEL_FONT = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font OUTPUT_FONT =
            new Font(Font.MONOSPACED, Font.PLAIN, 17);

    // Sizes
    public static final int GAP = 10;

    // Makes a styled button that runs the given code when clicked
    public static JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFont(BUTTON_FONT);
        button.addActionListener(event -> action.run());
        return button;
    }

    // Makes a big white title label
    public static JLabel createTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT);
        return title;
    }
}
