package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TimeGrant implements Serializable {

    private static final long serialVersionUID = 1L;

    private String studentId;
    private int minutes;
    private String reason;
    private String grantedBy;
    private LocalDateTime grantTime;

    public TimeGrant(String studentId, int minutes,
                     String reason, String grantedBy,
                     LocalDateTime grantTime) {

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        if (minutes <= 0) {
            throw new IllegalArgumentException("Granted minutes must be positive.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Grant reason is required.");
        }

        if (grantedBy == null || grantedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Administrator ID is required.");
        }

        if (grantTime == null) {
            throw new IllegalArgumentException("Grant time is required.");
        }

        this.studentId = studentId.trim();
        this.minutes = minutes;
        this.reason = reason.trim();
        this.grantedBy = grantedBy.trim();
        this.grantTime = grantTime;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getReason() {
        return reason;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public LocalDateTime getGrantTime() {
        return grantTime;
    }
}
