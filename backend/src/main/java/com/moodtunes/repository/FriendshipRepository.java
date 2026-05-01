package com.moodtunes.repository;

import com.moodtunes.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    Optional<Friendship> findByRequesterUserIdAndAddresseeUserId(Integer requesterId, Integer addresseeId);

    List<Friendship> findByAddresseeUserIdAndStatus(Integer addresseeId, Friendship.Status status);

    List<Friendship> findByRequesterUserIdAndStatus(Integer requesterId, Friendship.Status status);

    @Query("SELECT f FROM Friendship f " +
           "WHERE f.status = com.moodtunes.model.Friendship.Status.ACCEPTED " +
           "AND (f.requester.userId = :userId OR f.addressee.userId = :userId)")
    List<Friendship> findAcceptedFriendshipsForUser(@Param("userId") Integer userId);

    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.requester.userId = :a AND f.addressee.userId = :b) " +
           "   OR (f.requester.userId = :b AND f.addressee.userId = :a)")
    Optional<Friendship> findBetween(@Param("a") Integer a, @Param("b") Integer b);
}