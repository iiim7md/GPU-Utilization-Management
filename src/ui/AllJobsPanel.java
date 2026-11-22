package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import logic.ApplicationService;
import logic.PredictionService;
import model.Job;

public class AllJobsPanel extends JPanel {
    private final ApplicationService service;
    private final DefaultTableModel model = JobTableSupport.createModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(20);
    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(model);

    public AllJobsPanel(ApplicationService service) {
        this.service = service;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        table.setRowSorter(sorter);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());

        JButton cancelButton = new JButton("Cancel selected job");
        cancelButton.addActionListener(event -> cancelSelectedJob());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(refreshButton);
        toolbar.add(cancelButton);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                applySearch();
            }

            public void removeUpdate(DocumentEvent event) {
                applySearch();
            }

            public void changedUpdate(DocumentEvent event) {
                applySearch();
            }
        });

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        List<Job> jobs = service.getAllJobs();
        Map<Integer, PredictionService.JobPrediction> predictions =
                service.getPredictions();
        JobTableSupport.fill(model, jobs, predictions);
    }

    private void cancelSelectedJob() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a job first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int jobId = (Integer) model.getValueAt(modelRow, 0);
        String reason = JOptionPane.showInputDialog(
                this, "Why are you cancelling this job?");

        if (reason == null || reason.isBlank()) {
            return;
        }

        try {
            service.cancelJob(jobId, "admin", reason);
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not cancel job", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applySearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(query)));
        }
    }
}