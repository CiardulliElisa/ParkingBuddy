package ParkingBuddy.dataStorage;

import ParkingBuddy.dataGetter.OpenData;
import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.stream.StreamSupport;

public class CSVFile implements ReadData, SaveData{

    @Override
    public OpenData readData(String filepath) {
        return null;
    }

    public static void main(String[] args) throws IOException {

        // get data to store
        LocalDateTime date =LocalDateTime.now().minusDays(10);
        LocalDateTime date2 = LocalDateTime.now();
        ParkingStation save = ParkingData.getHistoricalData(date, date2, "P03 - Piazza Walther");

        //test method to generate file path
        System.out.println(genFilePathPS(save));

        //save the data into the specified filepath
        CSVFile csvFile = new CSVFile();
        String filepath = "./historicalData/test1.csv";
        csvFile.saveData(save, filepath);
    }

    /*method to generate a uniform name for the files, in which historical parking data is stored
    * input: Parking station to save
    * Output: String, in which the parking station should be stored
    * */

    //NOTE: this method does not have to stay here. Can be written in the method,
    //which is responsible to read and write data (e.g Prediction, automated data request)
    public static String genFilePathPS(ParkingStation station){
        String folder = "./historicalData/";
        return folder + station.getName() + ".csv";
    }

    @Override
    /*Stores data into a specified filepath in csv format
    * Input: subclass object of the OpenData class, path of the csv file
    * Output: true, iff the storage was successful
    * */
    public boolean saveData(OpenData data, String filepath) throws IOException {
        if(data == null){
            return false;
        }

        //create the line of headers for the file
        String[] headers = new String[data.getClass().getDeclaredFields().length];
        for(int a = 0; a < headers.length; a++){
            headers[a] = data.getClass().getDeclaredFields()[a].getName();
        }

        //create the file, if it is missing
        File myCsv = new File (filepath);
        try(Writer writer = new FileWriter(myCsv.getAbsolutePath());
            //writes the headers to the file
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))
        ) {

            //get the attribute values of the object by name and write their content into the file
            Object [] values = new Object[headers.length];
            for(int a = 0; a < headers.length; a++) {
                Field field = data.getClass().getDeclaredField(headers[a]);
                field.setAccessible(true);
                //calls help method to format the attributes as sense full strings
                values[a] = formatValue(field.get(data));
            }

            csvPrinter.printRecord(values);

        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    //help method to format the input value to a proper string
    private String formatValue(Object value) {
        if (value instanceof Iterable<?> iterable) {
            return String.join(";",
                    StreamSupport.stream(iterable.spliterator(), false)
                            .map(Object::toString)
                            .toArray(String[]::new));
        } else if (value instanceof Point point) {
            return point.getX() + "," + point.getY();
        } else if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }


    //alte methode zum lesen von datenfile
    /*

    private static int getPrediction(String data, String dayOfWeek, String startingHour, String endingHour) {
//    	int day = LocalDate.now().getDayOfWeek().toString().compareTo(dayOfWeek);
//    	LocalDate beforeOneWeek = LocalDate.now().minusDays(day);
    	ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	Response PiazzaWalther = null;

    	try {

    	    PiazzaWalther = objectMapper.readValue(data, Response.class);


    	} catch (IOException e) {
    	    e.printStackTrace();
    	}

    	List<Integer> freeLots = new ArrayList<>();

    	for(ParkingStation one : PiazzaWalther.data) {
    		if(one._timestamp.contains(LocalDate.now().toString())) {
    			freeLots.add(one.mvalue);
    		}
    	}


    	int numbers = freeLots.size();
    	int freeAdd = 0;
    	for(Integer one : freeLots) {
    		freeAdd += one;
    	}

    	int prediction = freeAdd/numbers;
    	System.out.println(prediction);

    	return prediction;
    }
    *
    * */
}
