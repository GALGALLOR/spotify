package Spotify.Spotify.Files;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("api/")
@RestController
public class controller {
    @Autowired
    private Service service;
    @Autowired
    private OAuthTokenFetcher fetcher;
    ObjectMapper mapper = new ObjectMapper();
    private String token;
    public String getToken() throws IOException {
        if(fetcher.getToken()==null){
            System.out.println("Token is null: Fetching");
            token = fetcher.getToken();
        }
        else if(fetcher.getExpiryTime()-300000<System.currentTimeMillis()){
            System.out.println("Token is expired: Fetching");
            token = fetcher.refresh_token();
        }
        else{
            token = fetcher.getToken();
        }
        return token;
    }
    @GetMapping(path="whatsapp")
    public String whatsapp() throws JsonProcessingException {
        return service.send_whatsapp();
    }
    @GetMapping(path="sms")
    public String sms() throws JsonProcessingException {
        return service.send_sms();
    }
    @GetMapping(path="artist/{id}")
    public String artists(@PathVariable("id") String id) throws IOException {
        token = getToken();
        return service.getArtistNameByID(id,token);
    }

    @GetMapping(path="song/{id}")
    public String songs(@PathVariable("id") String id) throws IOException {
        token = getToken();
        return service.getArtistTopSongByID(id,token);
    }
    @GetMapping(path="artist/search/{artist}")
    public String search(@PathVariable("artist") String artist) throws IOException {
        token = getToken();
        return service.getArtistIdBySearch(artist,token);
    }
    @GetMapping(path="song/artist/search/{artist}")
    public String searchArtist(@PathVariable("artist") String artist) throws IOException {
        token = getToken();
        String artist_id = service.getArtistIdBySearch(artist,token);
        return service.getArtistTopSongByID(artist_id, token);
    }
    @GetMapping(path = "authorize")
    public String authorize() {
        return service.authorize();
        //return callback(code); //so that we can dirctly go to the calledback link instead of having to click on a link to confirm
    }
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code) {
        return service.fetchToken(code);
    }



}
