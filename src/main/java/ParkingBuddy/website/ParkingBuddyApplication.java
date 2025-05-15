package ParkingBuddy.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkingBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingBuddyApplication.class, args);
	}
}
