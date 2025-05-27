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
	/* Preloads every model into a HashMap, so it's faster during prediction
	 * Input: none
	 * Output: none
	 */
	public void preloadModels() {
		System.out.println(" -> Preloading models...");
		for (OpenData data : allStations) {
			try {
				Model model = new ParkingStationModel(data.getName());
				modelCache.put(data, model);
			} catch (Exception e) {
				System.out.println("Data for '" + data.getName() + "' is empty");
			}
		}
		System.out.println(" -> Finished preloading models");
	}

	//Helper method to find model instance
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

	/* Fetches the preloaded model instance for the input string
	 * Input: name of the OpenData instance we want to fetch the model for
	 * Output: loaded model
	 */
	public Model getModel(String name) {
		OpenData instance = findInstanceByName(name);
		return modelCache.computeIfAbsent(instance, i -> {
			try {
				return new ParkingStationModel(i.getName());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
}

