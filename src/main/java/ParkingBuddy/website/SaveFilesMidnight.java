package ParkingBuddy.website;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;
import ParkingBuddy.dataStorage.HistoricalData;

/*
@Component
public class SaveFilesMidnight implements CommandLineRunner{

	//updates historical data every time spring boot application gets opened
	@Override
	public void run(String... args) throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(365);
        LocalDateTime endDate = LocalDateTime.now();
        Set<ParkingStation> allStations = ParkingData.findAllLatestData();
        HistoricalData.saveFiles(startDate, endDate, allStations);
	}
}*/
