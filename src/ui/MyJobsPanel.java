package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import model.JobStatus;

public class MyJobsPanel extends JPanel {
    private final ApplicationService service;
    private final String studentId;
    private final DefaultTableModel model = JobTableSupport.createModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(20);
    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(model);

    public MyJobsPanel(ApplicationService service, String studentId) {
        this.service = service;
        this.studentId = studentId;

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
        List<Job> jobs = service.getJobsForStudent(studentId);
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
            service.cancelJob(jobId, studentId, reason);
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

final class JobTableSupport {
    private static final String[] COLUMNS = {
        "Job ID", "Student ID", "Title", "Application", "Task size",
        "Status", "GPU", "Queue position", "Predicted start",
        "Predicted finish", "Wait (min)", "Submitted", "Actual start",
        "Actual finish", "Runtime (min)"
    };

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JobTableSupport() {
    }

    static DefaultTableModel createModel() {
        return new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static void fill(DefaultTableModel model, List<Job> jobs,
                     Map<Integer, PredictionService.JobPrediction> predictions) {
        model.setRowCount(0);

        for (Job job : jobs) {
            PredictionService.JobPrediction prediction =
                    predictions.get(job.getJobId());

            Integer gpuId = job.getAssignedGpuId();
            String queuePosition = "";
            LocalDateTime predictedStart = job.getStartTime();
            LocalDateTime predictedFinish = job.getFinishTime();
            String waitMinutes = "";

            if (job.getStatus() == JobStatus.QUEUED && prediction != null) {
                gpuId = prediction.getPredictedGpuId();
                queuePosition = String.valueOf(prediction.getQueuePosition());
                predictedStart = prediction.getPredictedStart();
                predictedFinish = prediction.getPredictedFinish();
                waitMinutes =
                        String.valueOf(prediction.getRemainingWaitMinutes());
            } else if (job.getStartTime() != null) {
                long actualWait = Duration.between(
                        job.getSubmissionTime(), job.getStartTime()).toMinutes();
                waitMinutes = String.valueOf(Math.max(0, actualWait));

                if (job.getStatus() == JobStatus.RUNNING) {
                    predictedFinish = job.getStartTime().plusMinutes(
                            job.getEstimatedDurationMinutes());
                }
            }

            model.addRow(new Object[] {
                job.getJobId(),
                job.getStudentId(),
                job.getTitle(),
                job.getApplicationName(),
                formatName(job.getTaskSize().name()),
                formatName(job.getStatus().name()),
                gpuId == null ? "" : gpuId,
                queuePosition,
                formatTime(predictedStart),
                formatTime(predictedFinish),
                waitMinutes,
                formatTime(job.getSubmissionTime()),
                formatTime(job.getStartTime()),
                formatTime(job.getFinishTime()),
                job.getElapsedMinutes()
            });
        }
    }

    private static String formatTime(LocalDateTime time) {
        return time == null ? "" : FORMAT.format(time);
    }

    private static String formatName(String name) {
        String text = name.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}