package Spotify.Spotify.Files;

import ch.qos.logback.core.net.server.Client;
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
    private String tokeninfo;
    private static final String CallBackUrl = "http://127.0.0.1:8080/api/callback";
    private static final String tokenUrl = "https://accounts.spotify.com/api/token";
    private String access_token;
    private String refresh_token;
    private long expiry_time;

    public String getToken(){
        return access_token;
    }
    public String getRefreshToken(){
        return refresh_token;
    }
    public long getExpiryTime(){
        return expiry_time;
    }
    public String refresh_token() throws IOException {
        URL url = new URL(tokenUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String auth = ClientID + ":" + ClientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        String body = "grant_type=refresh_token&refresh_token=" + refresh_token;
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.toString());

                access_token = root.get("access_token").asText();
                System.out.println(access_token);
                expiry_time = System.currentTimeMillis() + root.get("expires_in").asLong() * 1000;

                return access_token;
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                System.err.println("Error response: " + errorResponse.toString());
                return null;
            }
        }
    }
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
            tokeninfo = root.toString();
            access_token = mapper.readTree(tokeninfo).get("access_token").toString();
            access_token = access_token.substring(1,access_token.length()-1);
            refresh_token = mapper.readTree(tokeninfo).get("refresh_token").toString();
            refresh_token = refresh_token.substring(1,refresh_token.length()-1);
            expiry_time = mapper.readTree(tokeninfo).get("expires_in").asLong();
            expiry_time = System.currentTimeMillis() + expiry_time*1000;

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
