package ParkingBuddy.dataStorage;

import ParkingBuddy.dataGetter.OpenData;
import java.io.IOException;

public interface SaveData {
    //saves an OpenData Object into the specified filepath
    boolean saveData(OpenData data, String filepath) throws IOException;
}
