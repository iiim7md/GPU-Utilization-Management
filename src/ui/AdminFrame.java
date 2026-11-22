package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import logic.ApplicationService;
import model.*;

/** Administrator workspace: dashboard, approvals, student management and jobs. */
public class AdminFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ApplicationService service;
    private final DashboardPanel dashboard;
    private final RequestsPanel requests;
    private final StudentsPanel students;
    private final AllJobsPanel jobs;
    private final JTabbedPane tabs = new JTabbedPane();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminFrame(ApplicationService service, User admin, Runnable logout) {
        if (service == null || admin == null || !admin.isAdmin()) throw new SecurityException("Admin login required.");
        this.service = service;
        setTitle("GPU Utilization Management - Administrator");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard = new DashboardPanel(() -> {
            try { service.advanceOneMinute(); refreshAll(); }
            catch (Exception ex) { showError(ex); }
        });
        requests = new RequestsPanel(service.getAccountService(), admin, this::saveAndRefresh);
        students = new StudentsPanel(service.getAccountService(), admin, this::saveAndRefresh);
        jobs = new AllJobsPanel(service);
        tabs.addTab("Dashboard", dashboard);
        tabs.addTab("Requests", requests);
        tabs.addTab("Students", students);
        tabs.addTab("All jobs", jobs);
        tabs.addChangeListener(e -> refreshAll());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton start = new JButton("Set clock (first run)");
        start.setEnabled(!service.getState().isClockInitialized());
        start.addActionListener(e -> {
            ClockSetupDialog dialog = new ClockSetupDialog(this);
            dialog.setVisible(true);
            if (dialog.getSelectedDateTime() != null) {
                try { service.initializeClock(dialog.getSelectedDateTime()); start.setEnabled(false); refreshAll(); }
                catch (Exception ex) { showError(ex); }
            }
        });
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> refreshAll());
        JButton out = new JButton("Log out"); out.addActionListener(e -> { dispose(); logout.run(); });
        top.add(start); top.add(refresh); top.add(out);
        add(top, BorderLayout.NORTH); add(tabs, BorderLayout.CENTER);
        refreshAll();
    }
    private void saveAndRefresh() { try { service.save(); refreshAll(); } catch (Exception ex) { showError(ex); } }
    private void showError(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    private String time(java.time.LocalDateTime t) { return t == null ? "-" : FORMAT.format(t); }
    private void refreshAll() {
        AppState state = service.getState();
        dashboard.setSimulationTime(state.isClockInitialized() ? time(state.getCurrentTime()) : "Not initialized");
        int queued=0,running=0,completed=0,cancelled=0;
        long busyMinutes=0;
        for (Job j: state.getJobs()) {
            switch (j.getStatus()) {
                case QUEUED: queued++;break;
                case RUNNING: running++;break;
                case COMPLETED: completed++;break;
                case CANCELLED: cancelled++;break;
            }
            busyMinutes += j.getElapsedMinutes();
        }
        long elapsed = state.isClockInitialized() ? java.time.Duration.between(state.getInitialTime(),state.getCurrentTime()).toMinutes() : 0;
        dashboard.updateSummary(queued,running,completed,cancelled,
                elapsed == 0 ? "N/A" : String.format("%.1f%%",100.0*busyMinutes/(4*elapsed)));
        List<Object[]> gpuRows = new ArrayList<>();
        for (GPU g: state.getGpus()) {
            Job j=g.getCurrentJob();
            gpuRows.add(new Object[]{"GPU"+g.getId(),g.isAvailable()?"Free":"Busy",
                    j==null?"-":j.getJobId(),j==null?"-":j.getRemainingMinutes(),
                    j==null?"-":time(state.getCurrentTime().plusMinutes(j.getRemainingMinutes()))});
        }
        dashboard.updateGpuTable(gpuRows.toArray(new Object[0][]));
        List<Object[]> jobRows = new ArrayList<>();
        java.util.Map<Integer,logic.PredictionService.JobPrediction> forecasts = service.getPredictions();
        for (Job j: state.getJobs()) {
            logic.PredictionService.JobPrediction p=forecasts.get(j.getJobId());
            jobRows.add(new Object[]{j.getJobId(),j.getStudentId(),j.getApplicationName(),j.getStatus(),
                p==null?"-":p.getQueuePosition(),time(p==null?j.getStartTime():p.getPredictedStart()),
                time(p==null?j.getFinishTime():p.getPredictedFinish()),p==null?"-":p.getRemainingWaitMinutes()});
        }
        dashboard.updateJobsTable(jobRows.toArray(new Object[0][]));
        List<Object[]> balances=new ArrayList<>();
        for (Student s: state.getStudents().values()) {
            TimeAllowance a=s.getAllowance();
            balances.add(new Object[]{s.getUniversityId(),a.getAllocated(),a.getUsed(),a.getReserved(),a.getAvailable()});
        }
        dashboard.updateBalancesTable(balances.toArray(new Object[0][]));
        requests.refreshRequests(); students.refreshStudent(); jobs.refresh();
    }
}
