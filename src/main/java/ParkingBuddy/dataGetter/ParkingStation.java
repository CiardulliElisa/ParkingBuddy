package ParkingBuddy.dataGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

public class ParkingStation {

    private String scode;
    private String sname;
    private int mperiod;
    private Map<LocalDateTime, Integer> timestampValueMap;

    // Constructor
    public ParkingStation(String scode, String sname, int mperiod, Map<LocalDateTime, Integer> timestampValueMap) {
        this.scode = scode;
        this.sname = sname;
        this.mperiod = mperiod;
        this.timestampValueMap = timestampValueMap;
    }

    // Getters and Setters
    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public int getMperiod() {
        return mperiod;
    }

    public void setMperiod(int mperiod) {
        this.mperiod = mperiod;
    }

    public Map<LocalDateTime, Integer> getTimestampValueMap() {
        return timestampValueMap;
    }

    public void setTimestampValueMap(Map<LocalDateTime, Integer> timestampValueMap) {
        this.timestampValueMap = timestampValueMap;
    }

    @Override
    public String toString() {
        return "ParkingStationData{" +
                "scode='" + scode + '\'' +
                ", sname='" + sname + '\'' +
                ", mperiod=" + mperiod +
                ", timestampValueMap=" + timestampValueMap +
                '}';
    }
}

