package Spotify.Spotify.Files;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@org.springframework.stereotype.Service
public class Service {
    @Value("${spotify.api.client_id}")
    private String client_id;
    @Value("${spotify.api.client_secret}")
    private String client_secret;
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
            private OAuthTokenFetcher oAuthTokenFetcher;
    @Autowired
    private twilioClass twilioClass;

    public ResponseEntity<String> generate_response(String url, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","Bearer "+token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET,entity,String.class);
    }

    public String getArtistNameByID(String id, String token) throws JsonProcessingException {
        String artist_url = "https://api.spotify.com/v1/artists/"+id;
        ResponseEntity<String> response=generate_response(artist_url, token);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String path =  root.findPath("name").toString();
        return path.substring(1,path.length()-1);
    }

    public String getArtistTopSongByID(String id, String token) throws JsonProcessingException {
        String artist_url = "https://api.spotify.com/v1/artists/" + id + "/top-tracks";
        ResponseEntity<String> response = generate_response(artist_url, token);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String path= root.findPath("tracks").findPath("name").toString();
        return path.substring(1,path.length()-1);
    }
    public String getArtistIdBySearch(String search, String token) throws JsonProcessingException {
        String artist_url = "https://api.spotify.com/v1/search?q="+search+"&type=artist";
        ResponseEntity<String> response = generate_response(artist_url, token);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String path= root.findPath("artists").findPath("items").findPath("id").toString();
        return path.substring(1,path.length()-1);
        //return root.toPrettyString();
    }
    public String send_whatsapp(){
        twilioClass.send_whatsapp_message();
        return "whatsapp message sent";
    }
    public String send_sms(){
        //Doesn't work because the Phone number +254 is not verified
        twilioClass.send_sms();
        return "sms sent";
    }
    public String authorize() {
        String callbackUrl = "http://127.0.0.1:8080/api/callback";
        String authURL = "https://accounts.spotify.com/authorize?client_id=" + client_id + "&response_type=code&redirect_uri=" + callbackUrl;
        // How about we redirect to the url below?
        return "Redirect to the following URL to authorize: <a href='" + authURL+"'>"+authURL+"</a>";
    }
    public String fetchToken(String code) {
        try {
            String tokenResponse = oAuthTokenFetcher.fetchToken(code);
            if (tokenResponse != null) {
                // Process the token response (e.g., save the token, make further API calls)
                System.setProperty("spotify.api.token", tokenResponse); // Set the environment variable
                return "Access token fetched successfully: " + tokenResponse;
            } else {
                return "Failed to fetch access token.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching token: " + e.getMessage();
        }
    }

}
