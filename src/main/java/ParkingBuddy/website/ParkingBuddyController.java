package ParkingBuddy.website;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ParkingBuddy.prediction.DataPoint;
import ParkingBuddy.prediction.ParkingStationModel;
import ParkingBuddy.dataGetter.Coordinate;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

@Controller
public class ParkingBuddyController{
    private final Set<ParkingStation> allStations = ParkingData.findAllLatestData();
    private final Set<String> allMunicipalities = ParkingData.getAllMunicipalities();
    private final ModelCacheService modelCacheService;
    public ParkingBuddyController(ModelCacheService modelCacheService)throws MalformedURLException{
        this.modelCacheService = modelCacheService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String municipality, Model model) throws MalformedURLException {

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
    public String predictionFormSubmit(@RequestParam String station, @RequestParam String date, Model model) throws MalformedURLException {
        model.addAttribute("stationNames", allStations);
        int capacity = -1;
        Set<ParkingStation> myStation = ParkingData.getStationLatestData(station);
        for(ParkingStation s : myStation) {
            if(s.getName().equals(station)) {
                capacity = s.getCapacity();
                break;
            }
        }
        return "redirect:/chart?station=" + station + "&date=" + date + "&capacity=" + capacity;
    }

    @GetMapping("/api/stationData")
    @ResponseBody
    public ParkingStation getStationData(@RequestParam String name) throws MalformedURLException {
        Set<ParkingStation> stations = ParkingData.getStationLatestData(name);
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

    /*method to fetch the trend data of the last 7 days for the chart
    * Input: name and maximal capacity of the parking station
    * Output: List of data points to be displayed in the trend chart
    * */
    @GetMapping("/api/dataPoints")
    @ResponseBody
    public List<DataPoint> getDataPoints(@RequestParam String station, @RequestParam String capacity) throws Exception {
        System.out.println("get Data Points for = " + station);
        ParkingStationModel model = modelCacheService.getModel(station);
        return model.getDataPoints(7);
    }

    /*method to fetch the prediction data of the chosen day
     * Input: name and maximal capacity of the parking station, day of the prediction
     * Output: List of data points to be displayed in the prediction
     * */
    @GetMapping("/api/prediction")
    @ResponseBody
    public List<DataPoint> getPrediction(@RequestParam String station, @RequestParam String date, @RequestParam String capacity) throws Exception {
        System.out.println("get prediction for: " + station + " at " + date);
        ParkingStationModel model = modelCacheService.getModel(station);
        //parse the date of type String to LocalDateTime
        String[] dates = date.split("-");
        LocalDateTime dateForPrediction = LocalDateTime.of(
                Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]), 0, 0
        );

        return model.getPrediction(dateForPrediction);
    }

}


