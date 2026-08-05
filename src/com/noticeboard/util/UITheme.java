package com.noticeboard.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UITheme {

    // Color Palette
    public static final Color PRIMARY        = new Color(25, 118, 210);
    public static final Color PRIMARY_DARK   = new Color(13, 71, 161);
    public static final Color PRIMARY_LIGHT  = new Color(100, 181, 246);
    public static final Color ACCENT         = new Color(0, 188, 212);
    public static final Color SUCCESS        = new Color(76, 175, 80);
    public static final Color DANGER         = new Color(244, 67, 54);
    public static final Color WARNING        = new Color(255, 152, 0);
    public static final Color BG_DARK        = new Color(21, 32, 43);
    public static final Color BG_CARD        = new Color(255, 255, 255);
    public static final Color BG_SIDEBAR     = new Color(18, 26, 36);
    public static final Color BG_PANEL       = new Color(245, 247, 250);
    public static final Color TEXT_PRIMARY   = new Color(30, 30, 30);
    public static final Color TEXT_SECONDARY = new Color(100, 100, 110);
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color BORDER_COLOR   = new Color(220, 225, 235);
    public static final Color URGENT_COLOR   = new Color(244, 67, 54);
    public static final Color HIGH_COLOR     = new Color(255, 152, 0);
    public static final Color MED_COLOR      = new Color(33, 150, 243);
    public static final Color LOW_COLOR      = new Color(76, 175, 80);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 13);

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BTN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        return btn;
    }

    public static JButton createIconButton(String text, Color bg) {
        JButton btn = createButton(text, bg, Color.WHITE);
        return btn;
    }

    public static JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        tf.setToolTipText(placeholder);
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return pf;
    }

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));
        return card;
    }

    public static Color getPriorityColor(String priority) {
        if (priority == null) return MED_COLOR;
        switch (priority.toLowerCase()) {
            case "urgent": return URGENT_COLOR;
            case "high":   return HIGH_COLOR;
            case "low":    return LOW_COLOR;
            default:       return MED_COLOR;
        }
    }

    public static Color getCategoryColor(String category) {
        if (category == null) return PRIMARY;
        switch (category.toLowerCase()) {
            case "exam":     return new Color(156, 39, 176);
            case "event":    return new Color(0, 150, 136);
            case "holiday":  return new Color(255, 87, 34);
            case "sports":   return new Color(76, 175, 80);
            case "academic": return new Color(33, 150, 243);
            default:         return new Color(96, 125, 139);
        }
    }

    // Simple drop-shadow border simulation
    public static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 12, 12);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(x, y, width - 3, height - 3, 10, 10);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(4, 4, 6, 6); }
    }
}
