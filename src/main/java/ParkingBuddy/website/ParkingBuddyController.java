package ParkingBuddy.website;
import ParkingBuddy.chartPoint.ChartService;
import ParkingBuddy.chartPoint.DataPoint;
import ParkingBuddy.hello.HelloWorld;

import java.util.List;

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
    private final ChartService chartService = new ChartService();

	@GetMapping("/")
    public String home() {
        return "home"; //opens home.html
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


