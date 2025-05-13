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
import java.util.List;
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
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                }
                scanner.close();
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

        String name = firstElement.get("sname").asText();

        int period = firstElement.get("mperiod").asInt();
        int code = firstElement.get("scode").asInt();
        String municipality = firstElement.get("smetadata.municipality").asText();
        int capacity = firstElement.get("smetadata.capacity").asInt();

        // get the coordinates
        JsonNode coordinateNode = firstElement.get("scoordinate");
        double longitude = coordinateNode.get("x").asDouble();
        double latitude = coordinateNode.get("y").asDouble();
        Coordinate coordinates = new Coordinate (longitude, latitude);

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
            int value = node.get("mvalue").asInt();

            timestamps.add(timestamp);
            free_spots.add(value);
        }
        return new ParkingStation(name, code, period, municipality, capacity, coordinates, timestamps, free_spots);
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
                "?limit=-1&offset=0&where=sname.eq.%22" +
                name +
                "%22&where=tname.eq.free&shownull=false&distinct=true&timezone=UTC&select=mvalue,mvalidtime,mperiod,scode,tname,scoordinate,smetadata.capacity,sname,smetadata.municipality";
    };

    public static Set<ParkingStation> findLatestData(String nameInput) {
        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest?limit=-1&offset=0&shownull=false&distinct=true&where=tname.eq.free";

        // if there is no name, then the data for all stations should be fetched
        if(nameInput == null) {
            System.out.println("all stations");
            url += "&select=scoordinate,sname";
        }
        // if a name is given fetch the latest data for that station
        else {
            nameInput = URLEncoder.encode(nameInput, StandardCharsets.UTF_8);
            System.out.println("Station: " + nameInput);
            url += "&where=sname.eq." + nameInput + "&select=mvalue,mvalidtime,mperiod,scode,tname,scoordinate,smetadata.capacity,sname,smetadata.municipality";
        }

        Set<ParkingStation> stations = new HashSet<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(url));
            JsonNode dataArray = root.path("data");

            for (JsonNode entry : dataArray) {
                String name = entry.path("sname").asText(null);

                JsonNode coordNode = entry.path("scoordinate");
                double x = coordNode.path("x").asDouble();
                double y = coordNode.path("y").asDouble();
                Coordinate coordinates = new Coordinate(x, y);

                // If name is null -> minimal data
                if (nameInput == null) {
                    ParkingStation station = new ParkingStation(name, 0, 0, "", 0, coordinates, new ArrayList<>(), new ArrayList<>());
                    stations.add(station);
                } else {
                    // Full data parsing
                    int code = entry.path("scode").asInt();
                    int period = entry.path("mperiod").asInt();
                    int capacity = entry.path("smetadata.capacity").asInt();
                    String municipality = entry.path("smetadata.municipality").asText("");

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
                        int value = node.get("mvalue").asInt();

                        timestamps.add(timestamp);
                        free_spots.add(value);
                    }

                    // Build ParkingStation once per station name (grouped data)
                    ParkingStation station = new ParkingStation(
                        name,
                        code,
                        period,
                        municipality,
                        capacity,
                        coordinates,
                        timestamps,
                        free_spots
                        );
                        stations.add(station);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
}

    public static void main(String[] args) {
        //ParkingData.findLatestData(null);
        ParkingData.findLatestData("Park Plaza");

    }
}
