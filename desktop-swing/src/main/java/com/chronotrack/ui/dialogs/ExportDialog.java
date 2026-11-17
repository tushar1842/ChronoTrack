package com.chronotrack.ui.dialogs;

import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Dialog for exporting ChronoTrack history to CSV or JSON.
 */
public class ExportDialog extends JDialog {

    private final HistoryService historyService;
    private JRadioButton rbCsv;
    private JRadioButton rbJson;

    public ExportDialog(Frame parent, HistoryService historyService) {
        super(parent, "Export Browsing History", true);
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setSize(400, 240);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ModernTheme.BG_CARD);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitle = new JLabel("Export Format");
        lblTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblTitle.setForeground(ModernTheme.TEXT_MAIN);

        JLabel lblDesc = new JLabel("Choose file format to export " + historyService.getTotalCount() + " records:");
        lblDesc.setFont(ModernTheme.FONT_REGULAR);
        lblDesc.setForeground(ModernTheme.TEXT_MUTED);

        rbCsv = new JRadioButton("CSV (Comma Separated Values - Excel)", true);
        rbCsv.setFont(ModernTheme.FONT_REGULAR);
        rbCsv.setOpaque(false);

        rbJson = new JRadioButton("JSON (Standard ChronoTrack Data)", false);
        rbJson.setFont(ModernTheme.FONT_REGULAR);
        rbJson.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbCsv);
        bg.add(rbJson);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnChoose = ModernTheme.createPrimaryButton("Choose File & Export");
        btnChoose.addActionListener(e -> performExport());

        btnRow.add(btnCancel);
        btnRow.add(btnChoose);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblDesc);
        panel.add(Box.createVerticalStrut(14));
        panel.add(rbCsv);
        panel.add(Box.createVerticalStrut(6));
        panel.add(rbJson);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnRow);

        setContentPane(panel);
    }

    private void performExport() {
        boolean isCsv = rbCsv.isSelected();
        String ext = isCsv ? "csv" : "json";
        String desc = isCsv ? "CSV Files (*.csv)" : "JSON Files (*.json)";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Exported History");
        chooser.setSelectedFile(new File("chronotrack_export." + ext));
        chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));

        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith("." + ext)) {
                file = new File(file.getParentFile(), file.getName() + "." + ext);
            }

            try {
                if (isCsv) {
                    historyService.exportToCsv(file);
                } else {
                    historyService.exportToJson(file);
                }
                JOptionPane.showMessageDialog(this,
                        "Successfully exported " + historyService.getTotalCount() + " records to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
