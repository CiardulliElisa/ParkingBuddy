import java.time.LocalDateTime;

public interface Model {

	/*public void modelData();*/

	// Returns the predictions for that date
	// @params date - the date for which to get the predictions
	public Object[] getPrediction(LocalDateTime date);

	// Returns the predictions for that time period
	// @params startDate - beginning of the time period
	// @params endDate - end of the time period
	public Object[] getPrediction(LocalDateTime startDate, LocalDateTime endDate);

}
