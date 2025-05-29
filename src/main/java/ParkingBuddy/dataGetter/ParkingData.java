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

    /**
     * Retrieves the historical data in between two dates for a certain parking station
     *
     * @param now the most recent date
     * @param before the least recent date
     * @param name the name of the station
     *
     * @return the selected station as a ParkingStation object
     */
    public static ParkingStation getHistoricalData(LocalDateTime now, LocalDateTime before, String name) throws IOException {
        URL url = new URL(generateHistoricalURL(now, before, name));
        String response = readData(url, true);
        return parseParkingStationData(response);
    }

    //Parses JSON data representing a parking station and converts it into a ParkingStation object.
    private static ParkingStation parseParkingStationData(String jsonResponse) throws IOException {

        JsonNode dataArray = fetchJsonArray(jsonResponse);

        JsonNode firstElement = dataArray.get(0);

        String name = firstElement.get("sname").asText("Unknown");
        String municipality = firstElement.get("smetadata.municipality").asText("Unknown");
        int capacity = firstElement.get("smetadata.capacity").asInt(-1);

        Coordinate coordinates = parseCoordinates(firstElement);

        Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> timestamps_values = parseTimestampValues(dataArray);
        ArrayList<LocalDateTime> timestamps = timestamps_values._1();
        ArrayList<Integer> free_spots = timestamps_values._2();

        return new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
    }

    // Parses a JSON response and extracts the "data" array node.
    private static JsonNode fetchJsonArray(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataArray = rootNode.get("data");
        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
            throw new IllegalArgumentException("No data available in JSON response.");
        }
        return dataArray;
    }


    //Parses a JsonNode containing coordinates into a Coordinate object
    private static Coordinate parseCoordinates(JsonNode node) {
        JsonNode coordinateNode = node.has("scoordinate") ? node.get("scoordinate") : null;
        if(!coordinateNode.isNull() && coordinateNode.has("x") && coordinateNode.has("y")) {
            double longitude = coordinateNode.get("x").asDouble();
            double latitude = coordinateNode.get("y").asDouble();
            return new Coordinate (longitude, latitude);
        }
        else return null;
    }


    /**
     * Parses the timestamps and corresponding values from a JsonNode
     *
     * @param dataArray the JsonNode containing all timestamps and corresponding values
     * @return a Tuple containing two arrays, one for the timestamps as LocalDateTimes and one for the values as Integers
     */
    private static Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> parseTimestampValues(JsonNode dataArray) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

        ArrayList<LocalDateTime> timestamps = new ArrayList<>();
        ArrayList<Integer> free_spots = new ArrayList<>();

        for (JsonNode node : dataArray) {
            String timestampStr = node.has("_timestamp") ? node.get("_timestamp").asText() : null;
            if (timestampStr == null || timestampStr.isEmpty()) {
                continue;
            }
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
            int value = node.has("mvalue") ? node.get("mvalue").asInt() : -1;

            timestamps.add(timestamp);
            free_spots.add(value);
        }
        return new Tuple<>(timestamps, free_spots);
    }

    // Formats a given date into the yyyy-MM-dd format
    private static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    // Generates an url in string format for fetching the free_spots at given times, the coordinates, the capacity, the municipality and the name of a parking station
    private static String generateHistoricalURL(LocalDateTime startDate, LocalDateTime endDate, String name) throws UnsupportedEncodingException {
        name = URLEncoder.encode(name, StandardCharsets.UTF_8).replace(".", "%2E");;
        return "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/" +
                formatDate(startDate) + "/"+
                formatDate(endDate) +
                "?limit=-1&offset=0&where=sname.eq.%22" +
                name +
                "%22&where=tname.eq.free&shownull=false&distinct=true&timezone=UTC+2&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality";
    }

    // Retrieves all available parking stations and saves them as ParkingStation objects, with coordinates and names
    public static Set<ParkingStation> getAllStations() throws MalformedURLException {
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

    // Retrieves a list of all municipalities that contain parking stations
    public static Set<String> getAllMunicipalities() throws MalformedURLException {
        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free&timezone=UTC+2"
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

    //Retrieves all latest data for the given single parking station and saves it in a ParkinStation object
    public static ParkingStation getStationLatestData(String nameInput) throws MalformedURLException {
        String encodedName = URLEncoder.encode(nameInput, StandardCharsets.UTF_8).replace(".", "%2E");

        // Construct URL with parameters; combine where clauses properly if API supports multiple conditions
        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free and sname.eq." + encodedName
                + "&timezone=UTC+2&select=mvalue,scoordinate,smetadata.capacity,sname,smetadata.municipality");

        try {
            String response = readData(url, false);
            JsonNode dataArray = fetchJsonArray(response);

            if (!dataArray.isEmpty()) {
                JsonNode node = dataArray.get(0); // get the first element only

                String name = node.path("sname").asText("Unknown");
                Coordinate coordinates = parseCoordinates(node);

                int capacity = node.path("smetadata.capacity").asInt(-1);
                String municipality = node.path("smetadata.municipality").asText("Unknown");

                // Extract timestamps and free spots for this node, assuming parseTimestampValues can work on a single node or adjust accordingly
                Tuple<ArrayList<LocalDateTime>, ArrayList<Integer>> timestamp_values = parseTimestampValues(dataArray);
                ArrayList<LocalDateTime> timestamps = timestamp_values._1();
                ArrayList<Integer> free_spots = timestamp_values._2();

                return new ParkingStation(name, municipality, capacity, coordinates, timestamps, free_spots);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieves the set of all parking stations in the given municipality
    public static Set<ParkingStation> getLatestByMunicipality(String municipalityInput) throws MalformedURLException {
        Set<ParkingStation> stations = new HashSet<>();

        URL url = new URL("https://mobility.api.opendatahub.com/v2/flat/ParkingStation/*/latest"
                + "?limit=-1&offset=0&shownull=false&distinct=true"
                + "&where=tname.eq.free&timezone=UTC+2"
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
