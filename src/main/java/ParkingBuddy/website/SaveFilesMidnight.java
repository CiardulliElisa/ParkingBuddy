package ParkingBuddy.website;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;
import ParkingBuddy.dataStorage.HistoricalData;


@Component
public class SaveFilesMidnight {
	
	// Runs everyday at midnight and saves data from startDate to endDate
    @Scheduled(cron = "0 0 0 * * ?") 
    public void runAtMidnight() throws IOException {
        LocalDateTime startDate = LocalDateTime.now().minusDays(365);
        LocalDateTime endDate = LocalDateTime.now();
        Set<ParkingStation> allStations = ParkingData.findAllLatestData();
        HistoricalData.saveFiles(startDate, endDate, allStations);
    }
}
