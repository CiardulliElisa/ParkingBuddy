package ParkingBuddy.dataStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

public class HistoricalData{
	private static final Set<ParkingStation> allStations = ParkingData.findAllLatestData();

	public static void main(String[] args) throws IOException {
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();
        saveFiles(startDate, endDate, allStations);
	}

	public static void saveFiles(LocalDateTime startDate, LocalDateTime endDate, Set<ParkingStation> stations) throws IOException {
		for(String name: getLatestObjects(stations)) {
			try {
					ParkingStation save = ParkingData.getHistoricalData(startDate, endDate, name);
					CSVFile csvFile = new CSVFile();
					if(save != null) {
						String filepath = genFilePathPS(save);
						csvFile.saveData(save, filepath);
					}
				} catch (IllegalArgumentException e) {
				}

		}
	}

	/*method to generate a uniform name for the files, in which historical parking data is stored
	 * input: Parking station to save
	 * Output: String, in which the parking station should be stored
	 * */
	private static String genFilePathPS(ParkingStation station){
		String folder = "./historicalData/";
		return folder + station.getName().replace("/", "-") + ".csv";
	}

    private static Set<String> getLatestObjects(Set<ParkingStation> stations) {    	
        return stations.stream()
                .map(ParkingStation::getName)
                .collect(Collectors.toSet());
    }
}
