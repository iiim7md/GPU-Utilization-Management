package logic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AppState;
import model.GPU;
import model.Job;
import model.JobStatus;

/** Forecasts queue positions without mutating the real GPU assignments. */
public class PredictionService {
    public static final class JobPrediction {
        private final int position;
        private final int gpuId;
        private final LocalDateTime start;
        private final LocalDateTime finish;
        private final long wait;
        public JobPrediction(int position, int gpuId, LocalDateTime start, LocalDateTime finish, long wait) {
            this.position = position; this.gpuId = gpuId; this.start = start; this.finish = finish; this.wait = wait;
        }
        public int getQueuePosition() { return position; }
        public int getPredictedGpuId() { return gpuId; }
        public LocalDateTime getPredictedStart() { return start; }
        public LocalDateTime getPredictedFinish() { return finish; }
        public long getRemainingWaitMinutes() { return wait; }
    }
    public Map<Integer, JobPrediction> predict(AppState state) {
        Map<Integer, JobPrediction> result = new HashMap<>();
        if (!state.isClockInitialized()) return result;
        LocalDateTime now = state.getCurrentTime();
        Map<Integer, LocalDateTime> freeAt = new HashMap<>();
        for (GPU gpu : state.getGpus()) {
            Job running = gpu.getCurrentJob();
            freeAt.put(gpu.getId(), running == null ? now : now.plusMinutes(running.getRemainingMinutes()));
        }
        List<Job> queue = new ArrayList<>();
        for (Job job : state.getJobs()) if (job.getStatus() == JobStatus.QUEUED) queue.add(job);
        queue.sort(Comparator.comparing(Job::getSubmissionTime).thenComparingInt(Job::getJobId));
        int position = 0;
        for (Job job : queue) {
            int gpuId = freeAt.keySet().stream().min(Comparator.comparing((Integer i) -> freeAt.get(i))
                    .thenComparingInt(i -> i)).orElseThrow();
            LocalDateTime start = freeAt.get(gpuId);
            LocalDateTime finish = start.plusMinutes(job.getEstimatedDurationMinutes());
            result.put(job.getJobId(), new JobPrediction(++position, gpuId, start, finish,
                    Math.max(0, Duration.between(now, start).toMinutes())));
            freeAt.put(gpuId, finish);
        }
        return result;
    }
}
