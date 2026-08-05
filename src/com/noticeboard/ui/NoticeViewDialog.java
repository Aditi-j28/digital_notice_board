package com.noticeboard.ui;

import com.noticeboard.db.NoticeDAO;
import com.noticeboard.model.Notice;
import com.noticeboard.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class NoticeViewDialog extends JDialog {

    private final Notice notice;
    private final boolean canDownload;
    private final NoticeDAO dao = new NoticeDAO();

    public NoticeViewDialog(Frame parent, Notice notice, boolean isStudent) {
        super(parent, "Notice Details", true);
        this.notice = notice;
        this.canDownload = true;
        setSize(620, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // Priority color strip
        Color priorityColor = UITheme.getPriorityColor(notice.getPriority());
        JPanel colorStrip = new JPanel();
        colorStrip.setBackground(priorityColor);
        colorStrip.setPreferredSize(new Dimension(0, 5));

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 4));
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JTextArea titleLabel = new JTextArea(notice.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(UITheme.BG_DARK);
        titleLabel.setLineWrap(true);
        titleLabel.setWrapStyleWord(true);
        titleLabel.setEditable(false);    
        titleLabel.setLineWrap(true);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        meta.setOpaque(false);

        meta.add(createBadge(notice.getCategory(), UITheme.getCategoryColor(notice.getCategory())));
        meta.add(createBadge(notice.getPriority(), priorityColor));

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(meta, BorderLayout.SOUTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        JLabel dateLabel = UITheme.createLabel(
            "Posted by " + (notice.getPostedByName() != null ? notice.getPostedByName() : "Unknown") +
            "  •  " + (notice.getCreatedAt() != null ? sdf.format(notice.getCreatedAt()) : "—"),
            UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_COLOR);

        JTextArea contentArea = new JTextArea(notice.getContent());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setForeground(UITheme.TEXT_PRIMARY);
        contentArea.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.setPreferredSize(new Dimension(0, 200));
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridy = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        body.add(dateLabel, gbc);
        gbc.gridy = 1; body.add(sep, gbc);
        gbc.gridy = 2; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        body.add(contentScroll, gbc);

        // Attachment section
        JPanel attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        attachPanel.setBackground(new Color(245, 250, 255));
        attachPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 16, 4, 16)
        ));

        if (notice.hasAttachment()) {
            JLabel attachIcon = UITheme.createLabel("📎  " + notice.getAttachmentName(), UITheme.FONT_BODY, UITheme.TEXT_PRIMARY);
            JButton downloadBtn = UITheme.createButton("⬇ Download", UITheme.PRIMARY, Color.WHITE);
            downloadBtn.addActionListener(e -> downloadAttachment());
            attachPanel.add(attachIcon);
            attachPanel.add(downloadBtn);
        } else {
            attachPanel.add(UITheme.createLabel("No attachment", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY));
        }

        // Close button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(UITheme.BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        JButton closeBtn = UITheme.createButton("Close", UITheme.TEXT_SECONDARY, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        main.add(colorStrip, BorderLayout.NORTH);
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(header, BorderLayout.CENTER);
        main.add(topSection, BorderLayout.NORTH);
        main.add(body, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.add(attachPanel, BorderLayout.NORTH);
        bottomSection.add(footer, BorderLayout.SOUTH);
        main.add(bottomSection, BorderLayout.SOUTH);

        // Fix layout overlap: combine north panels
        JPanel northAll = new JPanel(new BorderLayout());
        northAll.add(colorStrip, BorderLayout.NORTH);
        northAll.add(header, BorderLayout.CENTER);
        main.remove(topSection);
        main.add(northAll, BorderLayout.NORTH);

        add(main);
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBackground(color);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return badge;
    }

    private void downloadAttachment() {
        try {
            Notice full = dao.getNoticeWithAttachment(notice.getId());
            if (full == null || full.getAttachmentData() == null) {
                JOptionPane.showMessageDialog(this, "Attachment data not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(full.getAttachmentName()));
            chooser.setDialogTitle("Save Attachment As");
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File dest = chooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(full.getAttachmentData());
                }
                JOptionPane.showMessageDialog(this,
                    "✅ File saved to:\n" + dest.getAbsolutePath(),
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Download failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
