
package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel clockLabel;
    private final JLabel queuedLabel;
    private final JLabel runningLabel;
    private final JLabel completedLabel;
    private final JLabel cancelledLabel;
    private final JLabel utilizationLabel;

    private final JTable gpuTable;
    private final JTable jobsTable;
    private final JTable balancesTable;

    public DashboardPanel(Runnable advanceAction) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));

        JPanel header = new JPanel(
                new BorderLayout());

        JLabel title = new JLabel(
                "GPU Utilization Dashboard");

        clockLabel = new JLabel(
                "Simulation clock not initialized");

        JButton advanceButton = new JButton(
                "+1 Minute");

        advanceButton.addActionListener(e -> {
            if (advanceAction != null) {
                advanceAction.run();
            }
        });

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        controls.add(clockLabel);
        controls.add(advanceButton);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(
                new BorderLayout(10, 10));

        JPanel summary = new JPanel(
                new GridLayout(1, 5, 10, 10));

        queuedLabel = new JLabel("Queued: 0");
        runningLabel = new JLabel("Running: 0");
        completedLabel = new JLabel("Completed: 0");
        cancelledLabel = new JLabel("Cancelled: 0");
        utilizationLabel = new JLabel(
                "Utilization: N/A");

        summary.add(queuedLabel);
        summary.add(runningLabel);
        summary.add(completedLabel);
        summary.add(cancelledLabel);
        summary.add(utilizationLabel);

        content.add(summary, BorderLayout.NORTH);

        gpuTable = createTable(new String[] {
                "GPU", "Status", "Current Job",
                "Remaining", "Finish"
        });

        jobsTable = createTable(new String[] {
                "Job ID", "Student", "Application",
                "Status", "Position", "Start",
                "Finish", "Wait"
        });

        balancesTable = createTable(new String[] {
                "Student", "Allocated", "Used",
                "Reserved", "Available"
        });

        JTabbedPane tables = new JTabbedPane();

        tables.addTab("GPUs",
                new JScrollPane(gpuTable));

        tables.addTab("Jobs",
                new JScrollPane(jobsTable));

        tables.addTab("Student Balances",
                new JScrollPane(balancesTable));

        content.add(tables, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JTable createTable(String[] columns) {
        DefaultTableModel model =
                new DefaultTableModel(columns, 0) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        return new JTable(model);
    }

    private void updateTable(
            JTable table, Object[][] rows) {

        DefaultTableModel model =
                (DefaultTableModel) table.getModel();

        model.setRowCount(0);

        if (rows != null) {
            for (Object[] row : rows) {
                model.addRow(row);
            }
        }
    }

    public void setSimulationTime(String time) {
        clockLabel.setText(
                "Simulation time: " + time);
    }

    public void updateSummary(
            int queued,
            int running,
            int completed,
            int cancelled,
            String utilization) {

        queuedLabel.setText("Queued: " + queued);
        runningLabel.setText("Running: " + running);
        completedLabel.setText(
                "Completed: " + completed);
        cancelledLabel.setText(
                "Cancelled: " + cancelled);
        utilizationLabel.setText(
                "Utilization: " + utilization);
    }

    public void updateGpuTable(Object[][] rows) {
        updateTable(gpuTable, rows);
    }

    public void updateJobsTable(Object[][] rows) {
        updateTable(jobsTable, rows);
    }

    public void updateBalancesTable(
            Object[][] rows) {
        updateTable(balancesTable, rows);
    }
}
