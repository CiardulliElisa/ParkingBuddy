package ParkingBuddy.dataStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import ParkingBuddy.dataGetter.OpenData;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

public class HistoricalData{
	private static final Set<ParkingStation> allStations = ParkingData.findAllLatestData();

	public static void main(String[] args) throws IOException {
        LocalDateTime startDate =LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        saveFiles(startDate, endDate, allStations);
	}

	private static void saveFiles(LocalDateTime startDate, LocalDateTime endDate, Set<ParkingStation> stations) throws IOException {
		for(String name: getLatestObjects(stations)) {
        	try {
        		ParkingStation save = ParkingData.getHistoricalData(startDate, endDate, name);
        		CSVFile csvFile = new CSVFile();
        		String filepath = CSVFile.genFilePathPS(save);
        		csvFile.saveData(save, filepath);
        	} catch (IllegalArgumentException e) {

        	}
        }
	}
	
    public static Set<String> getLatestObjects(Set<ParkingStation> stations) {    	
        return stations.stream()
                .map(ParkingStation::getName)
                .collect(Collectors.toSet());
    }
}
