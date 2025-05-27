package ParkingBuddy.dataStorage;

import ParkingBuddy.dataGetter.OpenData;

public interface ReadData {
    //method to read the data from a file
    OpenData readData(String url);
}
