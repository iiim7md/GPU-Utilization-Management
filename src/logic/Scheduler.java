package logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.AppState;
import model.CancellationInfo;
import model.GPU;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TaskSize;
import model.TimeAllowance;

public class Scheduler {

    public Job submit(AppState state, String studentId, String title,
                      String applicationName, String description,
                      TaskSize taskSize) {
        requireClock(state);
        requireText(studentId, "Student ID");
        requireText(title, "Title");
        requireText(applicationName, "Application name");
        requireText(description, "Description");

        if (taskSize == null) {
            throw new IllegalArgumentException("Task size is required.");
        }

        studentId = studentId.trim();
        Student student = state.getStudents().get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student account was not found.");
        }

        TimeAllowance allowance = student.getAllowance();
        int duration = taskSize.getDurationMinutes();
        if (duration > allowance.getAvailable()) {
            throw new IllegalArgumentException("Not enough available GPU minutes.");
        }

        Job job = new Job(
                state.getNextJobId(), studentId, title, applicationName,
                description, taskSize, state.getCurrentTime());

        allowance.reserve(duration);
        state.getJobs().add(job);
        dispatchWaitingJobs(state);
        return job;
    }

    public void advanceOneMinute(AppState state) {
        requireClock(state);

        // Snapshot first so newly dispatched jobs don't run during this minute.
        List<Job> runningAtStart = new ArrayList<>();
        for (Job job : state.getJobs()) {
            if (job.getStatus() == JobStatus.RUNNING) {
                runningAtStart.add(job);
            }
        }

        state.advanceClock();
        LocalDateTime now = state.getCurrentTime();

        for (Job job : runningAtStart) {
            job.recordOneMinuteOfProgress();

            if (job.isReadyToComplete()) {
                Student owner = state.getStudents().get(job.getStudentId());
                if (owner == null) {
                    throw new IllegalStateException("Job owner was not found.");
                }

                owner.getAllowance().settle(
                        job.getEstimatedDurationMinutes(),
                        job.getElapsedMinutes());

                job.complete(now);
                findAssignedGpu(state, job).releaseJob();
            }
        }

        dispatchWaitingJobs(state);
    }

    public void cancelJob(AppState state, int jobId, String actorId,
                          boolean actorIsAdmin, String reason) {
        requireClock(state);
        requireText(actorId, "Cancelling user");
        requireText(reason, "Cancellation reason");

        Job job = null;
        for (Job candidate : state.getJobs()) {
            if (candidate.getJobId() == jobId) {
                job = candidate;
                break;
            }
        }
        if (job == null) {
            throw new IllegalArgumentException("Job was not found.");
        }

        if (!actorIsAdmin && !job.getStudentId().equals(actorId)) {
            throw new SecurityException("You may cancel only your own jobs.");
        }

        if (job.getStatus() != JobStatus.QUEUED
                && job.getStatus() != JobStatus.RUNNING) {
            throw new IllegalStateException("Only queued or running jobs can be cancelled.");
        }

        Student owner = state.getStudents().get(job.getStudentId());
        if (owner == null) {
            throw new IllegalStateException("Job owner was not found.");
        }

        TimeAllowance allowance = owner.getAllowance();
        int fullReservation = job.getEstimatedDurationMinutes();
        CancellationInfo info =
                new CancellationInfo(actorId, state.getCurrentTime(), reason);

        if (job.getStatus() == JobStatus.QUEUED) {
            allowance.release(fullReservation);
            job.cancel(info);
            return;
        }

        GPU gpu = findAssignedGpu(state, job);
        allowance.settle(fullReservation, job.getElapsedMinutes());
        job.cancel(info);
        gpu.releaseJob();
        dispatchWaitingJobs(state);
    }

    private void dispatchWaitingJobs(AppState state) {
        while (true) {
            Job next = findNextQueuedJob(state);
            GPU freeGpu = findLowestAvailableGpu(state);

            if (next == null || freeGpu == null) {
                return;
            }

            next.startOnGpu(freeGpu.getId(), state.getCurrentTime());
            freeGpu.assignJob(next);
        }
    }

    private Job findNextQueuedJob(AppState state) {
        Job next = null;

        for (Job candidate : state.getJobs()) {
            if (candidate.getStatus() != JobStatus.QUEUED) {
                continue;
            }

            if (next == null
                    || candidate.getSubmissionTime().isBefore(next.getSubmissionTime())
                    || (candidate.getSubmissionTime().equals(next.getSubmissionTime())
                        && candidate.getJobId() < next.getJobId())) {
                next = candidate;
            }
        }

        return next;
    }

    private GPU findLowestAvailableGpu(AppState state) {
        GPU lowest = null;

        for (GPU gpu : state.getGpus()) {
            if (gpu.isAvailable()
                    && (lowest == null || gpu.getId() < lowest.getId())) {
                lowest = gpu;
            }
        }

        return lowest;
    }

    private GPU findAssignedGpu(AppState state, Job job) {
        Integer gpuId = job.getAssignedGpuId();
        if (gpuId == null) {
            throw new IllegalStateException("Running job has no assigned GPU.");
        }

        for (GPU gpu : state.getGpus()) {
            if (gpu.getId() == gpuId && gpu.getCurrentJob() == job) {
                return gpu;
            }
        }

        throw new IllegalStateException("Job's assigned GPU was not found.");
    }

    private void requireClock(AppState state) {
        if (state == null || !state.isClockInitialized()) {
            throw new IllegalStateException("The simulation clock is not initialized.");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }
}