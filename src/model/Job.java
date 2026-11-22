package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int jobId;
    private final String studentId;
    private final String title;
    private final String applicationName;
    private final String description;
    private final TaskSize taskSize;
    private final int workloadUnits;
    private final int estimatedDurationMinutes;
    private final LocalDateTime submissionTime;

    private JobStatus status;
    private Integer assignedGpuId;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private int elapsedMinutes;
    private CancellationInfo cancellationInfo;

    public Job(int jobId, String studentId, String title,
               String applicationName, String description,
               TaskSize taskSize, LocalDateTime submissionTime) {
        if (jobId < 1) {
            throw new IllegalArgumentException("Job ID must be positive.");
        }
        if (studentId == null || studentId.isBlank()
                || title == null || title.isBlank()
                || applicationName == null || applicationName.isBlank()
                || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Job details cannot be blank.");
        }
        if (taskSize == null || submissionTime == null) {
            throw new IllegalArgumentException("Task size and submission time are required.");
        }

        this.jobId = jobId;
        this.studentId = studentId.trim();
        this.title = title.trim();
        this.applicationName = applicationName.trim();
        this.description = description.trim();
        this.taskSize = taskSize;
        this.workloadUnits = taskSize.getWorkloadUnits();
        this.estimatedDurationMinutes = taskSize.getDurationMinutes();
        this.submissionTime = submissionTime;

        this.status = JobStatus.QUEUED;
        this.assignedGpuId = null;
        this.startTime = null;
        this.finishTime = null;
        this.elapsedMinutes = 0;
    }

    public int getJobId() {
        return jobId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDescription() {
        return description;
    }

    public TaskSize getTaskSize() {
        return taskSize;
    }

    public int getWorkloadUnits() {
        return workloadUnits;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Integer getAssignedGpuId() {
        return assignedGpuId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public int getElapsedMinutes() {
        return elapsedMinutes;
    }

    public int getRemainingMinutes() {
        return estimatedDurationMinutes - elapsedMinutes;
    }

    public CancellationInfo getCancellationInfo() {
        return cancellationInfo;
    }

    public void startOnGpu(int gpuId, LocalDateTime simulatedStartTime) {
        if (status != JobStatus.QUEUED) {
            throw new IllegalStateException("Only a queued job can start.");
        }
        if (gpuId < 1 || gpuId > 4 || simulatedStartTime == null) {
            throw new IllegalArgumentException("Valid GPU and start time are required.");
        }

        assignedGpuId = gpuId;
        startTime = simulatedStartTime;
        status = JobStatus.RUNNING;
    }

    public void recordOneMinuteOfProgress() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Only a running job can make progress.");
        }
        if (elapsedMinutes >= estimatedDurationMinutes) {
            throw new IllegalStateException("The job has already used its full duration.");
        }

        elapsedMinutes++;
    }

    public boolean isReadyToComplete() {
        return status == JobStatus.RUNNING
                && elapsedMinutes >= estimatedDurationMinutes;
    }

    public void complete(LocalDateTime simulatedFinishTime) {
        if (status != JobStatus.RUNNING || !isReadyToComplete()) {
            throw new IllegalStateException("Only a finished running job can complete.");
        }
        if (simulatedFinishTime == null) {
            throw new IllegalArgumentException("Finish time is required.");
        }

        finishTime = simulatedFinishTime;
        status = JobStatus.COMPLETED;
    }

    public void cancel(CancellationInfo info) {
        if (status != JobStatus.QUEUED && status != JobStatus.RUNNING) {
            throw new IllegalStateException("Only queued or running jobs can be cancelled.");
        }
        if (info == null) {
            throw new IllegalArgumentException("Cancellation details are required.");
        }

        cancellationInfo = info;
        finishTime = info.getCancelledAt();
        status = JobStatus.CANCELLED;
    }
}