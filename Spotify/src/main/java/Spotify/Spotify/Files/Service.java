package Spotify.Spotify.Files;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@org.springframework.stereotype.Service
public class Service {
    private final RestTemplate restTemplate = new RestTemplate();

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
        return root.findPath("name").toString();
        //return response.getBody();
        //return root.path("popularity").asText();

    }

    public String getArtistTopSongByID(String id, String token) throws JsonProcessingException {
        String artist_url = "https://api.spotify.com/v1/artists/" + id + "/top-tracks";
        ResponseEntity<String> response = generate_response(artist_url, token);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        return root.findPath("tracks").findPath("name").toString();
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

}
