package ParkingBuddy.website;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncJobService {

	private final Map<String, String> jobs = new ConcurrentHashMap<>();

	public String startJob() {
		String jobId = UUID.randomUUID().toString();
		jobs.put(jobId, "IN_PROGRESS");
		runLongTask(jobId);
		return jobId;
	}

	@Async
	public void runLongTask(String jobId) {
		try {
			Thread.sleep(15000);
			jobs.put(jobId, "DONE: Task completed successfully.");
		} catch (InterruptedException e) {
			jobs.put(jobId, "ERROR: Task was interrupted.");
		}
	}
	
	@Async
	public void runLongTask(String jobId) {
	    try {
	        Thread.sleep(15000);
	        jobs.put(jobId, "DONE");
	    } catch (Exception e) {
	        jobs.put(jobId, "ERROR: " + e.getMessage());
	        e.printStackTrace(); // log to Railway
	    }
	}

	public String getStatus(String jobId) {
		return jobs.getOrDefault(jobId, "NOT_FOUND");
	}
}

