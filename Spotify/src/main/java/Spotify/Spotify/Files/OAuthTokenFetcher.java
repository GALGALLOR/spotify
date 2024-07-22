package Spotify.Spotify.Files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class OAuthTokenFetcher {

    @Value("${spotify.api.client_id}")
    private String ClientID;

    @Value("${spotify.api.client_secret}")
    private String ClientSecret;
    @Value("${spotify.api.token}")
    private String token;

    private static final String CallBackUrl = "http://127.0.0.1:8080/api/callback";
    private static final String tokenUrl = "https://accounts.spotify.com/api/token";

    public String fetchToken(String code) throws IOException {
        URL url = new URL(tokenUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String auth = ClientID + ":" + ClientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        String body = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + CallBackUrl;
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            System.setProperty("spotify.api.token", root.get("access_token").asText());
            token = root.get("access_token").asText();
            System.setProperty("spotify.api.expires_in", root.get("expires_in").asText());
            //Set expiry Time
            System.setProperty("spotify.api.refresh_token", root.get("refresh_token").asText());

            if (root.has("access_token")) {
                return root.get("access_token").asText();
            } else {
                System.out.println("Access Token not found in the response.");
                return null;
            }
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                errorResponse.append(responseLine.trim());
            }
            System.out.println("Error response: " + errorResponse.toString());
            return null;
        }
    }
}
