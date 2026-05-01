package com.moodtunes.service;

import com.moodtunes.dto.FriendshipView;
import com.moodtunes.dto.UserSummary;
import com.moodtunes.model.Friendship;
import com.moodtunes.model.User;
import com.moodtunes.repository.FriendshipRepository;
import com.moodtunes.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/** Owner: Suzy Xu. */
@Service
public class FriendService {

    private final FriendshipRepository friendships;
    private final UserRepository users;

    public FriendService(FriendshipRepository friendships, UserRepository users) {
        this.friendships = friendships;
        this.users = users;
    }

    @Transactional
    public FriendshipView sendRequest(String requesterUsername, Integer targetUserId) {
        Integer requesterId = requireUser(requesterUsername).getUserId();
        if (requesterId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot friend yourself");
        }
        User requester = users.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester not found"));
        User target = users.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        friendships.findBetween(requesterId, targetUserId).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A friendship record already exists with this user");
        });

        Friendship f = new Friendship();
        f.setRequester(requester);
        f.setAddressee(target);
        f.setStatus(Friendship.Status.PENDING);
        friendships.save(f);

        return new FriendshipView(
                f.getFriendshipId(),
                summary(target),
                f.getStatus().name(),
                "SENT");
    }

    @Transactional(readOnly = true)
    public List<FriendshipView> incomingRequests(String username) {
        Integer userId = requireUser(username).getUserId();
        return friendships.findByAddresseeUserIdAndStatus(userId, Friendship.Status.PENDING)
                .stream()
                .map(f -> new FriendshipView(
                        f.getFriendshipId(),
                        summary(f.getRequester()),
                        f.getStatus().name(),
                        "RECEIVED"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipView> outgoingRequests(String username) {
        Integer userId = requireUser(username).getUserId();
        return friendships.findByRequesterUserIdAndStatus(userId, Friendship.Status.PENDING)
                .stream()
                .map(f -> new FriendshipView(
                        f.getFriendshipId(),
                        summary(f.getAddressee()),
                        f.getStatus().name(),
                        "SENT"))
                .toList();
    }

    @Transactional
    public void accept(String username, Integer friendshipId) {
        Integer userId = requireUser(username).getUserId();
        Friendship f = friendships.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!f.getAddressee().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request to accept");
        }
        if (f.getStatus() != Friendship.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already resolved");
        }
        f.setStatus(Friendship.Status.ACCEPTED);
    }

    @Transactional
    public void decline(String username, Integer friendshipId) {
        Integer userId = requireUser(username).getUserId();
        Friendship f = friendships.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!f.getAddressee().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request to decline");
        }
        f.setStatus(Friendship.Status.DECLINED);
    }

    @Transactional(readOnly = true)
    public List<FriendshipView> listFriends(String username) {
        Integer userId = requireUser(username).getUserId();
        List<Friendship> all = friendships.findAcceptedFriendshipsForUser(userId);
        List<FriendshipView> out = new ArrayList<>(all.size());
        for (Friendship f : all) {
            User other = f.getRequester().getUserId().equals(userId) ? f.getAddressee() : f.getRequester();
            out.add(new FriendshipView(
                    f.getFriendshipId(),
                    summary(other),
                    f.getStatus().name(),
                    f.getRequester().getUserId().equals(userId) ? "SENT" : "RECEIVED"));
        }
        return out;
    }

    @Transactional
    public void remove(String username, Integer friendshipId) {
        Integer userId = requireUser(username).getUserId();
        Friendship f = friendships.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found"));
        if (!f.getRequester().getUserId().equals(userId) && !f.getAddressee().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your friendship to remove");
        }
        friendships.delete(f);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> search(String username, String query) {
        Integer requesterId = requireUser(username).getUserId();
        if (query == null || query.isBlank()) return List.of();
        return users.findByUsernameContainingIgnoreCase(query.trim())
                .stream()
                .filter(u -> !u.getUserId().equals(requesterId))
                .map(this::summary)
                .toList();
    }

    private UserSummary summary(User u) {
        return new UserSummary(u.getUserId(), u.getUsername(), u.getDisplayName(), u.getProfilePicturePath());
    }

    private User requireUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
