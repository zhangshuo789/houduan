package com.volleyball.volleyballcommunitybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolleyballCommunityBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolleyballCommunityBackendApplication.class, args);
	}

}
