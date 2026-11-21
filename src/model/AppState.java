package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Student> students;
    private Map<String, AccessRequest> requests;
    private List<TimeGrant> grants;

    private List<Job> jobs;
    private List<GPU> gpus;
    private int nextJobId;

    private LocalDateTime initialTime;
    private LocalDateTime currentTime;

    public AppState() {
        students = new LinkedHashMap<>();
        requests = new LinkedHashMap<>();
        grants = new ArrayList<>();

        jobs = new ArrayList<>();
        gpus = new ArrayList<>();

        nextJobId = 101;

        initialTime = null;
        currentTime = null;

        for (int i = 1; i <= 4; i++) {
            gpus.add(new GPU(i));
        }
    }

    public Map<String, Student> getStudents() {
        return students;
    }

    public Map<String, AccessRequest> getRequests() {
        return requests;
    }

    public List<TimeGrant> getGrants() {
        return grants;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<GPU> getGpus() {
        return gpus;
    }

    public int getNextJobId() {
        return nextJobId++;
    }

    public LocalDateTime getInitialTime() {
        return initialTime;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void initializeClock(LocalDateTime startTime) {

        if (startTime == null) {
            throw new IllegalArgumentException("Starting time is required.");
        }

        if (initialTime != null) {
            throw new IllegalStateException("Simulation clock is already initialized.");
        }

        initialTime = startTime;
        currentTime = startTime;
    }

    public void advanceClock() {

        if (currentTime == null) {
            throw new IllegalStateException("Simulation clock is not initialized.");
        }

        currentTime = currentTime.plusMinutes(1);
    }

    public boolean isClockInitialized() {
        return currentTime != null;
    }
}
