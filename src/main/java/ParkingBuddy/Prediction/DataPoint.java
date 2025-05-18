package ParkingBuddy.Prediction;

import java.time.LocalDateTime;

public class DataPoint {
    public final LocalDateTime timestamp;
    public final int freeSlots;

    public DataPoint(LocalDateTime timestamp, int freeSlots) {
        this.timestamp = timestamp;
        this.freeSlots = freeSlots;
    }
}

