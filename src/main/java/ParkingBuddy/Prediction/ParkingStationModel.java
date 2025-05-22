
package ParkingBuddy.Prediction;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ParkingBuddy.dataGetter.ParkingStation;
import ParkingBuddy.dataStorage.CSVFile;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;



// Set class index to last attribute
public class ParkingStationModel implements Model {
     public final Classifier model;
     private final Instances datasetStructure;
     public final List<DataPoint> listOfPoints;

     private List<DataPoint> pointsToList(ParkingStation parkingStation) {
         ArrayList<Integer> freeSpots = parkingStation.getFree_spots();
         ArrayList<LocalDateTime> time = parkingStation.getTimestamps();
         List<DataPoint> dataPoints = new ArrayList<>();
         for(int count = 0; count < parkingStation.getFree_spots().size(); count++) {
             DataPoint aPoint = new DataPoint(time.get(count), freeSpots.get(count));
             dataPoints.add(aPoint);
         }
         return dataPoints;
     }

     public ParkingStationModel(String station) throws Exception {
         System.out.println("Loading parking station data for: '" + station + "'");
         String filepath = genFilePathPS2(station);
         System.out.println(filepath);
         CSVFile csv = new CSVFile();
         ParkingStation parkingStation = (ParkingStation) csv.readData(filepath);
         List<DataPoint> historicalData = pointsToList(parkingStation);
         historicalData = reduceDataPoints(historicalData);
         this.listOfPoints = historicalData;

         ArrayList<Attribute> attributes = new ArrayList<>();
         attributes.add(new Attribute("hour"));
         attributes.add(new Attribute("dayOfWeek"));
         attributes.add(new Attribute("month"));
         attributes.add(new Attribute("freeSlots")); // target

         datasetStructure = new Instances("ParkingData", attributes, historicalData.size());
         datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);

         for (DataPoint data : historicalData) {
             LocalDateTime dt = data.timestamp;
             Instance instance = new DenseInstance(4);
             instance.setValue(attributes.get(0), dt.getHour());
             instance.setValue(attributes.get(1), dt.getDayOfWeek().getValue());
             instance.setValue(attributes.get(2), dt.getMonthValue());
             instance.setValue(attributes.get(3), data.freeSlots);
             datasetStructure.add(instance);
         }

         this.model = new RandomForest();
         model.buildClassifier(datasetStructure);
     }



     public List<DataPoint> getPrediction(LocalDateTime futureDateTime) throws Exception {
         List<DataPoint> newList = new ArrayList<>();
         for(int count = 0; count < 24; count++) {
             LocalDateTime iterator = LocalDateTime.of(futureDateTime.getYear(), futureDateTime.getMonthValue(), futureDateTime.getDayOfMonth(), (count), 0);
             Instance futureInstance = new DenseInstance(4);
             futureInstance.setDataset(datasetStructure);
             futureInstance.setValue(0, iterator.getHour());
             futureInstance.setValue(1, iterator.getDayOfWeek().getValue());
             futureInstance.setValue(2, iterator.getMonthValue());
             newList.add(new DataPoint(iterator,(int) Math.round(model.classifyInstance(futureInstance))));
         }

         return newList;
     }


     /*method to generate a uniform name for the files, in which historical parking data is stored
      * input: Parking station to save
      * Output: URL for supabase, where the parking station should be stored
      * */     
     private static String genFilePathPS2(String station) {
    	 String url = "https://kwwvugjyccrpvcbziwfj.supabase.co/storage/v1/object/public/historicaldata/";
    	 String encoded = URLEncoder.encode(station.replace("/", ""), StandardCharsets.UTF_8);
    	encoded = encoded.replace("+", "%20");
    	 return url + encoded + ".csv";
     }

    public static List<DataPoint> reduceDataPoints(List<DataPoint> listOfPoints) {
        List<DataPoint> newList = new ArrayList<>();
        for(int i = 0; i < listOfPoints.size(); i++){
            LocalDateTime timestamp = listOfPoints.get(i).timestamp;
            LocalDateTime timestamp2 = timestamp.plusHours(1);
            int average = 0;
            int number = 0;
            while(listOfPoints.get(i).timestamp.isBefore(timestamp2)){
                average += listOfPoints.get(i).freeSlots;
                number++;
                i++;
                if(i == listOfPoints.size()){
                    break;
                }
            }
            if(number != 0) {
                newList.add(new DataPoint(timestamp, (average / number)));
            }
            i--;

        }
        return newList;
    }


    public List<DataPoint> getDataPoints() {
        List<DataPoint> deleteSome = new ArrayList<>();
        for(DataPoint aPoint : this.listOfPoints){
            if(aPoint.timestamp.isAfter(LocalDateTime.now().minusDays(7))){
                deleteSome.add(aPoint);
            }
        }
        return deleteSome;
    }

 }


