package com.chronotrack.ui.components;

import com.chronotrack.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Custom Swing component that renders modern charts using Java2D:
 * - Donut Chart for Normal vs Incognito mode distribution
 * - Horizontal Bar Chart for Top Visited Domains
 */
public class ChartsPanel extends JPanel {

    private int normalCount = 0;
    private int incognitoCount = 0;
    private Map<String, Integer> topDomains = Map.of();

    public ChartsPanel() {
        setBackground(ModernTheme.BG_CARD);
        setOpaque(true);
    }

    public void updateData(int normal, int incognito, Map<String, Integer> topDomains) {
        this.normalCount = normal;
        this.incognitoCount = incognito;
        this.topDomains = (topDomains != null) ? topDomains : Map.of();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        ModernTheme.applyAntialiasing(g2);

        int total = normalCount + incognitoCount;
        int width = getWidth();
        int height = getHeight();

        if (total == 0) {
            g2.setColor(ModernTheme.TEXT_MUTED);
            g2.setFont(ModernTheme.FONT_SUBTITLE);
            String msg = "No browsing history to visualize yet.";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (width - fm.stringWidth(msg)) / 2;
            int ty = (height / 2);
            g2.drawString(msg, tx, ty);
            return;
        }

        int halfWidth = width / 2;

        // --- LEFT HALF: DONUT CHART ---
        paintDonutChart(g2, 20, 20, halfWidth - 40, height - 40, total);

        // Divider
        g2.setColor(ModernTheme.BORDER);
        g2.drawLine(halfWidth, 20, halfWidth, height - 20);

        // --- RIGHT HALF: TOP DOMAINS BAR CHART ---
        paintBarChart(g2, halfWidth + 20, 20, halfWidth - 40, height - 40);
    }

    private void paintDonutChart(Graphics2D g2, int x, int y, int w, int h, int total) {
        // Title
        g2.setColor(ModernTheme.TEXT_MAIN);
        g2.setFont(ModernTheme.FONT_SUBTITLE);
        g2.drawString("Mode Distribution", x, y + 16);

        int chartSize = Math.min(w, h - 80);
        chartSize = Math.max(120, chartSize);
        int cx = x + (w - chartSize) / 2;
        int cy = y + 36;

        double normalAngle = (double) normalCount / total * 360.0;
        double incognitoAngle = 360.0 - normalAngle;

        // Draw Normal Arc
        g2.setColor(ModernTheme.NORMAL_TEXT);
        g2.setStroke(new BasicStroke(24, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(cx + 14, cy + 14, chartSize - 28, chartSize - 28, 90, -normalAngle, Arc2D.OPEN));

        // Draw Incognito Arc
        if (incognitoCount > 0) {
            g2.setColor(ModernTheme.INCOGNITO_TEXT);
            g2.draw(new Arc2D.Double(cx + 14, cy + 14, chartSize - 28, chartSize - 28, 90 - normalAngle, -incognitoAngle, Arc2D.OPEN));
        }

        // Center Text: Total
        g2.setFont(ModernTheme.FONT_METRIC);
        g2.setColor(ModernTheme.TEXT_MAIN);
        String totalStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr, cx + (chartSize - fm.stringWidth(totalStr)) / 2, cy + chartSize / 2 + 6);

        g2.setFont(ModernTheme.FONT_SMALL);
        g2.setColor(ModernTheme.TEXT_MUTED);
        String sub = "Visits";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, cx + (chartSize - fm2.stringWidth(sub)) / 2, cy + chartSize / 2 + 22);

        // Legend
        int legendY = cy + chartSize + 18;
        int normalPct = (int) Math.round(((double) normalCount / total) * 100);
        int incogPct = 100 - normalPct;

        paintLegendItem(g2, x + 20, legendY, ModernTheme.NORMAL_TEXT, "Normal (" + normalCount + " · " + normalPct + "%)");
        paintLegendItem(g2, x + w / 2 + 10, legendY, ModernTheme.INCOGNITO_TEXT, "Incognito (" + incognitoCount + " · " + incogPct + "%)");
    }

    private void paintLegendItem(Graphics2D g2, int lx, int ly, Color dotColor, String label) {
        g2.setColor(dotColor);
        g2.fillOval(lx, ly, 10, 10);
        g2.setColor(ModernTheme.TEXT_MAIN);
        g2.setFont(ModernTheme.FONT_REGULAR);
        g2.drawString(label, lx + 16, ly + 9);
    }

    private void paintBarChart(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(ModernTheme.TEXT_MAIN);
        g2.setFont(ModernTheme.FONT_SUBTITLE);
        g2.drawString("Top Visited Domains", x, y + 16);

        if (topDomains.isEmpty()) {
            g2.setColor(ModernTheme.TEXT_MUTED);
            g2.setFont(ModernTheme.FONT_REGULAR);
            g2.drawString("No domain data available.", x, y + 50);
            return;
        }

        int maxVisits = topDomains.values().stream().max(Integer::compareTo).orElse(1);
        int barY = y + 40;
        int barHeight = 18;
        int gap = 24;
        int labelWidth = Math.min(130, w / 3);
        int maxBarWidth = w - labelWidth - 60;

        Color barColor = ModernTheme.PRIMARY;

        for (Map.Entry<String, Integer> entry : topDomains.entrySet()) {
            if (barY + barHeight > y + h) break;

            String domain = entry.getKey();
            int count = entry.getValue();
            int barWidth = (int) (((double) count / maxVisits) * maxBarWidth);
            barWidth = Math.max(8, barWidth);

            // Domain label
            g2.setColor(ModernTheme.TEXT_MAIN);
            g2.setFont(ModernTheme.FONT_REGULAR);
            String displayDomain = domain.length() > 18 ? domain.substring(0, 15) + "..." : domain;
            g2.drawString(displayDomain, x, barY + 14);

            // Bar background track
            int bx = x + labelWidth;
            g2.setColor(new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Double(bx, barY, maxBarWidth, barHeight, 8, 8));

            // Bar fill
            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Double(bx, barY, barWidth, barHeight, 8, 8));

            // Count label
            g2.setColor(ModernTheme.TEXT_MUTED);
            g2.setFont(ModernTheme.FONT_SMALL);
            g2.drawString(String.valueOf(count), bx + barWidth + 8, barY + 14);

            barY += gap + 6;
        }
    }
}
