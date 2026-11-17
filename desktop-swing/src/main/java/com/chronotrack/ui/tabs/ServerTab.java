package com.chronotrack.ui.tabs;

import com.chronotrack.service.EmbeddedJavaServer;
import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Server and Configuration tab: manage connection to Node.js backend or run pure Java server.
 */
public class ServerTab extends JPanel {

    private final HistoryService historyService;

    private JTextField txtApiUrl;
    private JLabel lblApiStatus;
    private JButton btnTestConnection;

    private JLabel lblJavaServerStatus;
    private JButton btnToggleJavaServer;
    private JButton btnSampleData;

    public ServerTab(HistoryService historyService) {
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ModernTheme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. External / Node.js Backend Connection Card
        JPanel nodeCard = ModernTheme.createCardPanel();
        nodeCard.setLayout(new BoxLayout(nodeCard, BoxLayout.Y_AXIS));

        JLabel lblNodeTitle = new JLabel("🌐 ChronoTrack REST Backend Connection");
        lblNodeTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblNodeTitle.setForeground(ModernTheme.TEXT_MAIN);

        JLabel lblNodeDesc = new JLabel("Connect to the ChronoTrack Express/Node.js or external API service.");
        lblNodeDesc.setFont(ModernTheme.FONT_SMALL);
        lblNodeDesc.setForeground(ModernTheme.TEXT_MUTED);

        JPanel urlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        urlRow.setOpaque(false);

        JLabel lblUrl = new JLabel("API Base URL:");
        lblUrl.setFont(ModernTheme.FONT_BOLD);
        txtApiUrl = ModernTheme.createTextField(24);
        txtApiUrl.setText(historyService.getApiBaseUrl());

        btnTestConnection = ModernTheme.createPrimaryButton("Test Connection");
        btnTestConnection.addActionListener(e -> testBackendConnection());

        lblApiStatus = new JLabel("Ready");
        lblApiStatus.setFont(ModernTheme.FONT_BOLD);
        lblApiStatus.setForeground(ModernTheme.TEXT_MUTED);

        urlRow.add(lblUrl);
        urlRow.add(txtApiUrl);
        urlRow.add(btnTestConnection);
        urlRow.add(lblApiStatus);

        nodeCard.add(lblNodeTitle);
        nodeCard.add(Box.createVerticalStrut(4));
        nodeCard.add(lblNodeDesc);
        nodeCard.add(Box.createVerticalStrut(10));
        nodeCard.add(urlRow);

        add(nodeCard);
        add(Box.createVerticalStrut(18));

        // 2. Embedded Pure Java Server Card
        JPanel javaServerCard = ModernTheme.createCardPanel();
        javaServerCard.setLayout(new BoxLayout(javaServerCard, BoxLayout.Y_AXIS));

        JLabel lblJavaTitle = new JLabel("☕ Built-in Embedded Java Server");
        lblJavaTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblJavaTitle.setForeground(ModernTheme.TEXT_MAIN);

        JTextArea txtJavaDesc = new JTextArea(
                "Run a native Java HTTP REST backend inside this desktop app! It provides the exact same endpoints\n" +
                "(`GET /api/history`, `POST /api/history`) with full CORS support on port 5000.\n" +
                "When enabled, the ChronoTrack Chrome Extension can stream browsing data straight to Java without Node.js or MongoDB."
        );
        txtJavaDesc.setFont(ModernTheme.FONT_REGULAR);
        txtJavaDesc.setForeground(ModernTheme.TEXT_MUTED);
        txtJavaDesc.setEditable(false);
        txtJavaDesc.setOpaque(false);

        JPanel javaBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        javaBtnRow.setOpaque(false);

        btnToggleJavaServer = ModernTheme.createPrimaryButton("Start Java Server (Port 5000)");
        btnToggleJavaServer.setBackground(ModernTheme.SUCCESS);
        btnToggleJavaServer.addActionListener(e -> toggleJavaServer());

        lblJavaServerStatus = new JLabel("● Server Stopped");
        lblJavaServerStatus.setFont(ModernTheme.FONT_BOLD);
        lblJavaServerStatus.setForeground(ModernTheme.TEXT_MUTED);

        javaBtnRow.add(btnToggleJavaServer);
        javaBtnRow.add(lblJavaServerStatus);

        javaServerCard.add(lblJavaTitle);
        javaServerCard.add(Box.createVerticalStrut(4));
        javaServerCard.add(txtJavaDesc);
        javaServerCard.add(Box.createVerticalStrut(10));
        javaServerCard.add(javaBtnRow);

        add(javaServerCard);
        add(Box.createVerticalStrut(18));

        // 3. Demo Data & Utilities Card
        JPanel utilCard = ModernTheme.createCardPanel();
        utilCard.setLayout(new BoxLayout(utilCard, BoxLayout.Y_AXIS));

        JLabel lblUtilTitle = new JLabel("🛠️ Quick Tools & Test Data");
        lblUtilTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblUtilTitle.setForeground(ModernTheme.TEXT_MAIN);

        JPanel utilBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        utilBtnRow.setOpaque(false);

        btnSampleData = ModernTheme.createSecondaryButton("Generate 10 Sample Visits");
        btnSampleData.addActionListener(e -> {
            historyService.generateSampleData();
            JOptionPane.showMessageDialog(this, "Sample browsing activity generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClear = ModernTheme.createSecondaryButton("Clear All History");
        btnClear.setForeground(ModernTheme.DANGER);
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Clear all history records?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                historyService.clearAll();
            }
        });

        utilBtnRow.add(btnSampleData);
        utilBtnRow.add(btnClear);

        utilCard.add(lblUtilTitle);
        utilCard.add(Box.createVerticalStrut(10));
        utilCard.add(utilBtnRow);

        add(utilCard);

        add(Box.createVerticalGlue());
    }

    private void testBackendConnection() {
        String url = txtApiUrl.getText().trim();
        historyService.setApiBaseUrl(url);
        lblApiStatus.setText("Testing...");
        lblApiStatus.setForeground(ModernTheme.TEXT_MUTED);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return historyService.getApiClient().testConnection(url);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        lblApiStatus.setText("● Connected (200 OK)");
                        lblApiStatus.setForeground(ModernTheme.SUCCESS);
                        historyService.refreshHistory();
                    } else {
                        lblApiStatus.setText("● Unreachable");
                        lblApiStatus.setForeground(ModernTheme.DANGER);
                    }
                } catch (Exception e) {
                    lblApiStatus.setText("● Error: " + e.getMessage());
                    lblApiStatus.setForeground(ModernTheme.DANGER);
                }
            }
        };
        worker.execute();
    }

    private void toggleJavaServer() {
        EmbeddedJavaServer s = historyService.getEmbeddedServer();
        if (s.isRunning()) {
            s.stop();
            historyService.setUseEmbeddedServer(false);
            btnToggleJavaServer.setText("Start Java Server (Port 5000)");
            btnToggleJavaServer.setBackground(ModernTheme.SUCCESS);
            lblJavaServerStatus.setText("● Server Stopped");
            lblJavaServerStatus.setForeground(ModernTheme.TEXT_MUTED);
        } else {
            try {
                s.start();
                historyService.setUseEmbeddedServer(true);
                historyService.refreshHistory();
                btnToggleJavaServer.setText("Stop Java Server");
                btnToggleJavaServer.setBackground(ModernTheme.DANGER);
                lblJavaServerStatus.setText("● Active on http://127.0.0.1:" + s.getPort());
                lblJavaServerStatus.setForeground(ModernTheme.SUCCESS);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to start Java Server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateView() {
        EmbeddedJavaServer s = historyService.getEmbeddedServer();
        if (s.isRunning()) {
            btnToggleJavaServer.setText("Stop Java Server");
            btnToggleJavaServer.setBackground(ModernTheme.DANGER);
            lblJavaServerStatus.setText("● Active on http://127.0.0.1:" + s.getPort());
            lblJavaServerStatus.setForeground(ModernTheme.SUCCESS);
        } else {
            btnToggleJavaServer.setText("Start Java Server (Port 5000)");
            btnToggleJavaServer.setBackground(ModernTheme.SUCCESS);
            lblJavaServerStatus.setText("● Server Stopped");
            lblJavaServerStatus.setForeground(ModernTheme.TEXT_MUTED);
        }
    }
}
