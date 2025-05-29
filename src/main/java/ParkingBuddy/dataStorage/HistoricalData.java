package ParkingBuddy.dataStorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

public class HistoricalData{
	private static final Set<ParkingStation> allStations;

    /* main method to manually update historical data
     * (you can change the interval manually by modifying startDate and endDate)
     */
    public static void main(String[] args) throws IOException {
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now();
        saveFiles(startDate, endDate, allStations);
	}
    
    static {
        try {
            allStations = ParkingData.getAllStations();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /* Method to save the latest data of all parking stations up to one year ago 
     * Input: startDate and endDate of the interval to be saved and set of all stations to save
     * Output: none (csv files are generated)
     */
	public static void saveFiles(LocalDateTime startDate, LocalDateTime endDate, Set<ParkingStation> stations) throws IOException {
		System.out.println(" -> Saving parking station data up to one year ago into resources/historicaldata...");
		for(String name: getLatestObjects(stations)) {
			System.out.println("Current station: " + name);
			try {
					ParkingStation save = ParkingData.getHistoricalData(startDate, endDate, name);
					CSVFile csvFile = new CSVFile();
					if(save != null) {
				    	String filepath = genFilePathPS(save.getName());
						csvFile.saveData(save, filepath);
					}
				} catch (IllegalArgumentException e) {
				}
		}
		System.out.println(" -> Finished loading.");
	}

	/*method to generate a uniform name for the files, in which historical parking data is stored
	 * input: Parking station to save
	 * Output: String, in which the parking station should be stored
	 * */
	private static String genFilePathPS(String station){
		String folder = "./src/main/resources/historicalData/";
		return folder + station.replace("/", "-") + ".csv";
	}

	//helper method that saves the names of the stations inside a set
    private static Set<String> getLatestObjects(Set<ParkingStation> stations) {    	
        return stations.stream()
                .map(ParkingStation::getName)
                .collect(Collectors.toSet());
    }
}
