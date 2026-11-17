package com.chronotrack.ui.components;

import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Custom styled JTable with modern look, zebra striping, and polished headers.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setFont(ModernTheme.FONT_REGULAR);
        setRowHeight(38);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setGridColor(new Color(241, 245, 249)); // Slate 100
        setSelectionBackground(new Color(238, 242, 255)); // Indigo 50
        setSelectionForeground(ModernTheme.PRIMARY);
        setFillsViewportHeight(true);
        setBackground(Color.WHITE);

        // Header styling
        JTableHeader header = getTableHeader();
        header.setFont(ModernTheme.FONT_BOLD);
        header.setForeground(ModernTheme.TEXT_MUTED);
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(ModernTheme.FONT_BOLD);
                l.setForeground(ModernTheme.TEXT_MUTED);
                l.setBackground(new Color(248, 250, 252));
                l.setBorder(new EmptyBorder(8, 12, 8, 12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        });

        // Default cell renderer with padding
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBorder(new EmptyBorder(4, 12, 4, 12));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 254));
                    c.setForeground(ModernTheme.TEXT_MAIN);
                } else {
                    c.setBackground(new Color(238, 242, 255));
                    c.setForeground(ModernTheme.PRIMARY);
                }
                return c;
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (isRowSelected(row)) {
            c.setBackground(new Color(238, 242, 255));
        } else {
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 254));
        }
        return c;
    }
}
