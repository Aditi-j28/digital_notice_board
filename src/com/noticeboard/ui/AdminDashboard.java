package com.noticeboard.ui;

import com.noticeboard.db.NoticeDAO;
import com.noticeboard.model.Notice;
import com.noticeboard.model.User;
import com.noticeboard.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final User currentUser;
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private JTable noticeTable;
    private DefaultTableModel tableModel;
    private List<Notice> notices;
    private JLabel statusLabel;
    private JTextField searchField;

    public AdminDashboard(User user) {
        this.currentUser = user;
        setTitle("Admin Dashboard - Digital Notice Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        initUI();
        loadNotices();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ─── Sidebar ───────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel logoIcon = new JLabel("📋", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel logoText = UITheme.createLabel("Notice Board", UITheme.FONT_HEADING, UITheme.TEXT_WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel adminBadge = UITheme.createLabel("ADMIN PANEL", UITheme.FONT_SMALL, UITheme.ACCENT);
        adminBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminBadge.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));

        JSeparator sep1 = new JSeparator(); sep1.setForeground(new Color(50, 65, 80));
        sep1.setMaximumSize(new Dimension(200, 1));
        sep1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addBtn = createSidebarBtn("➕  Add Notice", UITheme.SUCCESS);
        JButton refreshBtn = createSidebarBtn("🔄  Refresh", UITheme.PRIMARY);
        JButton logoutBtn = createSidebarBtn("🚪  Logout", UITheme.DANGER);

        addBtn.addActionListener(e -> openNoticeDialog(null));
        refreshBtn.addActionListener(e -> loadNotices());
        logoutBtn.addActionListener(e -> logout());

        // User info at bottom
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setOpaque(false);
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 0, 16));
        JLabel userIcon = new JLabel("👤  " + currentUser.getName());
        userIcon.setFont(UITheme.FONT_SMALL);
        userIcon.setForeground(new Color(160, 190, 220));
        userPanel.add(userIcon, BorderLayout.CENTER);

        sidebar.add(logoIcon);
        sidebar.add(logoText);
        sidebar.add(adminBadge);
        sidebar.add(sep1);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(addBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(refreshBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(userPanel);

        // ─── Top bar ───────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        JLabel pageTitle = UITheme.createLabel("Manage Notices", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        searchField = UITheme.createTextField("Search notices...");
        searchField.setPreferredSize(new Dimension(220, 36));
        JButton searchBtn = UITheme.createButton("Search", UITheme.PRIMARY, Color.WHITE);
        searchBtn.addActionListener(e -> doSearch());
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doSearch();
            }
        });
        JButton clearBtn = UITheme.createButton("All", UITheme.TEXT_SECONDARY.brighter(), Color.WHITE);
        clearBtn.addActionListener(e -> loadNotices());

        topRight.add(searchField);
        topRight.add(searchBtn);
        topRight.add(clearBtn);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // ─── Table ─────────────────────────────────────────────────
        String[] cols = {"#", "Title", "Category", "Priority", "Posted By", "Date", "Attachment"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        noticeTable = new JTable(tableModel);
        styleTable();

        JScrollPane tableScroll = new JScrollPane(noticeTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(UITheme.BG_PANEL);

        // ─── Action bar ────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionBar.setBackground(Color.WHITE);
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        statusLabel = UITheme.createLabel("", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        JButton editBtn = UITheme.createButton("✏ Edit", UITheme.WARNING, Color.WHITE);
        JButton deleteBtn = UITheme.createButton("🗑 Delete", UITheme.DANGER, Color.WHITE);
        JButton viewBtn = UITheme.createButton("👁 View", UITheme.PRIMARY_LIGHT, UITheme.TEXT_PRIMARY);

        editBtn.addActionListener(e -> editSelectedNotice());
        deleteBtn.addActionListener(e -> deleteSelectedNotice());
        viewBtn.addActionListener(e -> viewSelectedNotice());

        actionBar.add(statusLabel);
        actionBar.add(Box.createHorizontalStrut(20));
        actionBar.add(viewBtn);
        actionBar.add(editBtn);
        actionBar.add(deleteBtn);

        // ─── Content area ─────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.BG_PANEL);
        content.add(topBar, BorderLayout.NORTH);
        content.add(tableScroll, BorderLayout.CENTER);
        content.add(actionBar, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private JButton createSidebarBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(188, 40));
        btn.setPreferredSize(new Dimension(188, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void styleTable() {
        noticeTable.setRowHeight(40);
        noticeTable.setFont(UITheme.FONT_BODY);
        noticeTable.setShowGrid(false);
        noticeTable.setIntercellSpacing(new Dimension(0, 0));
        noticeTable.setSelectionBackground(new Color(227, 242, 253));
        noticeTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        noticeTable.setBackground(Color.WHITE);

        JTableHeader header = noticeTable.getTableHeader();
        header.setFont(UITheme.FONT_LABEL);
        header.setBackground(UITheme.BG_SIDEBAR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 40));

        // Column widths
        int[] widths = {40, 260, 100, 80, 120, 140, 90};
        for (int i = 0; i < widths.length; i++) {
            noticeTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Priority cell renderer
        noticeTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String p = v == null ? "" : v.toString();
                lbl.setForeground(sel ? UITheme.TEXT_PRIMARY : UITheme.getPriorityColor(p));
                lbl.setFont(UITheme.FONT_LABEL);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // Attachment renderer
        noticeTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                boolean has = "📎 Yes".equals(v);
                lbl.setText(has ? "📎 Yes" : "—");
                lbl.setForeground(has ? UITheme.SUCCESS : UITheme.TEXT_SECONDARY);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });

        // Alternating row renderer for other columns
        DefaultTableCellRenderer altRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return this;
            }
        };
        for (int i : new int[]{0, 1, 2, 4, 5}) {
            noticeTable.getColumnModel().getColumn(i).setCellRenderer(altRenderer);
        }
    }

    private void loadNotices() {
        try {
            notices = noticeDAO.getAllNotices();
            populateTable(notices);
            statusLabel.setText("Total: " + notices.size() + " notice(s)");
        } catch (SQLException e) {
            showError("Failed to load notices: " + e.getMessage());
        }
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadNotices(); return; }
        try {
            notices = noticeDAO.searchNotices(kw);
            populateTable(notices);
            statusLabel.setText("Found: " + notices.size() + " result(s) for \"" + kw + "\"");
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void populateTable(List<Notice> list) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy  HH:mm");
        int idx = 1;
        for (Notice n : list) {
            tableModel.addRow(new Object[]{
                idx++,
                n.getTitle(),
                n.getCategory(),
                n.getPriority(),
                n.getPostedByName() != null ? n.getPostedByName() : "—",
                n.getCreatedAt() != null ? sdf.format(n.getCreatedAt()) : "—",
                n.hasAttachment() ? "📎 Yes" : "No"
            });
        }
    }

    private void openNoticeDialog(Notice existing) {
        NoticeDialog dialog = new NoticeDialog(this, existing, currentUser.getId());
        dialog.setVisible(true);
        if (dialog.isSaved()) loadNotices();
    }

    private void editSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) { showInfo("Please select a notice to edit."); return; }
        Notice n = notices.get(row);
        openNoticeDialog(n);
    }

    private void deleteSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) { showInfo("Please select a notice to delete."); return; }
        Notice n = notices.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete notice:\n\"" + n.getTitle() + "\"?\n\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (noticeDAO.deleteNotice(n.getId())) {
                    JOptionPane.showMessageDialog(this, "✅ Notice deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadNotices();
                }
            } catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
        }
    }

    private void viewSelectedNotice() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) { showInfo("Please select a notice to view."); return; }
        Notice n = notices.get(row);
        new NoticeViewDialog(this, n, false).setVisible(true);
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }
}
