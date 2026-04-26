package com.moodtunes.repository;

import com.moodtunes.model.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Integer> {

    @Query("SELECT t FROM PlaylistTrack t WHERE t.playlist.playlistId = :playlistId ORDER BY t.trackOrder ASC")
    List<PlaylistTrack> findByPlaylistId(@Param("playlistId") int playlistId);
}
