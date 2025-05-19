package ParkingBuddy.dataGetter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParkingData extends GetData {

    // Returns ParkingStation - all data for a certain parking station for a certain interval of time
    // @param startTime and endTime - start and end of the interval of time we are interested in
    // @param code - the code of the parking lot we are interested in
    public static ParkingStation getHistoricalData(LocalDateTime now, LocalDateTime aYearAgo, String name) throws IOException {
        String accessToken = generateAccessToken();

        try {
            URL apiUrl = new URL(generateHistoricalURL(now, aYearAgo, name));
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
        return new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
    }

    private static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    private static String generateHistoricalURL(LocalDateTime startDate, LocalDateTime endDate, String name) throws UnsupportedEncodingException {
        name = URLEncoder.encode(name, StandardCharsets.UTF_8).replace(".", "%2E");;
        return "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/" +
                formatDate(startDate) + "/"+
                formatDate(endDate) +
                "?limit=-1&offset=0&where=sname.eq.%22" +
                name +
                "%22&where=tname.eq.free&shownull=false&distinct=true&timezone=UTC&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality";
    };

    public static Set<ParkingStation> findAllLatestData() {
        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&select=scoordinate,sname";

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

                ParkingStation station = new ParkingStation(name, "", 0, coordinates, new ArrayList<>(), new ArrayList<>());
                stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }

    public static Set<String> getAllMunicipalities() {
        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&select=smetadata.municipality";

        Set<String> municipalities  = new HashSet<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(url));
            JsonNode dataArray = root.path("data");

            for (JsonNode entry : dataArray) {
                String municipality = entry.path("smetadata.municipality").asText(null);
                municipality = municipality.split("[ -]")[0];
                municipalities.add(municipality);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return municipalities;
    }

    public static Set<ParkingStation> findLatestData(String nameInput) {
        if (nameInput == null) {
            throw new IllegalArgumentException("Station name must not be null.");
        }

        String encodedName = URLEncoder.encode(nameInput, StandardCharsets.UTF_8).replace(".", "%2E");;

        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&where=sname.eq." + encodedName
                + "&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality";

        Set<ParkingStation> stations = new HashSet<>();
        parseLatestParkingStationData(url, stations);
        return stations;
    }

    public static Set<ParkingStation> getLatestByMunicipality(String municipality) {
        Set<ParkingStation> stations = new HashSet<>();
        String url = "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&where=smetadata.municipality.re." + municipality
                + "&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality";
        parseLatestParkingStationData(url, stations);
        return stations;
    }

    private static void parseLatestParkingStationData(String url, Set<ParkingStation> stations) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new URL(url));
            JsonNode dataArray = root.path("data");

            if (!dataArray.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

                // For each parking station entry in dataArray, create a ParkingStation instance
                for (JsonNode node : dataArray) {
                    String name = node.path("sname").asText(null);

                    JsonNode coordNode = node.path("scoordinate");
                    double x = coordNode.path("x").asDouble();
                    double y = coordNode.path("y").asDouble();
                    Coordinate coordinates = new Coordinate(x, y);

                    int capacity = node.path("smetadata.capacity").asInt();
                    String municipality = node.path("smetadata.municipality").asText("");

                    ArrayList<LocalDateTime> timestamps = new ArrayList<>();
                    ArrayList<Integer> free_spots = new ArrayList<>();

                    String timestampStr = node.path("_timestamp").asText("");
                    if (!timestampStr.isEmpty()) {
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
                        timestamps.add(timestamp);
                    }

                    int value = node.path("mvalue").asInt();
                    free_spots.add(value);

                    ParkingStation station = new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
                    stations.add(station);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
