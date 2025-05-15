package ParkingBuddy.dataGetter;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ParkingStation extends OpenData{

    private String municipality;
    private Coordinate coordinates;
    private int capacity;
    private String name;

    private ArrayList<LocalDateTime> timestamps;
    private ArrayList<Integer> free_spots;

    public ParkingStation() {
        super();
    }

    // Constructor
    public ParkingStation(String name, String municipality, int capacity, Coordinate coordinates, ArrayList<LocalDateTime> timestamps, ArrayList<Integer> free_spots) {
        this.name = name;
        this.coordinates = coordinates;
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

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ParkingStationData{" +
                "name='" + name + '\'' +
                ", municipality=" + municipality +
                ", capacity=" + capacity +
                ", coordinates='" + coordinates + '\'' +
                ", timestamps=" + timestamps +
                ", free_spots=" + free_spots +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParkingStation)) return false;
        ParkingStation other = (ParkingStation) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

