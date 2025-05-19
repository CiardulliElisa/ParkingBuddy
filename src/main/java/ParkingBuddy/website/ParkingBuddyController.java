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
    private Set<ParkingStation> allStations = ParkingData.findAllLatestData();
    private final Set<String> allMunicipalities = ParkingData.getAllMunicipalities();

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
        model.addAttribute("stationNames", allStations);

        return "redirect:/chart?station=" + station + "&date=" + date;
    }

    //@PostMapping("/chart")
    //public String predictionFormSubmit(@RequestParam String station, @RequestParam String date, Model model) {
    //    return "redirect:/chart?station=" + station + "&date=" + date;
    //}

    @GetMapping("/chart")
    public String getChart(@RequestParam String station,
                           @RequestParam String date,
                           Model model) throws IOException {
        List<DataPoint> dataPoints = null;
        model.addAttribute("dataPoints", dataPoints);
        model.addAttribute("station", station);
        model.addAttribute("date", date);
        return "chart";
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


//shows how to display a graph
//    @GetMapping("/")
//    public String home() {
//        return "redirect:/chart"; //opens home.html
//    }
//
//}



//    @GetMapping("/")
//    public String home() {
//        return "redirect:/hello"; //opens home.html
//    }
//    
//    //Tutorial - Send information to the page
//    @GetMapping("/hello")
//    public String hello(Model model) {
//        model.addAttribute("hello", new HelloWorld());
//        return "home";
//    }
//
//    // Tutorial - Get information from page
//    @PostMapping("/hello")
//    public String handleTasks(@RequestParam String code,
//                              Model model) {
//        System.out.println("Received code: " + code);
//        model.addAttribute("hello", new HelloWorld());
//        return "home";
//    }
}


