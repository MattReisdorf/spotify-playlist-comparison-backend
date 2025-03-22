package com.mattreisdorf.spotify_playlist_comparison_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpotifyPlaylistComparisonBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpotifyPlaylistComparisonBackendApplication.class, args);
	}

}
