package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CancellationInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String cancelledBy;
    private final LocalDateTime cancelledAt;
    private final String reason;

    public CancellationInfo(String cancelledBy, LocalDateTime cancelledAt, String reason) {
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
        this.reason = reason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getReason() {
        return reason;
    }
}