package com.chronotrack.ui.components;

import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Metric Card showing a key statistic (Total Visits, Normal, Incognito, Domains).
 */
public class MetricCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final JLabel lblIcon;

    public MetricCard(String title, String initialValue, String subtitle, String iconSymbol, Color accentColor) {
        setLayout(new BorderLayout(12, 8));
        setBackground(ModernTheme.BG_CARD);
        setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Left info container
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(ModernTheme.FONT_BOLD);
        lblTitle.setForeground(ModernTheme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(ModernTheme.FONT_METRIC);
        lblValue.setForeground(ModernTheme.TEXT_MAIN);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(ModernTheme.FONT_SMALL);
        lblSubtitle.setForeground(ModernTheme.TEXT_MUTED);

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblValue);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblSubtitle);

        add(infoPanel, BorderLayout.CENTER);

        // Right icon container
        JPanel iconWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ModernTheme.applyAntialiasing(g);
                g.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 28));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(50, 50));

        lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(accentColor);
        iconWrapper.add(lblIcon);

        add(iconWrapper, BorderLayout.EAST);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }
}
