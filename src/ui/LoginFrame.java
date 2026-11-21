package ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import logic.AccountService;
import model.User;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onRegister;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginFrame(AccountService accountService, Consumer<User> onLoginSuccess, Runnable onRegister) {

        if (accountService == null || onLoginSuccess == null || onRegister == null) {
            throw new IllegalArgumentException("Login dependencies cannot be null.");
        }

        this.accountService = accountService;
        this.onLoginSuccess = onLoginSuccess;
        this.onRegister = onRegister;

        setTitle("GPU Utilization Management - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createInterface();
    }

    private void createInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("GPU Utilization Management", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 21));

        JLabel subtitle = new JLabel("4 x NVIDIA H100 GPUs - Simulation", SwingConstants.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        mainPanel.add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username / University ID:"), gbc);

        usernameField = new JTextField(18);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(18);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Sign In");

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        JButton registerButton = new JButton("New Student? Request an Account");

        gbc.gridy = 3;
        formPanel.add(registerButton, gbc);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);

        gbc.gridy = 4;
        formPanel.add(messageLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        loginButton.addActionListener(e -> signIn());
        registerButton.addActionListener(e -> onRegister.run());

        getRootPane().setDefaultButton(loginButton);
    }

    private void signIn() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = accountService.signIn(username, password);

            messageLabel.setText("Login successful.");
            onLoginSuccess.accept(user);
            dispose();

        } catch (IllegalArgumentException | IllegalStateException ex) {

            messageLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);

        } finally {
            passwordField.setText("");
        }
    }
}
