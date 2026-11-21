package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import logic.AccountService;
import model.AccessRequest;
import model.User;

public class RequestsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService;
    private final User admin;
    private final Runnable onDataChanged;

    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JSpinner minutesSpinner;

    public RequestsPanel(AccountService accountService, User admin, Runnable onDataChanged) {

        if (accountService == null || admin == null || onDataChanged == null) {
            throw new IllegalArgumentException("RequestsPanel dependencies cannot be null.");
        }

        if (!admin.isAdmin()) {
            throw new SecurityException("Administrator access required.");
        }

        this.accountService = accountService;
        this.admin = admin;
        this.onDataChanged = onDataChanged;

        createInterface();
        refreshRequests();
    }

    private void createInterface() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Student Registration Requests");
        headerPanel.add(title, BorderLayout.WEST);

        statusFilter = new JComboBox<>(new String[]{"All Requests", "Pending", "Approved", "Rejected"});
        headerPanel.add(statusFilter, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"University ID", "Full Name", "Email", "Phone", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        requestsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setAutoCreateRowSorter(true);

        add(new JScrollPane(requestsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 10));

        JPanel allowancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        allowancePanel.add(new JLabel("Initial GPU Minutes:"));

        minutesSpinner = new JSpinner(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 5));
        allowancePanel.add(minutesSpinner);

        bottomPanel.add(allowancePanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton refreshButton = new JButton("Refresh");

        buttonsPanel.add(approveButton);
        buttonsPanel.add(rejectButton);
        buttonsPanel.add(refreshButton);

        bottomPanel.add(buttonsPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> approveSelected());
        rejectButton.addActionListener(e -> rejectSelected());
        refreshButton.addActionListener(e -> refreshRequests());
        statusFilter.addActionListener(e -> refreshRequests());
    }

    public void refreshRequests() {

        tableModel.setRowCount(0);

        String selectedStatus = (String) statusFilter.getSelectedItem();

        List<AccessRequest> requests = accountService.getAllRequests(admin);

        for (AccessRequest request : requests) {

            String status = request.getStatus().name();

            if (!"All Requests".equals(selectedStatus) && !status.equalsIgnoreCase(selectedStatus)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                request.getUniversityId(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                status
            });
        }
    }

    private String getSelectedStudentId() {

        int selectedRow = requestsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a registration request.");
            return null;
        }

        int modelRow = requestsTable.convertRowIndexToModel(selectedRow);
        return tableModel.getValueAt(modelRow, 0).toString();
    }

    private void approveSelected() {

        String studentId = getSelectedStudentId();
        if (studentId == null) return;

        int minutes = (Integer) minutesSpinner.getValue();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve student " + studentId + " with " + minutes + " GPU minutes?",
            "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            accountService.approve(admin, studentId, minutes);

            JOptionPane.showMessageDialog(this, "Student approved successfully!");
            refreshRequests();
            onDataChanged.run();

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Approval Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelected() {

        String studentId = getSelectedStudentId();
        if (studentId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reject student " + studentId + "?",
            "Confirm Rejection", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            accountService.reject(admin, studentId);

            JOptionPane.showMessageDialog(this, "Registration request rejected.");
            refreshRequests();
            onDataChanged.run();

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Rejection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
