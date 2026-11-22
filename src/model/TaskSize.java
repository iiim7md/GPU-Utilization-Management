package model;

public enum TaskSize {
    SMALL(5),
    MEDIUM(15),
    LARGE(30),
    VERY_LARGE(60);

    private final int durationMinutes;

    TaskSize(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getWorkloadUnits() {
        return durationMinutes;
    }
}