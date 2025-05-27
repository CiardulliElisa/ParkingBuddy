package ParkingBuddy.prediction;

import java.time.LocalDateTime;
import java.util.List;

public interface Model{
    /*Returns the predictions, associated with corresponding hour for that date
    * @params date - the date for which to get the predictions
    * */
    List<DataPoint> getPrediction(LocalDateTime futureDateTime) throws Exception;

}
