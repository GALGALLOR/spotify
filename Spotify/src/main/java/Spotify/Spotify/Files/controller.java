package Spotify.Spotify.Files;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/")
@RestController
public class controller {
    @Autowired
    private Service service;

    @Value("${spotify.api.token}")
    private String token;

    @GetMapping(path="artist/{id}")
    public String artists(@PathVariable("id") String id) throws JsonProcessingException {
        return service.getArtistNameByID(id,token);
    }

    @GetMapping(path="song/{id}")
    public String songs(@PathVariable("id") String id) throws JsonProcessingException {
        return service.getArtistTopSongByID(id,token);
    }
    @GetMapping(path="artist/search/{artist}")
    public String search(@PathVariable("artist") String artist) throws JsonProcessingException {
        return service.getArtistIdBySearch(artist,token);
    }
    @GetMapping(path="song/artist/search/{artist}")
    public String searchArtist(@PathVariable("artist") String artist) throws JsonProcessingException {
        String artist_id = service.getArtistIdBySearch(artist,token);
        return service.getArtistTopSongByID(artist_id, token);
    }



}
