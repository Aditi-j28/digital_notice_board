package com.noticeboard.ui;

import com.noticeboard.db.NoticeDAO;
import com.noticeboard.model.Notice;
import com.noticeboard.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;

public class NoticeDialog extends JDialog {

    private final Notice existing;
    private final int adminId;
    private final NoticeDAO dao = new NoticeDAO();

    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<String> categoryCombo, priorityCombo;
    private JLabel attachLabel;
    private byte[] attachData;
    private String attachName, attachType;
    private boolean saved = false;

    public NoticeDialog(Frame parent, Notice existing, int adminId) {
        super(parent, existing == null ? "Add New Notice" : "Edit Notice", true);
        this.existing = existing;
        this.adminId = adminId;
        setSize(600, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (existing != null) populateFields();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_PANEL);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(existing == null ? UITheme.SUCCESS : UITheme.WARNING);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel title = UITheme.createLabel(
            existing == null ? "➕  Add New Notice" : "✏  Edit Notice",
            UITheme.FONT_HEADING, Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        titleField = UITheme.createTextField("Enter notice title");
        categoryCombo = new JComboBox<>(Notice.CATEGORIES);
        categoryCombo.setFont(UITheme.FONT_BODY);
        priorityCombo = new JComboBox<>(Notice.PRIORITIES);
        priorityCombo.setFont(UITheme.FONT_BODY);

        contentArea = new JTextArea(8, 30);
        contentArea.setFont(UITheme.FONT_BODY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        attachLabel = UITheme.createLabel("No file selected", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        JButton chooseFileBtn = UITheme.createButton("📎 Choose File", UITheme.ACCENT, Color.WHITE);
        JButton clearFileBtn = UITheme.createButton("✕ Remove", UITheme.DANGER, Color.WHITE);
        chooseFileBtn.addActionListener(e -> chooseAttachment());
        clearFileBtn.addActionListener(e -> {
            attachData = null; attachName = null; attachType = null;
            attachLabel.setText("No file selected");
        });

        JPanel attachRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        attachRow.setOpaque(false);
        attachRow.add(chooseFileBtn);
        attachRow.add(clearFileBtn);
        attachRow.add(attachLabel);

        int row = 0;
        gbc.gridy = row++; form.add(formField("Notice Title *", titleField), gbc);
        gbc.gridy = row++;
        JPanel twoCol = new JPanel(new GridLayout(1, 2, 12, 0));
        twoCol.setOpaque(false);
        twoCol.add(formField("Category", categoryCombo));
        twoCol.add(formField("Priority", priorityCombo));
        form.add(twoCol, gbc);
        gbc.gridy = row++; form.add(formField("Content *", contentScroll), gbc);
        gbc.gridy = row++; form.add(formField("Attachment (optional)", attachRow), gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.BG_PANEL);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createButton("Cancel", UITheme.TEXT_SECONDARY, Color.WHITE);
        JButton saveBtn = UITheme.createButton(existing == null ? "✅ Save Notice" : "✅ Update Notice",
            existing == null ? UITheme.SUCCESS : UITheme.WARNING, Color.WHITE);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> saveNotice());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());

        main.add(header, BorderLayout.NORTH);
        main.add(formScroll, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);

        add(main);
    }

    private JPanel formField(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
         JLabel lbl = UITheme.createLabel(label, UITheme.FONT_LABEL,        UITheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void populateFields() {
        titleField.setText(existing.getTitle());
        contentArea.setText(existing.getContent());
        categoryCombo.setSelectedItem(existing.getCategory());
        priorityCombo.setSelectedItem(existing.getPriority());
        if (existing.hasAttachment()) {
            attachLabel.setText("📎 " + existing.getAttachmentName());
            attachName = existing.getAttachmentName();
            attachType = existing.getAttachmentType();
        }
    }

    private void chooseAttachment() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Attachment");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Supported Files (PDF, DOC, DOCX, PNG, JPG, TXT)",
            "pdf", "doc", "docx", "png", "jpg", "jpeg", "txt", "xlsx", "pptx"
        ));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.length() > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this, "File size must be under 10 MB.", "File Too Large", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                attachData = Files.readAllBytes(file.toPath());
                attachName = file.getName();
                attachType = Files.probeContentType(file.toPath());
                if (attachType == null) attachType = "application/octet-stream";
                attachLabel.setText("📎 " + attachName + " (" + (attachData.length / 1024) + " KB)");
                attachLabel.setForeground(UITheme.SUCCESS);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveNotice() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a notice title.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter notice content.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            contentArea.requestFocus();
            return;
        }

        Notice notice = existing != null ? existing : new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCategory(category);
        notice.setPriority(priority);
        notice.setPostedBy(adminId);

        if (attachData != null) {
            notice.setAttachmentData(attachData);
            notice.setAttachmentName(attachName);
            notice.setAttachmentType(attachType);
        } else if (existing != null && attachName != null) {
            // Keep existing attachment (no change)
        } else {
            notice.setAttachmentData(null);
            notice.setAttachmentName(null);
            notice.setAttachmentType(null);
        }

        try {
            boolean ok;
            if (existing == null) {
                ok = dao.addNotice(notice);
            } else {
                ok = dao.updateNotice(notice);
            }
            if (ok) {
                saved = true;
                JOptionPane.showMessageDialog(this,
                    "✅ Notice " + (existing == null ? "added" : "updated") + " successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
