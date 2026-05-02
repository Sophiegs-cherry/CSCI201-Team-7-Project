package com.moodtunes.repository;

import com.moodtunes.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    @Query("SELECT p FROM Playlist p WHERE p.user.userId = :userId ORDER BY p.createdAt DESC")
    List<Playlist> findByUserIdOrderByCreatedAtDesc(@Param("userId") int userId);
}
