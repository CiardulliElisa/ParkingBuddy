package ParkingBuddy.dataStorage;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Override   
    /* 
     * Reads csv data from a specific filepath and creates the corresponding object
     * Input: filepath of csv file
     * Output: created Object
     */
    public OpenData readData(String url) {
        try {			
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt3d3Z1Z2p5Y2NycHZjYnppd2ZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc3OTg0MzMsImV4cCI6MjA2MzM3NDQzM30.zY6HG4u-EcxaTXj2aROltbfR9itfMN-iEYH1Qmcgaxg")
                .GET()
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Fehler beim Download der Datei: HTTP " + response.statusCode());
            }

            try (
                InputStream csvStream = response.body();
                Reader reader = new InputStreamReader(csvStream);
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
            ) {
                List<CSVRecord> records = parser.getRecords();

                if (records.isEmpty()) {
                    throw new IllegalArgumentException("CSV-Datei ist leer");
                }
              //get class from list of subclasses
        		Class<? extends OpenData> clazz = findMatchingSubclass(parser.getHeaderMap().keySet());

        		CSVRecord record = records.get(0);
        		OpenData data = clazz.getDeclaredConstructor().newInstance();

        		//set class variables
        		for (String classVariable : parser.getHeaderMap().keySet()) {
        			Field field = clazz.getDeclaredField(classVariable);
        			field.setAccessible(true);

        			//if variables are of type list
        			if (Collection.class.isAssignableFrom(field.getType())) {
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
        						Object parsedItem = parseValue(genericClass, item.trim(), field);
        						aggregatedValues.add(parsedItem);
        					}
        				}
        				field.set(data, aggregatedValues);
        			} else {
        				String raw = record.get(classVariable);
        				Object value = parseValue(field.getType(), raw, field);
        				field.set(data, value);
        			}
        		}
        		return data;
            }    		
    	} catch (Exception e) {
    		throw new RuntimeException("Failed to read data from CSV", e);
    	}

    }
    
    // Helper method to parse String
    private static Object parseValue(Class<?> type, String raw, Field field) throws Exception {
    	if (raw == null || raw.isEmpty()) return null;
    	else if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
    	else if (type == double.class || type == Double.class) return Double.parseDouble(raw);
    	else if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
    	else if (type == Coordinate.class) {
    		raw = raw.replaceAll("[^0-9,.-]", "");
    		String[] parts = raw.split(",");
    		double lat = Double.parseDouble(parts[0]);
    		double lng = Double.parseDouble(parts[1]);
    		return new Coordinate(lng, lat);
    	}
    	else if (type == LocalDateTime.class) {
    		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    		return LocalDateTime.parse(raw, formatter);
    	}
    	else if (type == String.class) {
    		return raw;
    	} 
    	else {
    		throw new IllegalArgumentException("Unsupported collection item type: " + type);
    	}
    }
    
    //helper method for finding the subclasses of OpenData
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
    
    //helper method to check if the class variables from the csv file match with existing class variables
    private static boolean matchesHeaders(Class<?> clazz, Set<String> headers) {
    	for (String header : headers) {
    		if (!hasFieldInClassHierarchy(clazz, header)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    //helper method that returns true if class variable exists
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
}
