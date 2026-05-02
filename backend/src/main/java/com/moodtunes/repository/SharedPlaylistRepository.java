package com.moodtunes.repository;

import com.moodtunes.model.SharedPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedPlaylistRepository extends JpaRepository<SharedPlaylist, Integer> {
    List<SharedPlaylist> findByRecipientUserIdOrderBySharedAtDesc(Integer recipientId);

    boolean existsByPlaylistPlaylistIdAndRecipientUserId(Integer playlistId, Integer recipientId);

    Optional<SharedPlaylist> findByPlaylistPlaylistIdAndRecipientUserId(Integer playlistId, Integer recipientId);
}
