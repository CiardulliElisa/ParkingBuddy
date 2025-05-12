package ParkingBuddy.dataGetter;

import java.awt.*;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class ParkingStation {

    private String municipality;
    private Point coordinates;
    private int capacity;
    private int code;
    private String name;
    private int period;

    private ArrayList<LocalDateTime> timestamps;
    private ArrayList<Integer> free_spots;

    // Constructor
    public ParkingStation(String name, int code, int period, String municipality, int capacity, Point coordinates, ArrayList<LocalDateTime> timestamps, ArrayList<Integer> free_spots) {
        this.code = code;
        this.name = name;
        this.coordinates = coordinates;
        this.period = period;
        this.timestamps = timestamps;
        this.free_spots = free_spots;
        this.capacity = capacity;
        this.municipality = municipality;
    }

    // Getters and Setters
    public ArrayList<Integer> getFree_spots() {
        return free_spots;
    }

    public void setFree_spots(ArrayList<Integer> free_spots) {
        this.free_spots = free_spots;
    }

    public ArrayList<LocalDateTime> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(ArrayList<LocalDateTime> timestamps) {
        this.timestamps = timestamps;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int geCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return "ParkingStationData{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", period=" + period +
                ", period=" + municipality +
                ", period=" + capacity +
                ", coordinates='" + coordinates + '\'' +
                ", timestamps=" + timestamps +
                ", free_spots=" + free_spots +
                '}';
    }
}

