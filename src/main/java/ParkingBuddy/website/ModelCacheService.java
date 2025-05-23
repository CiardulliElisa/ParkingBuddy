package ParkingBuddy.website;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import ParkingBuddy.Prediction.ParkingStationModel;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;
import jakarta.annotation.PostConstruct;

@Service
public class ModelCacheService {
	  private final Map<ParkingStation, ParkingStationModel> modelCache = new ConcurrentHashMap<>();
	  private final Set<ParkingStation> allStations = ParkingData.findAllLatestData();
	  
	  public ModelCacheService() throws MalformedURLException{
		  
	  }

	    @PostConstruct
	    public void preloadModels() {
	        for (ParkingStation station : allStations) {
	            try {
	                System.out.println("Preloading model for: " + station.getName());
	                ParkingStationModel model = new ParkingStationModel(station.getName());
	                modelCache.put(station, model);
	            } catch (Exception e) {
	                System.out.println("Failed to load model for station: " + station.getName() + " since data is empty");
	            }
	        }
	        System.out.println("Finished preloading models");
	    }

	    public ParkingStation findStationByName(String name) {
	        return allStations.stream()
	            .filter(s -> s.getName().equals(name))
	            .findFirst()
	            .orElseThrow(() -> new IllegalArgumentException("Unknown station: " + name));
	    }

	    public ParkingStationModel getModel(String stationName) {
	        ParkingStation station = findStationByName(stationName);
	        return modelCache.computeIfAbsent(station, s -> {
	            try {
	                return new ParkingStationModel(s.getName());
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        });
	    }
}
