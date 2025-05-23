package ParkingBuddy.dataGetter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public abstract class GetData {

    private static final String TOKEN_URL = "https://auth.opendatahub.com/auth/realms/noi/protocol/openid-connect/token";
    private static final String CLIENT_ID = "opendatahub-bootcamp-2025";
    private static final String CLIENT_SECRET = "QiMsLjDpLi5ffjKRkI7eRgwOwNXoU9l1";

    // Returns ParkingStation - all data for a certain parking station for a certain interval of time
    // @param startTime and endTime - start and end of the interval of time we are interested in
    // @param code - the code of the parking lot we are interested in
    public static String readData(URL url, Boolean historical) throws IOException {

        String accessToken = null;

        if(historical) {
            accessToken= generateAccessToken();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if(historical) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();
                return response.toString();
            } else {
                throw new IOException("Response status: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }



    // Returns an access token that is used to retrieve data for 48 hours
    public static String generateAccessToken() throws IOException {
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String data = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
        }

        int status = conn.getResponseCode();
        InputStream responseStream = (status < HttpURLConnection.HTTP_BAD_REQUEST)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseBody = response.toString();
        // Extract access_token using basic JSON parsing
        String accessToken;
        accessToken = responseBody.split("\"access_token\":\"")[1].split("\"")[0];

        return accessToken;
    }

}
