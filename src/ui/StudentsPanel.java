package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import logic.AccountService;
import model.Student;
import model.TimeAllowance;
import model.User;

public class StudentsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService;
    private final User admin;
    private final Runnable onDataChanged;

    private JTextField searchField;
    private JTextField reasonField;
    private JSpinner minutesSpinner;

    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel allocatedLabel;
    private JLabel usedLabel;
    private JLabel reservedLabel;
    private JLabel availableLabel;

    private JTable jobsTable;
    private DefaultTableModel jobsTableModel;

    private Student selectedStudent;

    public StudentsPanel(AccountService accountService, User admin, Runnable onDataChanged) {

        if (accountService == null || admin == null || onDataChanged == null) {
            throw new IllegalArgumentException("StudentsPanel dependencies cannot be null.");
        }

        if (!admin.isAdmin()) {
            throw new SecurityException("Administrator access required.");
        }

        this.accountService = accountService;
        this.admin = admin;
        this.onDataChanged = onDataChanged;

        createInterface();
    }

    private void createInterface() {

        setLayout(new BorderLayout(10, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(5, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("University ID:"));

        searchField = new JTextField(15);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));

        nameLabel = new JLabel("Student Name: -");
        idLabel = new JLabel("Student ID: -");

        allocatedLabel = new JLabel("Allocated: -");
        usedLabel = new JLabel("Used: -");
        reservedLabel = new JLabel("Reserved: -");
        availableLabel = new JLabel("Available: -");

        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);
        infoPanel.add(allocatedLabel);
        infoPanel.add(usedLabel);
        infoPanel.add(reservedLabel);
        infoPanel.add(availableLabel);

        topPanel.add(infoPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Job ID", "Title", "Status", "GPU", "Start", "Charged"};

        jobsTableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jobsTable = new JTable(jobsTableModel);
        add(new JScrollPane(jobsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JLabel grantTitle = new JLabel("Grant Extra GPU Minutes");
        bottomPanel.add(grantTitle);

        JPanel grantPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        grantPanel.add(new JLabel("Minutes:"));

        minutesSpinner = new JSpinner(new SpinnerNumberModel(20, 1, Integer.MAX_VALUE, 1));
        grantPanel.add(minutesSpinner);

        grantPanel.add(new JLabel("Reason:"));

        reasonField = new JTextField(20);
        grantPanel.add(reasonField);

        bottomPanel.add(grantPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton grantButton = new JButton("Grant Minutes");
        JButton refreshButton = new JButton("Refresh");

        buttonsPanel.add(grantButton);
        buttonsPanel.add(refreshButton);

        bottomPanel.add(buttonsPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchStudent());
        searchField.addActionListener(e -> searchStudent());
        grantButton.addActionListener(e -> grantMinutes());
        refreshButton.addActionListener(e -> refreshStudent());
    }

    private void searchStudent() {

        String studentId = searchField.getText().trim();

        try {
            selectedStudent = accountService.findStudent(admin, studentId);
            refreshStudent();

        } catch (IllegalArgumentException | SecurityException ex) {
            selectedStudent = null;
            clearStudent();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshStudent() {

        if (selectedStudent == null) return;

        selectedStudent = accountService.findStudent(admin, selectedStudent.getUniversityId());

        TimeAllowance allowance = selectedStudent.getAllowance();

        nameLabel.setText("Student Name: " + selectedStudent.getFullName());
        idLabel.setText("Student ID: " + selectedStudent.getUniversityId());

        allocatedLabel.setText("Allocated: " + allowance.getAllocated());
        usedLabel.setText("Used: " + allowance.getUsed());
        reservedLabel.setText("Reserved: " + allowance.getReserved());
        availableLabel.setText("Available: " + allowance.getAvailable());


        jobsTableModel.setRowCount(0);
    }

    private void grantMinutes() {

        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please search for a student first.");
            return;
        }

        int minutes = (Integer) minutesSpinner.getValue();
        String reason = reasonField.getText().trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reason for the grant.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Grant " + minutes + " GPU minutes to " + selectedStudent.getFullName() + "?",
            "Confirm Time Grant", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            accountService.grantMinutes(admin, selectedStudent.getUniversityId(), minutes, reason);

            JOptionPane.showMessageDialog(this, "GPU minutes granted successfully!");

            reasonField.setText("");
            refreshStudent();
            onDataChanged.run();

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Grant Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearStudent() {

        nameLabel.setText("Student Name: -");
        idLabel.setText("Student ID: -");

        allocatedLabel.setText("Allocated: -");
        usedLabel.setText("Used: -");
        reservedLabel.setText("Reserved: -");
        availableLabel.setText("Available: -");

        jobsTableModel.setRowCount(0);
    }
}
