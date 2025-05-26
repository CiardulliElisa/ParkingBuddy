package ParkingBuddy.website;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import ParkingBuddy.dataGetter.OpenData;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;
import ParkingBuddy.prediction.Model;
import ParkingBuddy.prediction.ParkingStationModel;
import jakarta.annotation.PostConstruct;

@Service
public class ModelCacheService {
	  private final Map<OpenData, Model> modelCache = new HashMap<>();
	  private final Set<ParkingStation> allStations = ParkingData.findAllLatestData();
	  
	  public ModelCacheService() throws MalformedURLException{
	  }

	    @PostConstruct
	    /* Preloads every model into a HashMap, so we don't have to do it later
	     * Input: none
	     * Output: none
	     */
	    public void preloadModels() {
	        for (OpenData data : allStations) {
	            try {
	                Model model = new ParkingStationModel(data.getName());
	                modelCache.put(data, model);
	            } catch (Exception e) {
	                System.out.println("Failed to load model for: " + data.getName() + " since data is empty");
	            }
	        }
	        System.out.println("Finished preloading models");
	    }

	    /* 
	     * 
	     */
	    private OpenData findInstanceByName(String name) {
	    	String normalized = normalize(name);
	        return allStations.stream()
	            .filter(s -> normalize(s.getName()).equals(normalized))
	            .findFirst()
	            .orElseThrow(() -> new IllegalArgumentException("Unknown instance: " + name));
	    }
	    
	    // Helper method to make sure characters 'ä' and 'à' get read correctly
	    private String normalize(String text) {
	    	return text.replaceAll("�", "").replaceAll("ä", "").replaceAll("à", "");
	    }

	    /* Gets the correct Model instance for the input string
	     * 
	     */
	    public ParkingStationModel getModel(String name) {
	        OpenData instance = findInstanceByName(name);
	        return (ParkingStationModel) modelCache.computeIfAbsent(instance, i -> {
	            try {
	                return new ParkingStationModel(i.getName());
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        });
	    }
}
