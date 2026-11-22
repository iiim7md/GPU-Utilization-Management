package logic;

import java.time.Duration;
import java.time.LocalDateTime;

import model.AppState;

public class SimulationClock {

    public void initialize(AppState state, LocalDateTime startTime) {
        requireState(state);
        state.initializeClock(startTime);
    }

    public LocalDateTime getCurrentTime(AppState state) {
        requireInitialized(state);
        return state.getCurrentTime();
    }

    public void advanceOneMinute(AppState state) {
        requireInitialized(state);
        state.advanceClock();
    }

    public long getElapsedMinutes(AppState state) {
        requireInitialized(state);
        return Duration.between(
                state.getInitialTime(), state.getCurrentTime()).toMinutes();
    }

    private void requireState(AppState state) {
        if (state == null) {
            throw new IllegalArgumentException("Application state is required.");
        }
    }

    private void requireInitialized(AppState state) {
        requireState(state);
        if (!state.isClockInitialized()) {
            throw new IllegalStateException("The simulation clock is not initialized.");
        }
    }
}