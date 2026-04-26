package com.moodtunes.repository;

import com.moodtunes.model.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Integer> {

    @Query("SELECT m FROM Mood m WHERE m.user.userId = :userId ORDER BY m.createdAt DESC")
    List<Mood> findByUserIdOrderByCreatedAtDesc(@Param("userId") int userId);
}
