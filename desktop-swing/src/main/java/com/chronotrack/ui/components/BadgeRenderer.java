package com.chronotrack.ui.components;

import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer for Browser Mode with color-coded pill badge.
 */
public class BadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        String mode = (value != null) ? value.toString() : "normal";
        boolean isIncognito = "incognito".equalsIgnoreCase(mode);

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ModernTheme.applyAntialiasing(g);

                Color bg = isIncognito ? ModernTheme.INCOGNITO_BG : ModernTheme.NORMAL_BG;
                Color border = isIncognito ? ModernTheme.INCOGNITO_BORDER : ModernTheme.NORMAL_BORDER;

                int w = getWidth() - 16;
                int h = getHeight() - 10;
                int x = 8;
                int y = 5;

                g.setColor(bg);
                g.fillRoundRect(x, y, w, h, 12, 12);

                g.setColor(border);
                g.drawRoundRect(x, y, w, h, 12, 12);
            }
        };

        panel.setOpaque(isSelected);
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        }

        JLabel label = new JLabel(isIncognito ? "🕶️ Incognito" : "● Normal");
        label.setFont(ModernTheme.FONT_BOLD);
        label.setForeground(isIncognito ? ModernTheme.INCOGNITO_TEXT : ModernTheme.NORMAL_TEXT);

        panel.add(label);
        return panel;
    }
}
