package com.noticeboard.ui;

import com.noticeboard.db.DBConnection;
import com.noticeboard.db.UserDAO;
import com.noticeboard.model.User;
import com.noticeboard.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField nameField, emailField, studentIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton loginBtn, registerBtn, switchBtn;
    private JPanel studentIdPanel;
    private JLabel titleLabel, subtitleLabel;
    private boolean isLoginMode = true;
    private UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Digital Notice Board - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
        checkDBConnection();
    }

    private void checkDBConnection() {
        if (!DBConnection.testConnection()) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "⚠ Could not connect to MySQL database!\n\n" +
                    "Please ensure:\n" +
                    "1. MySQL server is running\n" +
                    "2. Database 'digital_notice_board' exists\n" +
                    "3. Credentials in DBConnection.java are correct\n" +
                    "4. Run database_setup.sql first",
                    "Database Connection Error", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, 0, getHeight(), new Color(13, 71, 161));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Header
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 20, 30));

        JLabel iconLabel = new JLabel("📋", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        titleLabel = UITheme.createLabel("Digital Notice Board", UITheme.FONT_TITLE, UITheme.TEXT_WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel = UITheme.createLabel("Sign in to your account", UITheme.FONT_SMALL, new Color(180, 210, 255));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 8, 0);
        headerPanel.add(iconLabel, gbc);
        gbc.gridy = 1; headerPanel.add(titleLabel, gbc);
        gbc.gridy = 2; headerPanel.add(subtitleLabel, gbc);

        // Form card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        c.gridx = 0; c.weightx = 1;

        // Name field (register only)
        nameField = UITheme.createTextField("Full Name");
        nameField.setVisible(false);

        // Role selector
        roleCombo = new JComboBox<>(new String[]{"student", "admin"});
        roleCombo.setFont(UITheme.FONT_BODY);
        roleCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        roleCombo.setBackground(Color.WHITE);

        // Student ID panel
        studentIdField = UITheme.createTextField("Student ID");
        studentIdPanel = new JPanel(new BorderLayout());
        studentIdPanel.setOpaque(false);
        studentIdPanel.add(createFormLabel("Student ID"), BorderLayout.NORTH);
        studentIdPanel.add(studentIdField, BorderLayout.CENTER);
        studentIdPanel.setVisible(false);

        emailField = UITheme.createTextField("Email Address");
        passwordField = UITheme.createPasswordField();

        // Role listener to show/hide Student ID
        roleCombo.addActionListener(e -> {
            boolean isStudent = "student".equals(roleCombo.getSelectedItem());
            studentIdPanel.setVisible(isStudent && !isLoginMode);
            revalidate(); repaint();
        });

        // Name row (register)
        JPanel nameRow = new JPanel(new BorderLayout()); nameRow.setOpaque(false);
        nameRow.add(createFormLabel("Full Name"), BorderLayout.NORTH);
        nameRow.add(nameField, BorderLayout.CENTER);
        nameRow.setVisible(false);

        // Email row
        JPanel emailRow = new JPanel(new BorderLayout()); emailRow.setOpaque(false);
        emailRow.add(createFormLabel("Email Address"), BorderLayout.NORTH);
        emailRow.add(emailField, BorderLayout.CENTER);

        // Password row
        JPanel passRow = new JPanel(new BorderLayout()); passRow.setOpaque(false);
        passRow.add(createFormLabel("Password"), BorderLayout.NORTH);
        JPanel passInner = new JPanel(new BorderLayout()); passInner.setOpaque(false);
        passInner.add(passwordField, BorderLayout.CENTER);
        passRow.add(passInner, BorderLayout.CENTER);

        // Role row
        JPanel roleRow = new JPanel(new BorderLayout()); roleRow.setOpaque(false);
        roleRow.add(createFormLabel("Role"), BorderLayout.NORTH);
        roleRow.add(roleCombo, BorderLayout.CENTER);

        // Buttons
        loginBtn = UITheme.createButton("Sign In", UITheme.PRIMARY, Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(0, 44));
        loginBtn.addActionListener(e -> handleLogin());

        registerBtn = UITheme.createButton("Create Account", UITheme.SUCCESS, Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(0, 44));
        registerBtn.setVisible(false);
        registerBtn.addActionListener(e -> handleRegister());

        switchBtn = new JButton("Don't have an account? Register");
        switchBtn.setFont(UITheme.FONT_SMALL);
        switchBtn.setForeground(UITheme.PRIMARY);
        switchBtn.setBorderPainted(false);
        switchBtn.setContentAreaFilled(false);
        switchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchBtn.addActionListener(e -> toggleMode(nameRow, roleRow, loginBtn, registerBtn));

        c.gridy = 0; card.add(nameRow, c);
        c.gridy = 1; card.add(emailRow, c);
        c.gridy = 2; card.add(passRow, c);
        c.gridy = 3; card.add(roleRow, c);
        c.gridy = 4; card.add(studentIdPanel, c);
        c.gridy = 5; c.insets = new Insets(14, 0, 4, 0); card.add(loginBtn, c);
        c.gridy = 6; c.insets = new Insets(4, 0, 4, 0); card.add(registerBtn, c);
        c.gridy = 7; c.insets = new Insets(8, 0, 0, 0); card.add(switchBtn, c);

        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(BorderFactory.createEmptyBorder(0, 24, 30, 24));
        cardWrapper.add(card, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardWrapper, BorderLayout.CENTER);

        add(mainPanel);

        // Enter key shortcut
        getRootPane().setDefaultButton(loginBtn);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        });
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private void toggleMode(JPanel nameRow, JPanel roleRow, JButton loginBtn, JButton registerBtn) {
        isLoginMode = !isLoginMode;
        nameRow.setVisible(!isLoginMode);
        nameField.setVisible(!isLoginMode);
        loginBtn.setVisible(isLoginMode);
        registerBtn.setVisible(!isLoginMode);

        boolean isStudent = "student".equals(roleCombo.getSelectedItem());
        studentIdPanel.setVisible(!isLoginMode && isStudent);

        if (isLoginMode) {
            subtitleLabel.setText("Sign in to your account");
            switchBtn.setText("Don't have an account? Register");
            setSize(480, 580);
        } else {
            subtitleLabel.setText("Create your account");
            switchBtn.setText("Already have an account? Sign In");
            setSize(480, 660);
        }
        setLocationRelativeTo(null);
        revalidate(); repaint();
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        try {
            UserDAO dao = new UserDAO();
            User user = dao.authenticate(email, password);

            if (user == null) {
                showError("Invalid email or password.");
            } else if (!user.getRole().equalsIgnoreCase(role)) {
                showError("Role mismatch. Please select the correct role.");
            } else {
                dispose();
                if (user.isAdmin()) {
                    new AdminDashboard(user).setVisible(true);
                } else {
                    new StudentDashboard(user).setVisible(true);
                }
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();
        String studentId = studentIdField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all required fields.");
            return;
        }
        if (!email.contains("@")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if ("student".equals(role) && studentId.isEmpty()) {
            showError("Student ID is required for students.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showError("An account with this email already exists.");
                return;
            }
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setStudentId(studentId);

            if (userDAO.registerUser(newUser)) {
                JOptionPane.showMessageDialog(this,
                    "✅ Account created successfully!\nYou can now sign in.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                // Switch back to login
                emailField.setText(email);
                passwordField.setText("");
            } else {
                showError("Registration failed. Please try again.");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
