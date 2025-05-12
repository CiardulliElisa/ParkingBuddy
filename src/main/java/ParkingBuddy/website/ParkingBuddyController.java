package ParkingBuddy.website;
import ParkingBuddy.chartPoint.ChartService;
import ParkingBuddy.chartPoint.DataPoint;
import ParkingBuddy.hello.HelloWorld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class ParkingBuddyController{
    private final ChartService chartService = new ChartService();

    @GetMapping("/")
    public String home() {
        return "redirect:/chart"; //opens home.html
    }

    @GetMapping("/chart")
        public String getChart(Model model) throws IOException {
            List<DataPoint> dataPoints = chartService.getDataPoints();
            model.addAttribute("dataPoints", dataPoints);
            return "chart";
        }
    }


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



