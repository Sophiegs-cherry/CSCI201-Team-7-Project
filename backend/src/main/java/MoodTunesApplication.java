package com.moodtunes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoodTunesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoodTunesApplication.class, args);
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   MoodTunes Backend Started!           ║");
        System.out.println("║   Server running on port 8080          ║");
        System.out.println("║   API: http://localhost:8080/api       ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}