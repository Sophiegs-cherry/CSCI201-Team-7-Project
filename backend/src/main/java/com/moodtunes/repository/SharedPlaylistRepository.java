package com.moodtunes.repository;

import com.moodtunes.model.SharedPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedPlaylistRepository extends JpaRepository<SharedPlaylist, Integer> {
    List<SharedPlaylist> findByRecipientUserIdOrderBySharedAtDesc(Integer recipientId);
}