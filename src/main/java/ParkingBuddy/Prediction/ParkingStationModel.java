/*
package ParkingBuddy.Prediction;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

import java.io.IOException;
import java.time.LocalDateTime;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import java.time.LocalDateTime;
import java.util.*;


 // Set class index to last attribute
public class ParkingStationModel implements Model {
	private final Classifier model;
	private final Instances datasetStructure;

	public LocalDateTime date;
	public LocalDateTime startDate;
	public LocalDateTime endDate;
	*/
/*public Object[] data;*//*



	 public ParkingStation hourAverage(ParkingStation aLotOfTimestamps){
		 Map<LocalDateTime, Integer> newMap = new HashMap<>();
		 Map<LocalDateTime, Integer> givenMap = aLotOfTimestamps.getTimestampValueMap();
		 List<Map.Entry<LocalDateTime, Integer>> list = new ArrayList<>(givenMap.entrySet());
		 list.sort(Map.Entry.comparingByKey());
		 LocalDateTime key = list.getFirst().getKey();
//		 for(LocalDateTime one : list){
//
//			 while(givenMap.get(givenMap.keySet().iterator().next())){}
//		 }

		 //ParkingStation lessData = new ParkingStation();

		 return null;

	 }

	 public ParkingStationModel(Map<LocalDateTime, Integer> historicalData) throws Exception {
		 ArrayList<Attribute> attributes = new ArrayList<>();
		 attributes.add(new Attribute("hour"));
		 attributes.add(new Attribute("dayOfWeek"));
		 attributes.add(new Attribute("month"));
		 attributes.add(new Attribute("freeSlots")); // target

		 datasetStructure = new Instances("ParkingData", attributes, historicalData.size());
		 datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);

		 // Build training data
		 for (Map.Entry<LocalDateTime, Integer> entry : historicalData.entrySet()) {
			 LocalDateTime dt = entry.getKey();
			 Instance instance = new DenseInstance(4);
			 instance.setValue(attributes.get(0), dt.getHour());
			 instance.setValue(attributes.get(1), dt.getDayOfWeek().getValue());
			 instance.setValue(attributes.get(2), dt.getMonthValue());
			 instance.setValue(attributes.get(3), entry.getValue());
			 datasetStructure.add(instance);
		 }

		 // Train model
		 this.model = new RandomForest();
		 model.buildClassifier(datasetStructure);
	 }

	 public static void main(String [] args) throws IOException {
		 ParkingData data= new ParkingData();
		 LocalDateTime start = LocalDateTime.of(2024, 4, 29, 10, 0);
		 LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 0);
		 int code = 103;
		 ParkingStation station = data.getData(start, end, code);

	 }

*/
/*	@Override
	public void modelData() {
		// TODO Auto-generated method stub
	}*//*


//	@Override
//	public Object[] getPrediction(LocalDateTime date) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	public int getPrediction(LocalDateTime futureDateTime) throws Exception {
		Instance futureInstance = new DenseInstance(4);
		futureInstance.setDataset(datasetStructure);
		futureInstance.setValue(0, futureDateTime.getHour());
		futureInstance.setValue(1, futureDateTime.getDayOfWeek().getValue());
		futureInstance.setValue(2, futureDateTime.getMonthValue());

		double prediction = model.classifyInstance(futureInstance);
		return (int) Math.round(prediction);
	}
}

//	@Override
//	public Object[] getPrediction(LocalDateTime startDate, LocalDateTime endDate) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public static void myTry(){
//		ConverterUtils.DataSource source = new ConverterUtils.DataSource("data/your-data.arff");
//		Instances data = source.getDataSet();
//		if (data.classIndex() == -1)
//			data.setClassIndex(data.numAttributes() - 1);
//	}


*/
