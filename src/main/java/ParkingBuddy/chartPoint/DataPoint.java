package ParkingBuddy.chartPoint;

import java.time.LocalDateTime;

public class DataPoint {
    public int value;
    public String label;
    public DataPoint(int value, String label) {
    }

    public DataPoint(LocalDateTime one, Integer myValue) {
        this.value = myValue;
        this.label = one.toString();
    }
}
