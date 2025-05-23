package ParkingBuddy.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ParkingBuddyApplication {

	public static void main(String[] args) {
	    String port = System.getenv("PORT");
	    System.out.println("PORT from Railway: " + port);
		SpringApplication.run(ParkingBuddyApplication.class, args);
	}
}