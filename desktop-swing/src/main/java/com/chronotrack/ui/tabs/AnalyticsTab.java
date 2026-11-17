package com.chronotrack.ui.tabs;

import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.components.ChartsPanel;
import com.chronotrack.ui.components.MetricCard;
import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Analytics Tab: In-depth charts and analytics breakdown.
 */
public class AnalyticsTab extends JPanel {

    private final HistoryService historyService;

    private MetricCard cardPrivacy;
    private MetricCard cardTopDomain;
    private ChartsPanel fullChartsPanel;
    private DefaultTableModel domainStatsModel;

    public AnalyticsTab(HistoryService historyService) {
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. TOP: Summary KPIs
        JPanel topGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        topGrid.setOpaque(false);

        cardPrivacy = new MetricCard("PRIVACY RATIO", "0%", "Incognito browsing share", "🛡️", ModernTheme.INCOGNITO_TEXT);
        cardTopDomain = new MetricCard("MOST VISITED DOMAIN", "-", "Highest frequency site", "⭐", ModernTheme.PRIMARY);

        topGrid.add(cardPrivacy);
        topGrid.add(cardTopDomain);

        add(topGrid, BorderLayout.NORTH);

        // 2. CENTER: Split view with Full Charts (Left) and Domain Frequency Table (Right)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left: Visual Charts Card
        JPanel chartCard = ModernTheme.createCardPanel();
        chartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblChart = new JLabel("Mode & Domain Analytics");
        lblChart.setFont(ModernTheme.FONT_SUBTITLE);
        lblChart.setForeground(ModernTheme.TEXT_MAIN);
        chartCard.add(lblChart, BorderLayout.NORTH);

        fullChartsPanel = new ChartsPanel();
        chartCard.add(fullChartsPanel, BorderLayout.CENTER);

        centerGrid.add(chartCard);

        // Right: Detailed Domain Ranking Table
        JPanel domainCard = ModernTheme.createCardPanel();
        domainCard.setLayout(new BorderLayout(0, 10));

        JLabel lblDomainTable = new JLabel("Domain Breakdown");
        lblDomainTable.setFont(ModernTheme.FONT_SUBTITLE);
        lblDomainTable.setForeground(ModernTheme.TEXT_MAIN);
        domainCard.add(lblDomainTable, BorderLayout.NORTH);

        String[] cols = {"Rank", "Domain", "Visits", "Share (%)"};
        domainStatsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable domainTable = new JTable(domainStatsModel);
        domainTable.setFont(ModernTheme.FONT_REGULAR);
        domainTable.setRowHeight(32);
        domainTable.setShowHorizontalLines(true);
        domainTable.setShowVerticalLines(false);
        domainTable.setGridColor(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(domainTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        domainCard.add(scrollPane, BorderLayout.CENTER);

        centerGrid.add(domainCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    public void updateView() {
        int total = historyService.getTotalCount();
        int normal = historyService.getNormalCount();
        int incognito = historyService.getIncognitoCount();

        int incogPct = total > 0 ? (int) Math.round(((double) incognito / total) * 100) : 0;
        cardPrivacy.setValue(incogPct + "%");
        cardPrivacy.setSubtitle(incognito + " of " + total + " visits were incognito");

        Map<String, Integer> topDomains = historyService.getTopDomains(15);
        if (!topDomains.isEmpty()) {
            Map.Entry<String, Integer> first = topDomains.entrySet().iterator().next();
            cardTopDomain.setValue(first.getKey());
            cardTopDomain.setSubtitle(first.getValue() + " visits recorded");
        } else {
            cardTopDomain.setValue("-");
            cardTopDomain.setSubtitle("No domain data");
        }

        fullChartsPanel.updateData(normal, incognito, topDomains);

        // Populate table
        domainStatsModel.setRowCount(0);
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topDomains.entrySet()) {
            int visits = entry.getValue();
            int pct = total > 0 ? (int) Math.round(((double) visits / total) * 100) : 0;
            domainStatsModel.addRow(new Object[]{
                    "#" + rank++,
                    entry.getKey(),
                    visits,
                    pct + "%"
            });
        }
    }
}
