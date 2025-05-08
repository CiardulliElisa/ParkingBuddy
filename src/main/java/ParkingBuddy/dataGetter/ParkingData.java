package ParkingBuddy.dataGetter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParkingData extends GetData {


    // Returns ParkingStation - all data for a certain parking station for a certain interval of time
    // @param startTime and endTime - start and end of the interval of time we are interested in
    // @param code - the code of the parking lot we are interested in
    public ParkingStation getData(LocalDateTime startDate, LocalDateTime endDate, int code) throws IOException {

        String accessToken = generateAccessToken();

        try {
            URL apiUrl = new URL(generateURL(startDate, endDate, code));
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
    private ParkingStation parseParkingStationData(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataArray = rootNode.get("data");

        if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
            throw new IllegalArgumentException("No data available in JSON response.");
        }

        JsonNode firstElement = dataArray.get(0);

        String scode = firstElement.get("scode").asText();
        String sname = firstElement.get("sname").asText();
        int mperiod = firstElement.get("mperiod").asInt();

        // Prepare the timestamp -> value map
        Map<LocalDateTime, Integer> timestampValueMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

        // Iterate over all elements in the rootNode array
        for (JsonNode node : dataArray) {
            // Parse timestamp
            String timestampStr = node.get("_timestamp").asText();
            if (timestampStr == null || timestampStr.isEmpty()) {
                continue; // Skip if no valid timestamp
            }
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);

            // Get the value (mvalue)
            int value = node.get("mvalue").asInt();

            // Add to the map
            timestampValueMap.put(timestamp, value);
        }

        // Return the ParkingStationData object
        return new ParkingStation(scode, sname, mperiod, timestampValueMap);
    }

    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    private String generateURL(LocalDateTime startDate, LocalDateTime endDate, int code) {
        return "https://mobility.api.opendatahub.com/v2/flat/ParkingStation/free/" +
                formatDate(startDate) + "/"+
                formatDate(endDate) +
                "?limit=-1&offset=0&where=scode.eq.%22" +
                code +
                "%22&shownull=false&distinct=true&timezone=UTC&select=mvalue,mvalidtime,mperiod,sname,scode";
    };


    public static void main(String[] args) throws IOException {
        ParkingData pd = new ParkingData();
        LocalDateTime start = LocalDateTime.of(2025, 4, 29, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 0);
        int code = 103;
        pd.getData(start, end, code);
    }

}
