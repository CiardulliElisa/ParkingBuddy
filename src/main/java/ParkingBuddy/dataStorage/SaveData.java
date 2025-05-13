package ParkingBuddy.dataStorage;

import ParkingBuddy.dataGetter.OpenData;

import java.io.IOException;

public interface SaveData {
    boolean saveData(OpenData data, String filepath) throws IOException;
}
