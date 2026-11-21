package ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import logic.AccountService;

public class RegisterPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService;
    private final Runnable onBack;

    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;

    public RegisterPanel(AccountService accountService, Runnable onBack) {

        if (accountService == null || onBack == null) {
            throw new IllegalArgumentException("Registration dependencies cannot be null.");
        }

        this.accountService = accountService;
        this.onBack = onBack;

        createInterface();
    }

    private void createInterface() {

        setLayout(new BorderLayout(10, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Request a Student Account", JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(20);
        nameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        passwordField = new JPasswordField(20);

        addField(formPanel, gbc, 0, "University ID:", idField);
        addField(formPanel, gbc, 1, "Full Name:", nameField);
        addField(formPanel, gbc, 2, "Email:", emailField);
        addField(formPanel, gbc, 3, "Phone Number:", phoneField);
        addField(formPanel, gbc, 4, "Password:", passwordField);

        JButton submitButton = new JButton("Submit Request");
        JButton backButton = new JButton("Back to Login");

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(submitButton, gbc);

        gbc.gridy = 6;
        formPanel.add(backButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        submitButton.addActionListener(e -> submitRegistration());
        backButton.addActionListener(e -> onBack.run());
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, javax.swing.JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void submitRegistration() {

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            accountService.register(id, name, email, phone, password);

            JOptionPane.showMessageDialog(this, "Registration submitted successfully!\nStatus: Pending", "Success", JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            onBack.run();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            passwordField.setText("");
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
    }
}
