package com.chronotrack.ui.tabs;

import com.chronotrack.model.HistoryRecord;
import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.components.BadgeRenderer;
import com.chronotrack.ui.components.ChartsPanel;
import com.chronotrack.ui.components.MetricCard;
import com.chronotrack.ui.components.ModernTable;
import com.chronotrack.ui.theme.ModernTheme;
import com.chronotrack.util.UrlHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Dashboard Tab: Overview of browsing activity with metric cards, charts, and recent activity.
 */
public class DashboardTab extends JPanel {

    private final HistoryService historyService;

    private MetricCard cardTotal;
    private MetricCard cardNormal;
    private MetricCard cardIncognito;
    private MetricCard cardDomains;

    private ChartsPanel chartsPanel;
    private DefaultTableModel recentTableModel;
    private ModernTable recentTable;

    public DashboardTab(HistoryService historyService) {
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. TOP: Metric Cards Grid (1 row, 4 columns)
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsGrid.setOpaque(false);

        cardTotal = new MetricCard("TOTAL VISITS", "0", "All recorded history", "🌐", ModernTheme.PRIMARY);
        cardNormal = new MetricCard("NORMAL MODE", "0", "Standard browsing", "🟢", ModernTheme.NORMAL_TEXT);
        cardIncognito = new MetricCard("INCOGNITO MODE", "0", "Private browsing", "🕶️", ModernTheme.INCOGNITO_TEXT);
        cardDomains = new MetricCard("UNIQUE WEBSITES", "0", "Distinct host domains", "📊", ModernTheme.ACCENT_CYAN);

        metricsGrid.add(cardTotal);
        metricsGrid.add(cardNormal);
        metricsGrid.add(cardIncognito);
        metricsGrid.add(cardDomains);

        add(metricsGrid, BorderLayout.NORTH);

        // 2. CENTER: Split into Charts (Left) and Recent Activity (Right)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centerPanel.setOpaque(false);

        // Left: Visual Charts Card
        JPanel chartCard = ModernTheme.createCardPanel();
        chartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblChartTitle = new JLabel("Activity Visualizations");
        lblChartTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblChartTitle.setForeground(ModernTheme.TEXT_MAIN);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        chartsPanel = new ChartsPanel();
        chartCard.add(chartsPanel, BorderLayout.CENTER);

        centerPanel.add(chartCard);

        // Right: Recent Activity Card
        JPanel recentCard = ModernTheme.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 10));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);

        JLabel lblRecentTitle = new JLabel("Recent Activity");
        lblRecentTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblRecentTitle.setForeground(ModernTheme.TEXT_MAIN);

        JLabel lblHint = new JLabel("Double-click to open in browser");
        lblHint.setFont(ModernTheme.FONT_SMALL);
        lblHint.setForeground(ModernTheme.TEXT_MUTED);

        recentHeader.add(lblRecentTitle, BorderLayout.WEST);
        recentHeader.add(lblHint, BorderLayout.EAST);
        recentCard.add(recentHeader, BorderLayout.NORTH);

        String[] columns = {"Mode", "Title", "Domain", "Time"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new ModernTable(recentTableModel);
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        recentTable.getColumnModel().getColumn(0).setMaxWidth(110);
        recentTable.getColumnModel().getColumn(0).setCellRenderer(new BadgeRenderer());
        recentTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        recentTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(125);

        recentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = recentTable.getSelectedRow();
                    if (row >= 0 && row < historyService.getAllRecords().size()) {
                        HistoryRecord r = historyService.getAllRecords().get(row);
                        UrlHelper.openInBrowser(r.getUrl());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        recentCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(recentCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateView() {
        int total = historyService.getTotalCount();
        int normal = historyService.getNormalCount();
        int incognito = historyService.getIncognitoCount();
        int domains = historyService.getUniqueDomainsCount();

        cardTotal.setValue(String.valueOf(total));
        cardTotal.setSubtitle("All recorded events");

        int normalPct = total > 0 ? (int) Math.round(((double) normal / total) * 100) : 0;
        cardNormal.setValue(String.valueOf(normal));
        cardNormal.setSubtitle(normalPct + "% of total visits");

        int incogPct = total > 0 ? 100 - normalPct : 0;
        cardIncognito.setValue(String.valueOf(incognito));
        cardIncognito.setSubtitle(incogPct + "% of total visits");

        cardDomains.setValue(String.valueOf(domains));
        cardDomains.setSubtitle("Active domains tracked");

        chartsPanel.updateData(normal, incognito, historyService.getTopDomains(6));

        // Update recent activity table (top 15)
        recentTableModel.setRowCount(0);
        List<HistoryRecord> records = historyService.getAllRecords();
        int limit = Math.min(15, records.size());
        for (int i = 0; i < limit; i++) {
            HistoryRecord r = records.get(i);
            recentTableModel.addRow(new Object[]{
                    r.getBrowserMode(),
                    r.getTitle(),
                    r.getDomain(),
                    r.getFormattedVisitTime()
            });
        }
    }
}
