package ParkingBuddy.prediction;

import java.time.LocalDateTime;

//class, that associates a timestamp with a value
public class DataPoint {
    public final LocalDateTime timestamp;
    public final int value;

    //constructor
    public DataPoint(LocalDateTime timestamp, int value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}

