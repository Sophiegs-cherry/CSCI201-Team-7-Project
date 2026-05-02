package com.moodtunes.controller;

import com.moodtunes.model.Mood;
import com.moodtunes.model.User;
import com.moodtunes.repository.MoodRepository;
import com.moodtunes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moods")
public class MoodController {

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/log")
    public ResponseEntity<?> logMood(@RequestBody Map<String, String> request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Mood mood = new Mood();
        mood.setUser(user);
        mood.setMoodText(request.get("moodText"));
        mood.setMusicPreferences(request.get("musicPreferences"));
        mood.setContextNote(request.get("contextNote"));

        Mood saved = moodRepository.save(mood);
        return ResponseEntity.ok(toMoodMap(saved));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMoodHistory(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Mood> moods = moodRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (Mood mood : moods) {
            response.add(toMoodMap(mood));
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMoodMap(Mood mood) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("moodId", mood.getMoodId());
        map.put("moodText", mood.getMoodText());
        map.put("musicPreferences", mood.getMusicPreferences());
        map.put("contextNote", mood.getContextNote());
        map.put("createdAt", mood.getCreatedAt());
        return map;
    }
}
