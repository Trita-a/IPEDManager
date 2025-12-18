package it.ipedmanager.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import it.ipedmanager.ui.config.VectorIcon;

/**
 * Helper class for creating themed dialogs with consistent styling.
 * All dialogs have: white background, black title bar text, buttons aligned
 * right.
 */
public class DialogHelper {

    private static final Color PRIMARY_BLUE = new Color(45, 85, 145);
    private static final Color TEXT_DARK = new Color(50, 50, 50);
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color SUCCESS_GREEN = new Color(46, 125, 50);
    private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color ERROR_RED = new Color(183, 28, 28);

    /**
     * Shows an information dialog with themed styling.
     */
    public static void showInfo(Component parent, String title, String message) {
        showDialog(parent, title, message, "info", PRIMARY_BLUE, new String[] { "OK" }, null);
    }

    /**
     * Shows an info dialog with a custom icon type.
     */
    public static void showInfo(Component parent, String title, String message, String iconType) {
        showDialog(parent, title, message, iconType, PRIMARY_BLUE, new String[] { "OK" }, null);
    }

    /**
     * Shows a success dialog.
     */
    public static void showSuccess(Component parent, String title, String message) {
        showDialog(parent, title, message, "check", SUCCESS_GREEN, new String[] { "OK" }, null);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarning(Component parent, String title, String message) {
        showDialog(parent, title, message, "alert", WARNING_ORANGE, new String[] { "OK" }, null);
    }

    /**
     * Shows an error dialog.
     */
    public static void showError(Component parent, String title, String message) {
        showDialog(parent, title, message, "x", ERROR_RED, new String[] { "OK" }, null);
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     * 
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirm(Component parent, String title, String message) {
        return showConfirm(parent, title, message, "help");
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons and custom icon.
     * 
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean showConfirm(Component parent, String title, String message, String iconType) {
        final boolean[] result = { false };
        Color iconColor = iconType.equals("alert") ? WARNING_ORANGE : PRIMARY_BLUE;
        showDialog(parent, title, message, iconType, iconColor, new String[] { "Sì", "No" },
                idx -> result[0] = (idx == 0));
        return result[0];
    }

    /**
     * Shows an input dialog.
     * 
     * @return the input text, or null if cancelled
     */
    public static String showInput(Component parent, String title, String prompt) {
        return showInput(parent, title, prompt, "");
    }

    /**
     * Shows an input dialog with default value.
     * 
     * @return the input text, or null if cancelled
     */
    public static String showInput(Component parent, String title, String prompt, String defaultValue) {
        final String[] result = { null };

        JDialog dialog = createBaseDialog(parent, title);
        JPanel contentPanel = createContentPanel();

        // Icon
        JLabel iconLabel = new JLabel(new VectorIcon("edit", 40, PRIMARY_BLUE));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        contentPanel.add(iconLabel, BorderLayout.WEST);

        // Center panel with prompt and input field
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        JLabel promptLabel = new JLabel(prompt);
        promptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promptLabel.setForeground(TEXT_DARK);
        centerPanel.add(promptLabel, BorderLayout.NORTH);

        JTextField inputField = new JTextField(defaultValue);
        inputField.setPreferredSize(new Dimension(250, 30));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        centerPanel.add(inputField, BorderLayout.CENTER);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = createButton("Annulla", null, false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        JButton okBtn = createButton("OK", PRIMARY_BLUE, true);
        okBtn.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });
        buttonPanel.add(okBtn);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(350, dialog.getHeight()));
        dialog.setLocationRelativeTo(
                parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent));

        // Focus on input field
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                inputField.requestFocusInWindow();
                inputField.selectAll();
            }
        });

        dialog.setVisible(true);
        return result[0];
    }

    // === Private Helper Methods ===

    private static void showDialog(Component parent, String title, String message,
            String iconType, Color iconColor, String[] buttons,
            java.util.function.IntConsumer buttonCallback) {
        JDialog dialog = createBaseDialog(parent, title);
        JPanel contentPanel = createContentPanel();

        // Icon on left
        JLabel iconLabel = new JLabel(new VectorIcon(iconType, 40, iconColor));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        contentPanel.add(iconLabel, BorderLayout.WEST);

        // Calculate dimensions based on text length
        int textLength = message.length();
        int lineCount = message.split("\n").length;

        // Dynamic width: wider for longer text, max 500px
        int dialogWidth = Math.min(500, Math.max(350, 280 + textLength / 3));

        // Dynamic height based on lines and text length
        int textHeight;
        if (textLength < 80 && lineCount <= 2) {
            textHeight = 50; // Short message
        } else if (textLength < 200 && lineCount <= 5) {
            textHeight = 80 + lineCount * 15; // Medium message
        } else if (textLength < 500) {
            textHeight = 120 + lineCount * 12; // Long message
        } else {
            textHeight = Math.min(300, 150 + lineCount * 10); // Very long, capped
        }

        // Dynamic font size: smaller for very long text
        int fontSize;
        if (textLength > 500) {
            fontSize = 11;
        } else if (textLength > 300) {
            fontSize = 12;
        } else {
            fontSize = 13;
        }

        // Text area with dynamic font
        JTextArea area = new JTextArea(message);
        area.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setForeground(TEXT_DARK);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(dialogWidth - 100, textHeight));
        contentPanel.add(scroll, BorderLayout.CENTER);

        // Buttons (aligned right)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        for (int i = 0; i < buttons.length; i++) {
            final int idx = i;
            boolean isPrimary = (i == 0 && buttons.length > 1) || (buttons.length == 1);
            JButton btn = createButton(buttons[i], isPrimary ? PRIMARY_BLUE : null, isPrimary);
            btn.addActionListener(e -> {
                if (buttonCallback != null) {
                    buttonCallback.accept(idx);
                }
                dialog.dispose();
            });
            buttonPanel.add(btn);
        }

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(dialogWidth, dialog.getHeight()));
        dialog.setLocationRelativeTo(
                parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent));
        dialog.setVisible(true);
    }

    private static JDialog createBaseDialog(Component parent, String title) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, title, true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // FlatLaf title bar theming
        dialog.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.BLACK);
        dialog.getRootPane().putClientProperty("JRootPane.titleBarBackground", Color.WHITE);
        return dialog;
    }

    private static JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private static JButton createButton(String text, Color bgColor, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(80, 32));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        if (isPrimary && bgColor != null) {
            btn.setBackground(bgColor);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(240, 240, 240));
            btn.setForeground(TEXT_DARK);
        }
        return btn;
    }
}
