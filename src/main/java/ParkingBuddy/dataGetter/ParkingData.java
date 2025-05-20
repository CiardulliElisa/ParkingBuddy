package ParkingBuddy.dataGetter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.util.Tuple;

import java.io.*;
import java.net.MalformedURLException;
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
    public static ParkingStation getHistoricalData(LocalDateTime now, LocalDateTime before, String name) throws IOException {
        URL url = new URL(generateHistoricalURL(now, before, name));
        String response = readData(url, true);
        return parseParkingStationData(response);
    }

    // Method to parse JSON response and create a ParkingStationData object
    private static ParkingStation parseParkingStationData(String jsonResponse) throws IOException {

        JsonNode dataArray = fetchJsonArray(jsonResponse);

        JsonNode firstElement = dataArray.get(0);

        String name = firstElement.get("sname").asText();
        String municipality = firstElement.get("smetadata.municipality").asText();
        int capacity = firstElement.get("smetadata.capacity").asInt();

        Coordinate coordinates = parseCoordinates(firstElement);

        Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> timestamps_values = parseTimestampValues(firstElement);
        ArrayList<LocalDateTime> timestamps = timestamps_values._1();
        ArrayList<Integer> free_spots = timestamps_values._2();

        return new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
    }

    private static Coordinate parseCoordinates(JsonNode node) {
        System.out.println("PArsing coordinates");
        JsonNode coordinateNode = node.get("scoordinate");
        double longitude = coordinateNode.get("x").asDouble();
        double latitude = coordinateNode.get("y").asDouble();
        return new Coordinate (longitude, latitude);
    }

    private static Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> parseTimestampValues(JsonNode dataArray) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

        ArrayList<LocalDateTime> timestamps = new ArrayList<>();
        ArrayList<Integer> free_spots = new ArrayList<>();

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
        return new Tuple<>(timestamps, free_spots);
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
    }

    public static Set<ParkingStation> findAllLatestData() throws MalformedURLException {
        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&select=scoordinate,sname");

        Set<ParkingStation> stations = new HashSet<>();

        try {
             String response = readData(url, false);
             JsonNode dataArray = fetchJsonArray(response);

            for (JsonNode node : dataArray) {
                String name = node.path("sname").asText(null);
                Coordinate coordinates = parseCoordinates(node);
                ParkingStation station = new ParkingStation(name, "", 0, coordinates, new ArrayList<>(), new ArrayList<>());
                stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }

    private static JsonNode fetchJsonArray(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataArray = rootNode.get("data");
        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
            throw new IllegalArgumentException("No data available in JSON response.");
        }
        return dataArray;
    }

    public static Set<String> getAllMunicipalities() throws MalformedURLException {
        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&select=smetadata.municipality");

        Set<String> municipalities  = new HashSet<>();

        try {
            String response = readData(url, false);
            JsonNode dataArray = fetchJsonArray(response);

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

    public static Set<ParkingStation> findStationLatestData(String nameInput) throws MalformedURLException {

        String encodedName = URLEncoder.encode(nameInput, StandardCharsets.UTF_8).replace(".", "%2E");;

        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&where=sname.eq." + encodedName
                + "&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality");

        Set<ParkingStation> stations = new HashSet<>();

        try {
            String response = readData(url, false);
            JsonNode dataArray = fetchJsonArray(response);

            if (!dataArray.isEmpty()) {

                // For each parking station entry in dataArray, create a ParkingStation instance
                for (JsonNode node : dataArray) {
                    String name = node.path("sname").asText("Unknown");
                    Coordinate coordinates = parseCoordinates(node);

                    int capacity = node.path("smetadata.capacity").asInt(-1);
                    String municipality = node.path("smetadata.municipality").asText("Unknown");

                    Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> timestamp_values = parseTimestampValues(dataArray);
                    ArrayList<LocalDateTime> timestamps = timestamp_values._1();
                    ArrayList<Integer> free_spots = timestamp_values._2();

                    ParkingStation station = new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
                    stations.add(station);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }

    public static Set<ParkingStation> getLatestByMunicipality(String municipalityInput) throws MalformedURLException {
        Set<ParkingStation> stations = new HashSet<>();

        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free"
                + "&where=smetadata.municipality.re." + municipalityInput
                + "&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality");
        try {
            String response = readData(url, false);
            JsonNode dataArray = fetchJsonArray(response);

            if (!dataArray.isEmpty()) {

                // For each parking station entry in dataArray, create a ParkingStation instance
                for (JsonNode node : dataArray) {
                    String name = node.path("sname").asText("Unknown");
                    Coordinate coordinates = parseCoordinates(node);
                    int capacity = node.path("smetadata.capacity").asInt(-1);
                    String municipality = node.path("smetadata.municipality").asText("Unknown");

                    Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> timestamp_values = parseTimestampValues(dataArray);
                    ArrayList<LocalDateTime> timestamps = timestamp_values._1();
                    ArrayList<Integer> free_spots = timestamp_values._2();

                    ParkingStation station = new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
                    stations.add(station);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }
}
