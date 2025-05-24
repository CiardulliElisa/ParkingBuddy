package ParkingBuddy.Prediction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataPoint {
    public LocalDateTime timestamp;
    public final int freeSlots;

    public DataPoint(LocalDateTime timestamp, int freeSlots) {
        this.timestamp = timestamp;
        this.freeSlots = freeSlots;
    }
}

