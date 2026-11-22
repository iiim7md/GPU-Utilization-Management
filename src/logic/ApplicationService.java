package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.AppState;
import model.Job;
import model.Student;
import model.TaskSize;
import storage.DataStore;

/** Coordinates account operations, scheduling and persisted application state. */
public class ApplicationService {
    private final AppState state;
    private final AccountService accountService;
    private final DataStore dataStore;
    private final Scheduler scheduler = new Scheduler();
    private final PredictionService predictionService = new PredictionService();

    public ApplicationService(AppState state, DataStore dataStore) {
        if (state == null || dataStore == null) {
            throw new IllegalArgumentException("Application dependencies cannot be null.");
        }
        this.state = state;
        this.dataStore = dataStore;
        this.accountService = new AccountService(state);
    }

    public AppState getState() { return state; }
    public AccountService getAccountService() { return accountService; }
    public List<Job> getAllJobs() { return new ArrayList<>(state.getJobs()); }
    public List<Job> getJobsForStudent(String id) {
        return state.getJobs().stream().filter(j -> j.getStudentId().equals(id))
                .collect(Collectors.toList());
    }
    public Map<Integer, PredictionService.JobPrediction> getPredictions() {
        return predictionService.predict(state);
    }
    public Job submitJob(String studentId, String title, String app, String description, TaskSize size) throws IOException {
        Job job = scheduler.submit(state, studentId, title, app, description, size);
        save();
        return job;
    }
    public void cancelJob(int jobId, String actorId, String reason) {
        boolean admin = "admin".equals(actorId);
        if (!admin && !state.getStudents().containsKey(actorId)) {
            throw new SecurityException("Student is not registered.");
        }
        scheduler.cancelJob(state, jobId, actorId, admin, reason);
        saveUnchecked();
    }
    public void advanceOneMinute() throws IOException {
        scheduler.advanceOneMinute(state);
        save();
    }
    public void initializeClock(java.time.LocalDateTime when) throws IOException {
        state.initializeClock(when);
        save();
    }
    public void save() throws IOException { dataStore.save(state); }
    public void saveUnchecked() {
        try { save(); } catch (IOException ex) { throw new IllegalStateException("Unable to save state: " + ex.getMessage(), ex); }
    }
}
