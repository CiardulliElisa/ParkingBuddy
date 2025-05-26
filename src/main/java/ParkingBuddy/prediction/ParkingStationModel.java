
package ParkingBuddy.prediction;

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


public class ParkingStationModel implements Model {
     public final Classifier model;
     private final Instances datasetStructure;
     public final List<DataPoint> listOfPoints;

     /*constructor: generates a model object for the given station name
     * Input: name of the parking station
     * */
     public ParkingStationModel(String station) throws Exception {
         System.out.println("Loading parking station data for: '" + station + "'");
         //get the Parking station object by name
         String filepath = genFilePathPS(station);
         CSVFile csv = new CSVFile();
         ParkingStation parkingStation = (ParkingStation) csv.readData(filepath);
         List<DataPoint> historicalData = pointsToList(parkingStation);
         this.listOfPoints = reduceDataPoints(historicalData);

         //specify the attributes, which should be respected for the prediction model
         ArrayList<Attribute> attributes = new ArrayList<>();
         attributes.add(new Attribute("hour"));
         attributes.add(new Attribute("dayOfWeek"));
         attributes.add(new Attribute("month"));
         attributes.add(new Attribute("freeSlots")); // target

         //generate the data structure, respecting all points of the historical data
         datasetStructure = new Instances("ParkingData", attributes, historicalData.size());
         datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);

         for (DataPoint data : this.listOfPoints) {
             LocalDateTime dt = data.timestamp;
             Instance instance = new DenseInstance(4);
             instance.setValue(attributes.get(0), dt.getHour());
             instance.setValue(attributes.get(1), dt.getDayOfWeek().getValue());
             instance.setValue(attributes.get(2), dt.getMonthValue());
             instance.setValue(attributes.get(3), data.value);
             datasetStructure.add(instance);
         }

         //generate the model with the datasetStructure
         this.model = new RandomForest();
         model.buildClassifier(datasetStructure);
     }

    /*helper method to transform the timestamp and freeSlots attributes of the ParkingStation to a List of DataPoints
     * Input: parkingStation to extract the data
     * Output: List of data points
     * */
    private List<DataPoint> pointsToList(ParkingStation parkingStation) {
        ArrayList<Integer> freeSpots = parkingStation.getFree_spots();
        ArrayList<LocalDateTime> time = parkingStation.getTimestamps();
        List<DataPoint> dataPoints = new ArrayList<>();
        //add a new point to the list for each array instance
        for(int count = 0; count < parkingStation.getFree_spots().size(); count++) {
            DataPoint aPoint = new DataPoint(time.get(count), freeSpots.get(count));
            dataPoints.add(aPoint);
        }
        return dataPoints;
    }

    /*computes the prediction for each hour of a given day
    * Input: day, for which the prediction should be made
    * Output: list of data Points, associating the 24 hours with a prediction value
    * */
    @Override
     public List<DataPoint> getPrediction(LocalDateTime futureDateTime) throws Exception {
         List<DataPoint> newList = new ArrayList<>();
         //generates one prediction for each hour of the day
         for(int count = 0; count < 24; count++) {
             LocalDateTime iterator = LocalDateTime.of(futureDateTime.getYear(), futureDateTime.getMonthValue(), futureDateTime.getDayOfMonth(), (count), 0);
             Instance futureInstance = new DenseInstance(4);
             futureInstance.setDataset(datasetStructure);
             //set the values of the date, which should be respected in the prediction
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
     private static String genFilePathPS(String station) {
  		String folder = "./src/main/resources/historicalData/";
  		return folder + station.replace("/", "-") + ".csv";
     }

     /*reduces the number of DataPoints, so the model can be computed faster
     * Input: List of DataPoints with a distance of less than one hour
     * Output: average of the input data points to get one point per hour
     * */
    private static List<DataPoint> reduceDataPoints(List<DataPoint> listOfPoints) {
        List<DataPoint> newList = new ArrayList<>();
        //iterate over all points
        for(int i = 0; i < listOfPoints.size(); i++){
            LocalDateTime timestamp = listOfPoints.get(i).timestamp;
            LocalDateTime timestamp2 = timestamp.plusHours(1);
            int average = 0;
            int number = 0;
            //compute the average
            while(listOfPoints.get(i).timestamp.isBefore(timestamp2)){
                average += listOfPoints.get(i).value;
                number++;
                i++;
                if(i == listOfPoints.size()){
                    break;
                }
            }
            //add the new data point with the average values to the new list
            if(number != 0) {
                newList.add(new DataPoint(timestamp, (average / number)));
            }
            i--;

        }
        return newList;
    }


    /*returns the data points for the given time interval in the past
    * Input: number of days for which the data points should be returned
    * Output: list of data point containing the data of the last .(range). days
    * */
    public List<DataPoint> getDataPoints(int range) {
        List<DataPoint> deleteSome = new ArrayList<>();
        for(DataPoint aPoint : this.listOfPoints){
            if(aPoint.timestamp.isAfter(LocalDateTime.now().minusDays(range))){
                deleteSome.add(aPoint);
            }
        }
        return deleteSome;
    }

 }


