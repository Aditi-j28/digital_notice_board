package com.noticeboard.ui;

import com.noticeboard.db.NoticeDAO;
import com.noticeboard.model.Notice;
import com.noticeboard.model.User;
import com.noticeboard.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final User currentUser;
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private List<Notice> notices;
    private JPanel noticeListPanel;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel countLabel;

    public StudentDashboard(User user) {
        this.currentUser = user;
        setTitle("Student Dashboard - Digital Notice Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 550));
        initUI();
        loadNotices();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ─── Sidebar ─────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel logoIcon = new JLabel("📋", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoText = UITheme.createLabel("Notice Board", UITheme.FONT_HEADING, UITheme.TEXT_WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel badge = UITheme.createLabel("STUDENT PORTAL", UITheme.FONT_SMALL, UITheme.PRIMARY_LIGHT);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 65, 80));
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Student info card
        JPanel infoCard = new JPanel(new GridBagLayout());
        infoCard.setBackground(new Color(30, 42, 55));
        infoCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        infoCard.setMaximumSize(new Dimension(196, 100));
        infoCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0; g.weightx = 1;
        g.insets = new Insets(2, 0, 2, 0);

        JLabel nameL = UITheme.createLabel("👤  " + currentUser.getName(), UITheme.FONT_BODY, Color.WHITE);
        JLabel emailL = UITheme.createLabel(currentUser.getEmail(), UITheme.FONT_SMALL, new Color(160, 190, 220));
        emailL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        String sid = currentUser.getStudentId();
        JLabel idL = UITheme.createLabel("ID: " + (sid != null ? sid : "—"), UITheme.FONT_SMALL, UITheme.ACCENT);

        g.gridy = 0; infoCard.add(nameL, g);
        g.gridy = 1; infoCard.add(emailL, g);
        g.gridy = 2; infoCard.add(idL, g);

        JButton refreshBtn = createSidebarBtn("🔄  Refresh", UITheme.PRIMARY);
        JButton logoutBtn = createSidebarBtn("🚪  Logout", UITheme.DANGER);
        refreshBtn.addActionListener(e -> loadNotices());
        logoutBtn.addActionListener(e -> logout());

        // Category quick filters
        JLabel filterTitle = UITheme.createLabel("  QUICK FILTERS", new Font("Segoe UI", Font.BOLD, 10), new Color(120, 150, 180));
        filterTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterTitle.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 0));

        sidebar.add(logoIcon);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(logoText);
        sidebar.add(badge);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(infoCard);
        sidebar.add(Box.createVerticalStrut(20));

        String[] catColors = {"General", "Academic", "Exam", "Event", "Holiday", "Sports"};
        String[] catEmojis = {"📌", "📚", "📝", "🎉", "🏖", "⚽"};
        for (int i = 0; i < catColors.length; i++) {
            final String cat = catColors[i];
            JButton catBtn = new JButton(catEmojis[i] + "  " + cat);
            catBtn.setFont(UITheme.FONT_SMALL);
            catBtn.setForeground(new Color(180, 210, 240));
            catBtn.setBackground(UITheme.BG_SIDEBAR);
            catBtn.setOpaque(true);
            catBtn.setBorderPainted(false);
            catBtn.setFocusPainted(false);
            catBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            catBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            catBtn.setMaximumSize(new Dimension(196, 34));
            catBtn.setHorizontalAlignment(SwingConstants.LEFT);
            catBtn.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 16));
            catBtn.addActionListener(e -> filterByCategory(cat));
            sidebar.add(catBtn);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(refreshBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        // ─── Top bar ─────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        JLabel pageTitle = UITheme.createLabel("📢  Notice Board", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        searchField = UITheme.createTextField("🔍 Search notices...");
        searchField.setPreferredSize(new Dimension(200, 36));
        JButton searchBtn = UITheme.createButton("Search", UITheme.PRIMARY, Color.WHITE);

        String[] cats = new String[Notice.CATEGORIES.length + 1];
        cats[0] = "All Categories";
        System.arraycopy(Notice.CATEGORIES, 0, cats, 1, Notice.CATEGORIES.length);
        categoryFilter = new JComboBox<>(cats);
        categoryFilter.setFont(UITheme.FONT_BODY);
        categoryFilter.setPreferredSize(new Dimension(160, 36));

        JButton allBtn = UITheme.createButton("Show All", new Color(96, 125, 139), Color.WHITE);

        searchBtn.addActionListener(e -> doSearch());
        allBtn.addActionListener(e -> { searchField.setText(""); categoryFilter.setSelectedIndex(0); loadNotices(); });
        categoryFilter.addActionListener(e -> {
            int idx = categoryFilter.getSelectedIndex();
            if (idx == 0) loadNotices();
            else filterByCategory((String) categoryFilter.getSelectedItem());
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doSearch();
            }
        });

        controls.add(searchField);
        controls.add(categoryFilter);
        controls.add(searchBtn);
        controls.add(allBtn);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);

        // ─── Notice list area ─────────────────────────────────────
        noticeListPanel = new JPanel();
        noticeListPanel.setLayout(new BoxLayout(noticeListPanel, BoxLayout.Y_AXIS));
        noticeListPanel.setBackground(UITheme.BG_PANEL);
        noticeListPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        scrollPane = new JScrollPane(noticeListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UITheme.BG_PANEL);

        // Count bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        statusBar.setBackground(UITheme.BG_PANEL);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        countLabel = UITheme.createLabel("", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        statusBar.add(countLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.BG_PANEL);
        content.add(topBar, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(statusBar, BorderLayout.SOUTH);

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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void loadNotices() {
        try {
            notices = noticeDAO.getAllNotices();
            renderNotices(notices);
        } catch (SQLException e) {
            showError("Failed to load notices: " + e.getMessage());
        }
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadNotices(); return; }
        try {
            notices = noticeDAO.searchNotices(kw);
            renderNotices(notices);
        } catch (SQLException e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private void filterByCategory(String category) {
        try {
            notices = noticeDAO.filterByCategory(category);
            renderNotices(notices);
        } catch (SQLException e) {
            showError("Filter error: " + e.getMessage());
        }
    }

    private void renderNotices(List<Notice> list) {
        noticeListPanel.removeAll();
        if (list.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setOpaque(false);
            JLabel emptyLabel = UITheme.createLabel("📭  No notices found", UITheme.FONT_HEADING, UITheme.TEXT_SECONDARY);
            empty.add(emptyLabel);
            empty.setPreferredSize(new Dimension(0, 300));
            noticeListPanel.add(empty);
        } else {
            for (Notice n : list) {
                noticeListPanel.add(buildNoticeCard(n));
                noticeListPanel.add(Box.createVerticalStrut(12));
            }
        }
        countLabel.setText(list.size() + " notice(s) shown");
        noticeListPanel.revalidate();
        noticeListPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildNoticeCard(Notice notice) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Priority strip on left
        JPanel strip = new JPanel();
        strip.setBackground(UITheme.getPriorityColor(notice.getPriority()));
        strip.setPreferredSize(new Dimension(5, 0));

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);

        // Top row: title + badges
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLabel = UITheme.createLabel(notice.getTitle(), UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(createBadge(notice.getCategory(), UITheme.getCategoryColor(notice.getCategory())));
        badges.add(createBadge(notice.getPriority(), UITheme.getPriorityColor(notice.getPriority())));
        if (notice.hasAttachment()) badges.add(createBadge("📎 Attachment", UITheme.ACCENT));

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(badges, BorderLayout.EAST);

        // Preview text
        String preview = notice.getContent();
        if (preview.length() > 140) preview = preview.substring(0, 140) + "…";
        JLabel previewLabel = UITheme.createLabel(preview, UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        previewLabel.setPreferredSize(new Dimension(0, 36));

        // Bottom row: meta + action buttons
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        JLabel meta = UITheme.createLabel(
            "By " + (notice.getPostedByName() != null ? notice.getPostedByName() : "Admin") +
            "  •  " + (notice.getCreatedAt() != null ? sdf.format(notice.getCreatedAt()) : ""),
            UITheme.FONT_SMALL, new Color(150, 160, 175)
        );

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JButton viewBtn = UITheme.createButton("View", UITheme.PRIMARY, Color.WHITE);
        viewBtn.setFont(UITheme.FONT_SMALL);
        viewBtn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        viewBtn.addActionListener(e -> new NoticeViewDialog((Frame) SwingUtilities.getWindowAncestor(this), notice, true).setVisible(true));
        actions.add(viewBtn);

        if (notice.hasAttachment()) {
            JButton dlBtn = UITheme.createButton("⬇ Download", UITheme.ACCENT, Color.WHITE);
            dlBtn.setFont(UITheme.FONT_SMALL);
            dlBtn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
            dlBtn.addActionListener(e -> downloadAttachment(notice));
            actions.add(dlBtn);
        }

        bottomRow.add(meta, BorderLayout.WEST);
        bottomRow.add(actions, BorderLayout.EAST);

        content.add(topRow, BorderLayout.NORTH);
        content.add(previewLabel, BorderLayout.CENTER);
        content.add(bottomRow, BorderLayout.SOUTH);

        card.add(strip, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(new Color(248, 250, 255)); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
            @Override public void mouseClicked(MouseEvent e) {
                new NoticeViewDialog((Frame) SwingUtilities.getWindowAncestor(StudentDashboard.this), notice, true).setVisible(true);
            }
        });

        return card;
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setBackground(color);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private void downloadAttachment(Notice notice) {
        try {
            NoticeDAO dao = new NoticeDAO();
            Notice full = dao.getNoticeWithAttachment(notice.getId());
            if (full == null || full.getAttachmentData() == null) {
                JOptionPane.showMessageDialog(this, "Attachment not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File(full.getAttachmentName()));
            chooser.setDialogTitle("Save Attachment As");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File dest = chooser.getSelectedFile();
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                    fos.write(full.getAttachmentData());
                }
                JOptionPane.showMessageDialog(this,
                    "✅ File saved:\n" + dest.getAbsolutePath(),
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showError("Download failed: " + ex.getMessage());
        }
    }

    private void logout() { dispose(); new LoginFrame().setVisible(true); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
