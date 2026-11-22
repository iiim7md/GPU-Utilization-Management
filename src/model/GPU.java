package model;

import java.io.Serializable;

public class GPU implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private Job currentJob;

    public GPU(int id) {
        if (id < 1 || id > 4) {
            throw new IllegalArgumentException("GPU ID must be between 1 and 4.");
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public boolean isAvailable() {
        return currentJob == null;
    }

    public void assignJob(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null.");
        }
        if (currentJob != null) {
            throw new IllegalStateException("This GPU is already busy.");
        }
        currentJob = job;
    }

    public void releaseJob() {
        if (currentJob == null) {
            throw new IllegalStateException("This GPU has no running job.");
        }
        currentJob = null;
    }
}