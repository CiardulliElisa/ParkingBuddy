package ParkingBuddy.dataStorage;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.reflections.Reflections;

import ParkingBuddy.dataGetter.Coordinate;
import ParkingBuddy.dataGetter.OpenData;
import ParkingBuddy.dataGetter.ParkingStation;

public class CSVFile implements ReadData, SaveData{

    public static void main(String[] args) throws IOException {

//      // get data to store
//      LocalDateTime date =LocalDateTime.now().minusDays(10);
//      LocalDateTime date2 = LocalDateTime.now();
//      ParkingStation save = ParkingData.getHistoricalData(date, date2, "P03 - Piazza Walther");
//
//      //test method to generate file path
//      System.out.println(genFilePathPS(save));
//
//      //save the data into the specified filepath
//      CSVFile csvFile = new CSVFile();
//      String filepath = "./historicalData/test1.csv";
//      csvFile.saveData(save, filepath);
    	
		try {
			CSVFile reader = new CSVFile();
	    	OpenData dataStation = reader.readData("./historicalData/Park Kellerei Algund.csv");
	    	System.out.println("data Station class:" +dataStation.getClass());
	    	System.out.println("data Station:" +dataStation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
  }
    
    @Override
    public OpenData readData(String filepath) {
    	try (
    			Reader reader = new FileReader(filepath);
    			CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
    			) {
    		List<CSVRecord> records = parser.getRecords();
    		
    		if (records.isEmpty()) {
    			throw new IllegalArgumentException("CSV file is empty.");
    		}
    		
    		//get class from list of subclasses
    		Class<? extends OpenData> clazz = findMatchingSubclass(parser.getHeaderMap().keySet());

    		CSVRecord record = records.get(0);
    		OpenData data = clazz.getDeclaredConstructor().newInstance();
    		
    		for (String classVariable : parser.getHeaderMap().keySet()) {
    			Field field = clazz.getDeclaredField(classVariable);
    			field.setAccessible(true);

    			if (Collection.class.isAssignableFrom(field.getType())) {
    				continue;
    			}

    			String raw = record.get(classVariable);
    			Object value = parseValue2(field.getType(), raw, field);
    			field.set(data, value);
    		}
    		
            for (String classVariable : parser.getHeaderMap().keySet()) {
                Field field = clazz.getDeclaredField(classVariable);
                field.setAccessible(true);

                if (Collection.class.isAssignableFrom(field.getType())) {
                    // Determine the generic type of the collection
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> genericClass = (Class<?>) listType.getActualTypeArguments()[0];

                    List<Object> aggregatedValues = new ArrayList<>();

                    for (CSVRecord rec : records) {
                        String fieldStr = rec.get(classVariable);
                        if (fieldStr == null || fieldStr.isEmpty()) {
                            continue;
                        }
                        String[] items = fieldStr.split(";");
                        for (String item : items) {
                            Object parsedItem = parseCollectionItem(genericClass, item.trim());
                            aggregatedValues.add(parsedItem);
                        }
                    }

                    // Assign the aggregated list to the field
                    field.set(data, aggregatedValues);
                }
            }

//    		Field tsField = clazz.getDeclaredField("timestamps");
//    		Field freeField = clazz.getDeclaredField("free_spots");
//    		tsField.setAccessible(true);
//    		freeField.setAccessible(true);
//
//    		List<LocalDateTime> timestamps = new ArrayList<>();
//    		List<Integer> freeSpots = new ArrayList<>();
//    		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//
//    		for (CSVRecord rec : records) {
//    			String tsStr = rec.get("timestamps");
//    			if (tsStr != null && !tsStr.isEmpty()) {
//    				String[] tsStrItems = tsStr.split(";");
//    				for (String tsItem : tsStrItems) {
//    					timestamps.add(LocalDateTime.parse(tsItem.trim(), formatter));
//    				}
//    			}
//
//    			String freeStr = rec.get("free_spots");
//    			if (freeStr != null && !freeStr.isEmpty()) {
//    				String[] freeItems = freeStr.split(";");
//    				for (String freeItem : freeItems) {
//    					freeSpots.add(Integer.parseInt(freeItem.trim()));
//    				}
//    			}
//    		}
//
//    		tsField.set(data, timestamps);
//    		freeField.set(data, freeSpots);

    		return data;

    	} catch (Exception e) {
    		throw new RuntimeException("Failed to read data from CSV", e);
    	}
    }

    private Object parseCollectionItem(Class<?> genericClass, String raw) {
        if (genericClass == LocalDateTime.class) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDateTime.parse(raw, formatter);
        }
        else if (genericClass == Integer.class) {
            return Integer.parseInt(raw);
        }
        else if (genericClass == String.class) {
            return raw;
        }
        // Add other types here as needed
        else {
            throw new IllegalArgumentException("Unsupported collection item type: " + genericClass);
        }
    }

    private static Object parseValue2(Class<?> type, String raw, Field field) throws Exception {
    	if (raw == null || raw.isEmpty()) return null;

    	if (type == String.class) return raw;
    	if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
    	if (type == double.class || type == Double.class) return Double.parseDouble(raw);
    	if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
    	if (type == Coordinate.class) {
    		// e.g. Coordinate{lat=469, lng=1139}
    		raw = raw.replaceAll("[^0-9,.-]", ""); // remove non-numeric stuff
    		String[] parts = raw.split(",");
    		double lat = Double.parseDouble(parts[0]);
    		double lng = Double.parseDouble(parts[1]);
    		return new Coordinate(lng, lat);
    	}

    	if (List.class.isAssignableFrom(type)) {
    		Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    		Class<?> elementType = (Class<?>) genericType;

    		String[] items = raw.split(";");
    		List<Object> list = new ArrayList<>();
    		for (String item : items) {
    			String trimmed = item.trim();
    			if (elementType == Integer.class) {
    				list.add(Integer.parseInt(trimmed));
    			} else if (elementType == LocalDateTime.class) {
    				DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    				list.add(LocalDateTime.parse(trimmed, formatter));
    			} 
    		}
    		return list;
    	}

    	return raw; // fallback
    }
    
    private static Class<? extends OpenData> findMatchingSubclass(Set<String> headers) throws ClassNotFoundException {
    	Reflections reflections = new Reflections("ParkingBuddy.dataGetter");
    	Set<Class<? extends OpenData>> subclasses = reflections.getSubTypesOf(OpenData.class);
    	for (Class<? extends OpenData> clazz : subclasses) {
    		if (matchesHeaders(clazz, headers)) {
    			return clazz;
    		}
    	}
    	throw new IllegalArgumentException("No matching subclass found for headers: " + headers);
    }
    
    private static boolean matchesHeaders(Class<?> clazz, Set<String> headers) {
    	for (String header : headers) {
    		if (!hasFieldInClassHierarchy(clazz, header)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private static boolean hasFieldInClassHierarchy(Class<?> clazz, String fieldName) {
    	Class<?> current = clazz;
    	while (current != null) {
    		try {
    			current.getDeclaredField(fieldName);
    			return true;
    		} catch (NoSuchFieldException e) {
    			current = current.getSuperclass();
    		}
    	}
    	return false;
    }
    
//    private static ParkingStation parseParkingStationData(String jsonResponse) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        JsonNode rootNode = objectMapper.readTree(jsonResponse);
//        JsonNode dataArray = rootNode.get("data");
//
//        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
//            throw new IllegalArgumentException("No data available in JSON response.");
//        }
//
//        JsonNode firstElement = dataArray.get(0);
//
//        String name = firstElement.get("sname").asText();
//        String municipality = firstElement.get("smetadata.municipality").asText();
//        int capacity = firstElement.get("smetadata.capacity").asInt();
//
//        // get the coordinates
//        JsonNode coordinateNode = firstElement.get("scoordinate");
//        double longitude = coordinateNode.get("x").asDouble();
//        double latitude = coordinateNode.get("y").asDouble();
//        Coordinate coordinates = new Coordinate (longitude, latitude);
//
//        ArrayList<LocalDateTime> timestamps = new ArrayList<>();
//        ArrayList<Integer> free_spots = new ArrayList<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
//
//        // Iterate over all elements in the rootNode array
//        for (JsonNode node : dataArray) {
//            String timestampStr = node.get("_timestamp").asText();
//            if (timestampStr == null || timestampStr.isEmpty()) {
//                continue;
//            }
//            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
//            int value = node.get("mvalue").asInt();
//
//            timestamps.add(timestamp);
//            free_spots.add(value);
//        }
//        return new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
//    }

    /*method to generate a uniform name for the files, in which historical parking data is stored
    * input: Parking station to save
    * Output: String, in which the parking station should be stored
    * */

    //NOTE: this method does not have to stay here. Can be written in the method,
    //which is responsible to read and write data (e.g Prediction, automated data request)
    public static String genFilePathPS(ParkingStation station){
        String folder = "./historicalData/";
        return folder + station.getName().replace("/", "-") + ".csv";
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
    
    private Object parseValue(Class<?> type, String raw) {
        if (type == List.class || type == ArrayList.class) {
            // Assume List<String>
            if (raw.isEmpty()) return new ArrayList<>();
            return new ArrayList<>(Arrays.asList(raw.split(";")));
        } else if (type == Point.class) {
            if (raw.isEmpty()) return null;
            String[] parts = raw.split(",");
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(raw);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(raw);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(raw);
        } else {
            return raw;
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
