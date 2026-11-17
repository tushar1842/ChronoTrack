package com.chronotrack.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern theme definitions, color palettes, and UI helper methods for ChronoTrack Swing application.
 */
public class ModernTheme {

    // Backgrounds & Surface
    public static final Color BG_APP = new Color(248, 250, 252);        // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(15, 23, 42);       // Slate 900
    public static final Color BG_SIDEBAR_ACTIVE = new Color(30, 41, 59);// Slate 800
    public static final Color BG_HEADER = Color.WHITE;

    // Text
    public static final Color TEXT_MAIN = new Color(15, 23, 42);        // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);    // Slate 100
    public static final Color TEXT_LIGHT_MUTED = new Color(148, 163, 184);

    // Accents & Brand
    public static final Color PRIMARY = new Color(79, 70, 229);         // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);   // Indigo 700
    public static final Color ACCENT_CYAN = new Color(6, 182, 212);     // Cyan 500

    // Mode Badges
    public static final Color NORMAL_BG = new Color(236, 253, 245);     // Emerald 50
    public static final Color NORMAL_TEXT = new Color(5, 150, 105);     // Emerald 600
    public static final Color NORMAL_BORDER = new Color(167, 243, 208);

    public static final Color INCOGNITO_BG = new Color(245, 243, 255);  // Purple 50
    public static final Color INCOGNITO_TEXT = new Color(124, 58, 237); // Purple 600
    public static final Color INCOGNITO_BORDER = new Color(221, 214, 254);

    // Status & Utility
    public static final Color SUCCESS = new Color(16, 185, 129);        // Emerald 500
    public static final Color DANGER = new Color(239, 68, 68);          // Red 500
    public static final Color WARNING = new Color(245, 158, 11);        // Amber 500
    public static final Color BORDER = new Color(226, 232, 240);        // Slate 200
    public static final Color BORDER_DARK = new Color(51, 65, 85);      // Slate 700

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private ModernTheme() {}

    /**
     * Creates a styled card container panel.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    /**
     * Creates a primary filled action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });

        return btn;
    }

    /**
     * Creates a secondary outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_APP);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    /**
     * Creates a styled text field.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return tf;
    }

    /**
     * Applies antialiasing hints to graphics context.
     */
    public static void applyAntialiasing(Graphics g) {
        if (g instanceof Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
    }
}
