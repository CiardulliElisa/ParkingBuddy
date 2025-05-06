package ParkingBuddy.website;
import ParkingBuddy.hello.HelloWorld;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParkingBuddyController{

    @GetMapping("/")
    public String home() {
        return "redirect:/hello"; //opens home.html
    }

    //Tutorial - Send information to the page
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("hello", new HelloWorld());
        return "home";
    }

    // Tutorial - Get information from page
    @PostMapping("/hello")
    public String handleTasks(@RequestParam String code,
                              Model model) {
        System.out.println("Received code: " + code);
        model.addAttribute("hello", new HelloWorld());
        return "home";
    }
}


