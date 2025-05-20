package ParkingBuddy.website;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ParkingBuddy.Prediction.ParkingStationModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ParkingBuddy.Prediction.DataPoint;
import ParkingBuddy.dataGetter.Coordinate;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

@Controller
public class ParkingBuddyController{

    private static String stationName = "";
    private static String predictionDate = "";
    private final Set<ParkingStation> allStations = ParkingData.findAllLatestData();
    private final Set<String> allMunicipalities = ParkingData.getAllMunicipalities();

    public ParkingBuddyController() throws IOException {
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String municipality, Model model) {

        model.addAttribute("allMunicipalities", allMunicipalities);

        if (municipality != null && !municipality.isEmpty()) {
            Set<ParkingStation> filteredStations = ParkingData.getLatestByMunicipality(municipality);
            model.addAttribute("allStations", filteredStations);
            model.addAttribute("selectedMunicipality", municipality);
        } else {
            model.addAttribute("allStations", allStations);
        }
        return "home";
    }

    @PostMapping("/municipality")
    public String municipalitySelection(@RequestParam String municipality) {
        return "redirect:/?municipality=" + municipality;
    }

    @PostMapping("/home")
    public String predictionFormSubmit(@RequestParam String station, @RequestParam String date, Model model) {
        stationName = station;
        predictionDate = date;
        model.addAttribute("stationNames", allStations);
        return "redirect:/chart?station=" + station + "&date=" + date;
    }

    @GetMapping("/api/stationData")
    @ResponseBody
    public ParkingStation getStationData(@RequestParam String name) {
        Set<ParkingStation> stations = ParkingData.findLatestData(name);
        return stations.stream().findFirst().orElse(null);
    }

    @GetMapping("/api/points")
    @ResponseBody
    public Map<String, Coordinate> getPoints() {    	
    	return allStations.stream()
    		    .collect(Collectors.toMap(
    		        ParkingStation::getName,
    		        ParkingStation::getCoordinates
    		    ));
    }

    @GetMapping("/chart")
    public String getChartPage(@RequestParam String station, @RequestParam String date, Model model) {
        model.addAttribute("station", station);
        model.addAttribute("date", date);
        return "chart";
    }

    @GetMapping("/api/dataPoints")
    @ResponseBody
    public List<DataPoint> getDataPoints() throws Exception {
        System.out.println("get Data Points for = " + stationName);
        ParkingStationModel predModel = new ParkingStationModel(stationName);
        return predModel.getDataPoints();
    }

    @GetMapping("/api/prediction")
    @ResponseBody
    public List<DataPoint> getPrediction() throws Exception {
        System.out.println("get preditcion for: " + stationName);
        ParkingStationModel predModel = new ParkingStationModel(stationName);
        String[] dates = predictionDate.split("-");
        LocalDateTime dateForPrediction = LocalDateTime.of(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]), 0, 0);
        return predModel.getPrediction(dateForPrediction);
    }
}


