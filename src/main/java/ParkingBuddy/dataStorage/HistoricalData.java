package ParkingBuddy.dataStorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

public class HistoricalData{
	private static final Set<ParkingStation> allStations;

    static {
        try {
            allStations = ParkingData.findAllLatestData();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveHistoricalData() throws IOException {
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();
        saveFiles(startDate, endDate, allStations);
	}

	public static void saveFiles(LocalDateTime startDate, LocalDateTime endDate, Set<ParkingStation> stations) throws IOException {
		System.out.println("Saving parking station data until one year ago into resources/historicaldata...");
		for(String name: getLatestObjects(stations)) {
			try {
					System.out.println("Current station: " + name);
					ParkingStation save = ParkingData.getHistoricalData(startDate, endDate, name);
					CSVFile csvFile = new CSVFile();
					if(save != null) {
				    	String filepath = genFilePathPS(save);
						csvFile.saveData(save, filepath);
					}
				} catch (IllegalArgumentException e) {
				}
		}
		System.out.println("Finished loading.");
	}

	/*method to generate a uniform name for the files, in which historical parking data is stored
	 * input: Parking station to save
	 * Output: String, in which the parking station should be stored
	 * */
	private static String genFilePathPS(ParkingStation station){
		String folder = "./src/main/resources/historicalData/";
		return folder + station.getName().replace("/", "-") + ".csv";
	}

    private static Set<String> getLatestObjects(Set<ParkingStation> stations) {    	
        return stations.stream()
                .map(ParkingStation::getName)
                .collect(Collectors.toSet());
    }
}
