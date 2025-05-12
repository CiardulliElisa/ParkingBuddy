package ParkingBuddy.dataGetter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.json.JSONFilter;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ParkingData extends GetData {


    // Returns ParkingStation - all data for a certain parking station for a certain interval of time
    // @param startTime and endTime - start and end of the interval of time we are interested in
    // @param code - the code of the parking lot we are interested in
    public static ParkingStation getData(LocalDateTime now, LocalDateTime aYearAgo, String name) throws IOException {

        String accessToken = generateAccessToken();

        try {
            URL apiUrl = new URL(generateURL(now, aYearAgo, name));
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("responseCode == HttpURLConnection.HTTP_OK");
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                }
                scanner.close();
                System.out.println(response);
                return parseParkingStationData(response.toString());

            } else {
                throw new IOException("Response status: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Method to parse JSON response and create a ParkingStationData object
    private static ParkingStation parseParkingStationData(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataArray = rootNode.get("data");

        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
            throw new IllegalArgumentException("No data available in JSON response.");
        }

        JsonNode firstElement = dataArray.get(0);

        int period = firstElement.get("mperiod").asInt();
        int code = firstElement.get("scode").asInt();
        String name = firstElement.get("smetadata.name_en").asText();
        String municipality = firstElement.get("smetadata.municipality").asText();
        int capacity = firstElement.get("smetadata.capacity").asInt();

        // get the coordinates
        JsonNode coordinateNode = firstElement.get("scoordinate");
        double longitude = coordinateNode.get("x").asDouble();
        double latitude = coordinateNode.get("y").asDouble();
        Point coordinates = new Point ((int)longitude, (int) latitude);

        ArrayList<LocalDateTime> timestamps = new ArrayList<>();
        ArrayList<Integer> free_spots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

        // Iterate over all elements in the rootNode array
        for (JsonNode node : dataArray) {
            String timestampStr = node.get("_timestamp").asText();
            if (timestampStr == null || timestampStr.isEmpty()) {
                continue;
            }
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);

            // Get the value (mvalue)
            int value = node.get("mvalue").asInt();

            timestamps.add(timestamp);
            free_spots.add(value);
        }
        ParkingStation p1 = new ParkingStation(name, code, period, municipality, capacity, coordinates, timestamps, free_spots);
        System.out.println(p1);
        return p1;
    }

    private static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    private static String generateURL(LocalDateTime startDate, LocalDateTime endDate, String name) throws UnsupportedEncodingException {
        name = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/" +
                formatDate(startDate) + "/"+
                formatDate(endDate) +
                "?limit=-1&offset=0&where=smetadata.name_en.eq.%22" +
                name +
                "%22&where=tdescription.eq.free&shownull=false&distinct=true&timezone=UTC&select=mvalue,mvalidtime,mperiod,scode,tname,scoordinate,smetadata.capacity,smetadata.name_en,smetadata.municipality";
    };

    public static void main(String[] args) throws IOException {
        ParkingData pd = new ParkingData();
        LocalDateTime start = LocalDateTime.of(2025, 4, 29, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 0);
        String name = "Piazza Walther";
        pd.getData(start, end, name);
        pd.findAll();
    }

    public static Set<String> findAll() {
        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest?limit=-1&offset=0&shownull=false&distinct=true&select=smetadata.name_en";
        Set<String> stations = new HashSet<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(url));
            JsonNode dataArray = root.path("data");

            for (JsonNode entry : dataArray) {
                String name = entry.path("smetadata.name_en").asText();
                if (!"test-en".equalsIgnoreCase(name) && !name.isEmpty()) {
                    stations.add(name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }
}
