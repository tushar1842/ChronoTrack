package com.chronotrack.ui.dialogs;

import com.chronotrack.model.HistoryRecord;
import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.theme.ModernTheme;
import com.chronotrack.util.UrlHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Dialog for manually adding a browsing record to ChronoTrack.
 */
public class AddRecordDialog extends JDialog {

    private final HistoryService historyService;
    private JTextField txtUrl;
    private JTextField txtTitle;
    private JRadioButton rbNormal;
    private JRadioButton rbIncognito;

    public AddRecordDialog(Frame parent, HistoryService historyService) {
        super(parent, "Add Browsing History Entry", true);
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setSize(460, 320);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ModernTheme.BG_CARD);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblTitleHeader = new JLabel("Log Website Visit");
        lblTitleHeader.setFont(ModernTheme.FONT_SUBTITLE);
        lblTitleHeader.setForeground(ModernTheme.TEXT_MAIN);

        // URL Field
        JLabel lblUrl = new JLabel("URL (Required):");
        lblUrl.setFont(ModernTheme.FONT_BOLD);
        lblUrl.setForeground(ModernTheme.TEXT_MUTED);

        txtUrl = ModernTheme.createTextField(24);
        txtUrl.setText("https://");

        // Page Title Field
        JLabel lblTitle = new JLabel("Page Title (Optional):");
        lblTitle.setFont(ModernTheme.FONT_BOLD);
        lblTitle.setForeground(ModernTheme.TEXT_MUTED);

        txtTitle = ModernTheme.createTextField(24);

        // Mode Radio buttons
        JLabel lblMode = new JLabel("Browsing Mode:");
        lblMode.setFont(ModernTheme.FONT_BOLD);
        lblMode.setForeground(ModernTheme.TEXT_MUTED);

        rbNormal = new JRadioButton("Normal Browsing", true);
        rbNormal.setFont(ModernTheme.FONT_REGULAR);
        rbNormal.setOpaque(false);

        rbIncognito = new JRadioButton("Incognito Mode", false);
        rbIncognito.setFont(ModernTheme.FONT_REGULAR);
        rbIncognito.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbNormal);
        bg.add(rbIncognito);

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        modeRow.setOpaque(false);
        modeRow.add(rbNormal);
        modeRow.add(rbIncognito);

        // Action Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ModernTheme.createPrimaryButton("Save Record");
        btnSave.addActionListener(e -> saveRecord());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        // Assembly
        panel.add(lblTitleHeader);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lblUrl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(txtUrl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(txtTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblMode);
        panel.add(Box.createVerticalStrut(4));
        panel.add(modeRow);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnRow);

        setContentPane(panel);
    }

    private void saveRecord() {
        String url = txtUrl.getText().trim();
        if (url.isBlank() || !UrlHelper.isValidHttpUrl(url)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid HTTP/HTTPS URL (e.g. https://example.com)", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        if (title.isBlank()) {
            title = UrlHelper.extractDomain(url);
        }

        String mode = rbIncognito.isSelected() ? "incognito" : "normal";
        HistoryRecord record = new HistoryRecord(UUID.randomUUID().toString(), url, UrlHelper.extractDomain(url), title, mode, Instant.now());

        historyService.addRecord(record);
        dispose();
    }
}
