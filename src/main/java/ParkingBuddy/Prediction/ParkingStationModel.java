import java.time.LocalDateTime;

public class ParkingStationModel implements Model {

	public LocalDateTime date;
	public LocalDateTime startDate;
	public LocalDateTime endDate;
	/*public Object[] data;*/

/*	@Override
	public void modelData() {
		// TODO Auto-generated method stub
	}*/

	@Override
	public Object[] getPrediction(LocalDateTime date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getPrediction(LocalDateTime startDate, LocalDateTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

}
