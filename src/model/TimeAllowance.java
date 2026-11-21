package model;

import java.io.Serializable;

public class TimeAllowance implements Serializable {

    private static final long serialVersionUID = 1L;

    private int allocated;
    private int used;
    private int reserved;

    public TimeAllowance(int initialMinutes) {
        if (initialMinutes <= 0) {
            throw new IllegalArgumentException("Initial minutes must be positive.");
        }

        this.allocated = initialMinutes;
        this.used = 0;
        this.reserved = 0;
    }

    public int getAllocated() {
        return allocated;
    }

    public int getUsed() {
        return used;
    }

    public int getReserved() {
        return reserved;
    }

    public int getAvailable() {
        return allocated - used - reserved;
    }

    public void reserve(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be positive.");
        }

        if (minutes > getAvailable()) {
            throw new IllegalArgumentException("Not enough available GPU minutes.");
        }

        reserved += minutes;
    }

    public void release(int minutes) {
        if (minutes <= 0 || minutes > reserved) {
            throw new IllegalArgumentException("Invalid reservation release.");
        }

        reserved -= minutes;
    }

    public void settle(int reservedMinutes, int usedMinutes) {
        if (reservedMinutes <= 0 ||
            reservedMinutes > reserved ||
            usedMinutes < 0 ||
            usedMinutes > reservedMinutes) {

            throw new IllegalArgumentException("Invalid job settlement.");
        }

        reserved -= reservedMinutes;
        used += usedMinutes;
    }

    public void grantMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Granted minutes must be positive.");
        }

        allocated += minutes;
    }
}
