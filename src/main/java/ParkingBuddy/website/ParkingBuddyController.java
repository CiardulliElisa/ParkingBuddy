package ParkingBuddy.website;
/*import ParkingBuddy.chartPoint.ChartService;*/
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class ParkingBuddyController{
    //private final ChartService chartService = new ChartService();

	@GetMapping("/")
    public String home(Model model) {
        Set<String> stationNames = ParkingData.findAll();
        model.addAttribute("stationNames", stationNames);
        return "home"; // Opens home.html
    }

    @GetMapping("/api/stationData")
    @ResponseBody
    public ParkingStation getStationData(@RequestParam String name) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneYearAgo = now.minusMonths(1);
            ParkingStation station = ParkingData.getData(oneYearAgo, now, name);
            if (station != null) {
                System.out.println("Found parking station: " + station);
            } else {
                System.out.println("Parking station not found for: " + name);
            }
            return station;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //will be removed
    @GetMapping("/api/points")
    @ResponseBody
    public List<Point> getPoints() {
    	return List.of(
    			new Point("Sarntal", 46.638780, 11.350111),
    			new Point("Location B", 34.0522, -118.2437)
    			);
    }


    public static class Point {
        public String name;
        public double lat;
        public double lng;

        public Point(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }


//shows how to display a graph
//    @GetMapping("/")
//    public String home() {
//        return "redirect:/chart"; //opens home.html
//    }
//
//    @GetMapping("/chart")
//    public String getChart(Model model) throws IOException {
//        List<DataPoint> dataPoints = chartService.getDataPoints();
//        model.addAttribute("dataPoints", dataPoints);
//        return "chart";
//    }
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


