package com.chronotrack.ui;

import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.dialogs.AddRecordDialog;
import com.chronotrack.ui.tabs.AnalyticsTab;
import com.chronotrack.ui.tabs.DashboardTab;
import com.chronotrack.ui.tabs.HistoryTab;
import com.chronotrack.ui.tabs.ServerTab;
import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application frame containing the top navigation header and tabbed interface.
 */
public class MainFrame extends JFrame {

    private final HistoryService historyService;

    private JLabel lblStatusPill;
    private JCheckBox chkAutoRefresh;
    private Timer autoRefreshTimer;

    private DashboardTab dashboardTab;
    private HistoryTab historyTab;
    private AnalyticsTab analyticsTab;
    private ServerTab serverTab;
    private JTabbedPane tabbedPane;

    public MainFrame(HistoryService historyService) {
        this.historyService = historyService;
        initFrame();
        initUI();
        initAutoRefresh();

        // Listen for data changes to auto-update active tabs
        historyService.addListener(records -> SwingUtilities.invokeLater(this::updateAllTabs));

        // Initial fetch
        refreshData();
    }

    private void initFrame() {
        setTitle("ChronoTrack — Smart Browser Activity Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 740);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernTheme.BG_APP);

        // Graceful shutdown of embedded server if running
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (historyService.getEmbeddedServer().isRunning()) {
                    historyService.getEmbeddedServer().stop();
                }
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setBackground(ModernTheme.BG_HEADER);
        headerBar.setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("⚡");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblAppTitle = new JLabel("ChronoTrack");
        lblAppTitle.setFont(ModernTheme.FONT_TITLE);
        lblAppTitle.setForeground(ModernTheme.PRIMARY);

        JLabel lblAppSubtitle = new JLabel("Browser Activity & History Monitor · Java Swing");
        lblAppSubtitle.setFont(ModernTheme.FONT_SMALL);
        lblAppSubtitle.setForeground(ModernTheme.TEXT_MUTED);

        titleBox.add(lblAppTitle);
        titleBox.add(lblAppSubtitle);

        brandPanel.add(lblLogo);
        brandPanel.add(titleBox);

        // Center Status Pill
        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        statusWrapper.setOpaque(false);

        lblStatusPill = new JLabel(" Connecting... ");
        lblStatusPill.setFont(ModernTheme.FONT_BOLD);
        lblStatusPill.setForeground(ModernTheme.TEXT_MUTED);
        lblStatusPill.setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        statusWrapper.add(lblStatusPill);

        // Right Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        controlsPanel.setOpaque(false);

        chkAutoRefresh = new JCheckBox("Auto-Refresh (5s)", true);
        chkAutoRefresh.setFont(ModernTheme.FONT_REGULAR);
        chkAutoRefresh.setForeground(ModernTheme.TEXT_MAIN);
        chkAutoRefresh.setOpaque(false);
        chkAutoRefresh.addActionListener(e -> {
            if (chkAutoRefresh.isSelected()) {
                autoRefreshTimer.start();
            } else {
                autoRefreshTimer.stop();
            }
        });

        JButton btnRefresh = ModernTheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        JButton btnAddRecord = ModernTheme.createPrimaryButton("➕ Log Visit");
        btnAddRecord.addActionListener(e -> {
            AddRecordDialog dlg = new AddRecordDialog(this, historyService);
            dlg.setVisible(true);
        });

        controlsPanel.add(chkAutoRefresh);
        controlsPanel.add(btnRefresh);
        controlsPanel.add(btnAddRecord);

        headerBar.add(brandPanel, BorderLayout.WEST);
        headerBar.add(statusWrapper, BorderLayout.CENTER);
        headerBar.add(controlsPanel, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // 2. CENTER TABBED PANE
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);
        tabbedPane.setBackground(ModernTheme.BG_APP);

        dashboardTab = new DashboardTab(historyService);
        historyTab = new HistoryTab(historyService);
        analyticsTab = new AnalyticsTab(historyService);
        serverTab = new ServerTab(historyService);

        tabbedPane.addTab("📊  Dashboard", dashboardTab);
        tabbedPane.addTab("📋  History Explorer", historyTab);
        tabbedPane.addTab("📈  Analytics & Charts", analyticsTab);
        tabbedPane.addTab("⚙️  Server & Setup", serverTab);

        tabbedPane.addChangeListener(e -> updateActiveTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initAutoRefresh() {
        autoRefreshTimer = new Timer(5000, e -> refreshData());
        autoRefreshTimer.start();
    }

    public void refreshData() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return historyService.refreshHistory();
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    updateStatusPill(success);
                    updateAllTabs();
                } catch (Exception ignored) {
                    updateStatusPill(false);
                }
            }
        };
        worker.execute();
    }

    private void updateStatusPill(boolean connected) {
        if (historyService.isUseEmbeddedServer() && historyService.getEmbeddedServer().isRunning()) {
            lblStatusPill.setText("● Java Server Active (Port 5000)");
            lblStatusPill.setForeground(ModernTheme.SUCCESS);
        } else if (connected) {
            lblStatusPill.setText("● Connected to Backend (Port 5000)");
            lblStatusPill.setForeground(ModernTheme.SUCCESS);
        } else {
            lblStatusPill.setText("○ Backend Offline / Local Cache");
            lblStatusPill.setForeground(ModernTheme.WARNING);
        }
    }

    private void updateAllTabs() {
        dashboardTab.updateView();
        historyTab.updateView();
        analyticsTab.updateView();
        serverTab.updateView();
    }

    private void updateActiveTab() {
        Component selected = tabbedPane.getSelectedComponent();
        if (selected == dashboardTab) dashboardTab.updateView();
        else if (selected == historyTab) historyTab.updateView();
        else if (selected == analyticsTab) analyticsTab.updateView();
        else if (selected == serverTab) serverTab.updateView();
    }
}
