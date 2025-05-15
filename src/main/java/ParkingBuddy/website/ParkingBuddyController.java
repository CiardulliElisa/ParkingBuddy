package ParkingBuddy.website;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ParkingBuddy.chartPoint.DataPoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*import ParkingBuddy.chartPoint.ChartService;*/
import ParkingBuddy.chartPoint.ChartService;
import ParkingBuddy.dataGetter.Coordinate;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

@Controller
public class ParkingBuddyController{
    private final ChartService chartService = new ChartService();
    private final Set<ParkingStation> allStations = ParkingData.findAllLatestData();

	@GetMapping("/")
    public String home(Model model) {
        model.addAttribute("allStations", allStations);
        return "home";
    }

    @PostMapping("/home")
    public String predictionFormSubmit(@RequestParam String station, @RequestParam String date, Model model) {
        return "redirect:/chart";
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
    @GetMapping("/chart")
    public String getChart(Model model) throws IOException {
        List<DataPoint> dataPoints = chartService.getDataPoints();
        model.addAttribute("dataPoints", dataPoints);
        model.addAttribute("stationNames", allStations);
        return "chart";
    }
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


