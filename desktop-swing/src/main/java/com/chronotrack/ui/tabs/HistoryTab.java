package com.chronotrack.ui.tabs;

import com.chronotrack.model.FilterCriteria;
import com.chronotrack.model.HistoryRecord;
import com.chronotrack.service.HistoryService;
import com.chronotrack.ui.components.BadgeRenderer;
import com.chronotrack.ui.components.ModernTable;
import com.chronotrack.ui.dialogs.ExportDialog;
import com.chronotrack.ui.theme.ModernTheme;
import com.chronotrack.util.UrlHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Full History Explorer Tab with search, filters, pagination/scrolling, context menu, and actions.
 */
public class HistoryTab extends JPanel {

    private final HistoryService historyService;

    private JTextField txtSearch;
    private JComboBox<String> cmbMode;
    private JComboBox<String> cmbDomain;
    private JLabel lblStatusCount;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<HistoryRecord> displayedRecords = new ArrayList<>();

    public HistoryTab(HistoryService historyService) {
        this.historyService = historyService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(ModernTheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. TOP: Filter Card
        JPanel filterCard = ModernTheme.createCardPanel();
        filterCard.setLayout(new BorderLayout(12, 10));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);

        // Search Input
        JLabel lblSearch = new JLabel("🔍 Search:");
        lblSearch.setFont(ModernTheme.FONT_BOLD);
        lblSearch.setForeground(ModernTheme.TEXT_MUTED);
        txtSearch = ModernTheme.createTextField(18);
        txtSearch.setToolTipText("Filter by Title, URL or Domain");

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Mode Dropdown
        JLabel lblMode = new JLabel("Mode:");
        lblMode.setFont(ModernTheme.FONT_BOLD);
        lblMode.setForeground(ModernTheme.TEXT_MUTED);
        cmbMode = new JComboBox<>(new String[]{"All Modes", "Normal Only", "Incognito Only"});
        cmbMode.setFont(ModernTheme.FONT_REGULAR);
        cmbMode.setBackground(Color.WHITE);
        cmbMode.addActionListener(e -> applyFilters());

        // Domain Dropdown
        JLabel lblDomain = new JLabel("Domain:");
        lblDomain.setFont(ModernTheme.FONT_BOLD);
        lblDomain.setForeground(ModernTheme.TEXT_MUTED);
        cmbDomain = new JComboBox<>(new String[]{"All Domains"});
        cmbDomain.setFont(ModernTheme.FONT_REGULAR);
        cmbDomain.setBackground(Color.WHITE);
        cmbDomain.addActionListener(e -> applyFilters());

        // Reset button
        JButton btnReset = ModernTheme.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbMode.setSelectedIndex(0);
            cmbDomain.setSelectedIndex(0);
            applyFilters();
        });

        filterRow.add(lblSearch);
        filterRow.add(txtSearch);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(lblMode);
        filterRow.add(cmbMode);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(lblDomain);
        filterRow.add(cmbDomain);
        filterRow.add(btnReset);

        // Right side action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        JButton btnOpen = ModernTheme.createSecondaryButton("🌐 Open URL");
        btnOpen.addActionListener(e -> openSelectedUrl());

        JButton btnDelete = ModernTheme.createSecondaryButton("🗑️ Delete");
        btnDelete.addActionListener(e -> deleteSelectedRecord());

        JButton btnExport = ModernTheme.createSecondaryButton("📥 Export");
        btnExport.addActionListener(e -> openExportDialog());

        JButton btnClear = ModernTheme.createSecondaryButton("⚠️ Clear All");
        btnClear.setForeground(ModernTheme.DANGER);
        btnClear.addActionListener(e -> clearAllHistory());

        actionRow.add(btnOpen);
        actionRow.add(btnDelete);
        actionRow.add(btnExport);
        actionRow.add(btnClear);

        filterCard.add(filterRow, BorderLayout.WEST);
        filterCard.add(actionRow, BorderLayout.EAST);

        add(filterCard, BorderLayout.NORTH);

        // 2. CENTER: Full History Table
        JPanel tableCard = ModernTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] cols = {"#", "Mode", "Domain", "Title", "URL", "Visit Time"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(105);
        table.getColumnModel().getColumn(1).setMaxWidth(120);
        table.getColumnModel().getColumn(1).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        // Right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem itemOpen = new JMenuItem("Open in Browser");
        itemOpen.addActionListener(e -> openSelectedUrl());
        JMenuItem itemCopy = new JMenuItem("Copy URL");
        itemCopy.addActionListener(e -> copySelectedUrl());
        JMenuItem itemDel = new JMenuItem("Delete Entry");
        itemDel.addActionListener(e -> deleteSelectedRecord());

        contextMenu.add(itemOpen);
        contextMenu.add(itemCopy);
        contextMenu.addSeparator();
        contextMenu.add(itemDel);

        table.setComponentPopupMenu(contextMenu);

        // Double click
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedUrl();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom status row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        lblStatusCount = new JLabel("Showing 0 of 0 records");
        lblStatusCount.setFont(ModernTheme.FONT_SMALL);
        lblStatusCount.setForeground(ModernTheme.TEXT_MUTED);

        bottomRow.add(lblStatusCount, BorderLayout.WEST);
        tableCard.add(bottomRow, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    public void updateView() {
        updateDomainDropdown();
        applyFilters();
    }

    private void updateDomainDropdown() {
        String currentSelection = (String) cmbDomain.getSelectedItem();
        List<String> domains = historyService.getAllUniqueDomains();

        cmbDomain.removeAllItems();
        cmbDomain.addItem("All Domains");
        for (String d : domains) {
            cmbDomain.addItem(d);
        }

        if (currentSelection != null) {
            cmbDomain.setSelectedItem(currentSelection);
        }
    }

    private void applyFilters() {
        String kw = txtSearch.getText();
        String modeSelection = (String) cmbMode.getSelectedItem();
        String domainSelection = (String) cmbDomain.getSelectedItem();

        String mode = "all";
        if ("Normal Only".equalsIgnoreCase(modeSelection)) mode = "normal";
        else if ("Incognito Only".equalsIgnoreCase(modeSelection)) mode = "incognito";

        String domain = ("All Domains".equalsIgnoreCase(domainSelection) || domainSelection == null) ? "all" : domainSelection;

        FilterCriteria criteria = new FilterCriteria(kw, mode, domain);
        displayedRecords = historyService.getFilteredRecords(criteria);

        tableModel.setRowCount(0);
        int index = 1;
        for (HistoryRecord r : displayedRecords) {
            tableModel.addRow(new Object[]{
                    index++,
                    r.getBrowserMode(),
                    r.getDomain(),
                    r.getTitle(),
                    r.getUrl(),
                    r.getFormattedVisitTime()
            });
        }

        lblStatusCount.setText(String.format("Showing %d of %d records", displayedRecords.size(), historyService.getTotalCount()));
    }

    private HistoryRecord getSelectedRecord() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedRecords.size()) {
            return displayedRecords.get(row);
        }
        return null;
    }

    private void openSelectedUrl() {
        HistoryRecord r = getSelectedRecord();
        if (r != null && r.getUrl() != null) {
            UrlHelper.openInBrowser(r.getUrl());
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copySelectedUrl() {
        HistoryRecord r = getSelectedRecord();
        if (r != null && r.getUrl() != null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(r.getUrl()), null);
            JOptionPane.showMessageDialog(this, "URL copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedRecord() {
        HistoryRecord r = getSelectedRecord();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete visit to \"" + r.getTitle() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            historyService.deleteRecord(r.getId());
            applyFilters();
        }
    }

    private void clearAllHistory() {
        if (historyService.getTotalCount() == 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete ALL browsing history?",
                "Confirm Clear All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            historyService.clearAll();
            applyFilters();
        }
    }

    private void openExportDialog() {
        ExportDialog dlg = new ExportDialog((Frame) SwingUtilities.getWindowAncestor(this), historyService);
        dlg.setVisible(true);
    }
}
