package ParkingBuddy.dataStorage;

import ParkingBuddy.dataGetter.DataType;
import ParkingBuddy.dataGetter.OpenData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class CSVFile implements ReadData, SaveData{
    private static Map<String, OpenData> dataMap = new HashMap<>();

    @Override
    public Object[] readData(String filepath) {
        return new Object[0];
    }

//    public static void main(String[] args) {
//        CSVFile csvFile = new CSVFile();
//        String filepath = "./data";
//
//        csvFile.saveData(null, filepath);
//    }
    public static void main(String[] args) {}

    @Override
    public boolean saveData(Object[] data, String filepath) {

        loadDataClasses();
        String dataType = data[0].getClass().getDeclaredAnnotation(DataType.class).name();
        OpenData clazz = dataMap.get(dataType);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String[] headers = new String[data.getClass().getDeclaredFields().length];

        for(int a = 0; a < headers.length; a++){
            headers[a] = data.getClass().getDeclaredFields()[a].getName();
        }

        File myCsv = new File (filepath.toString());


        try(Writer writer = new FileWriter("C:/Users/184826/eclipse-workspace_2/Joel/parkingLotData.csv");
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))
        ) {

            for(Object one : data){
                Object [] values = new Object[headers.length];
                for(int a = 0; a < headers.length; a++){
                    Field field = clazz.getClass().getDeclaredField(headers[a]);
                    values[a] = field.get(clazz);
                }
                csvPrinter.printRecord(values);

            }
//            for(ParkingStation one : PiazzaWalther.data) {
//                csvPrinter.printRecord(one._timestamp, one.mperiod, one.mvalue);
//            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void loadDataClasses() {
        Iterator<OpenData> dataTypes = ServiceLoader.
                load(OpenData.class).iterator();
        while(dataTypes.hasNext()) {
            OpenData dataClass = dataTypes.next();
            DataType data = dataClass.getClass().getAnnotation(DataType.class);
            dataMap.put(data.name(), dataClass);
        }
    }

    /*
    * public static void saveData(String jsonData) {

    	ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	Response PiazzaWalther = null;

    	try {

    	    PiazzaWalther = objectMapper.readValue(jsonData, Response.class);


    	} catch (IOException e) {
    	    e.printStackTrace();
    	}

    	File myCsv = new File ("./parkingLotData.csv");
    	Object[] collection = new Object[1];

		String[] headers = new String[] {"timestamp", "mperiod", "mvalue" };

		try(Writer writer = new FileWriter("C:/Users/184826/eclipse-workspace_2/Joel/parkingLotData.csv");
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))
						) {

			for(ParkingStation one : PiazzaWalther.data) {
				csvPrinter.printRecord(one._timestamp, one.mperiod, one.mvalue);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }


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
