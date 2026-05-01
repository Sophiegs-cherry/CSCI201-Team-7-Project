package com.moodtunes.controller;

import com.moodtunes.dto.FriendRequestDto;
import com.moodtunes.dto.FriendshipView;
import com.moodtunes.dto.UserSummary;
import com.moodtunes.security.CurrentUser;
import com.moodtunes.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Owner: Suzy Xu. */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friends;

    public FriendController(FriendService friends) {
        this.friends = friends;
    }

    @PostMapping("/request")
    public FriendshipView request(@Valid @RequestBody FriendRequestDto req) {
        return friends.sendRequest(CurrentUser.require().userId(), req.getTargetUserId());
    }

    @GetMapping("/requests")
    public List<FriendshipView> incomingRequests() {
        return friends.incomingRequests(CurrentUser.require().userId());
    }

    @GetMapping("/requests/sent")
    public List<FriendshipView> sentRequests() {
        return friends.outgoingRequests(CurrentUser.require().userId());
    }

    @PostMapping("/accept/{id}")
    public void accept(@PathVariable("id") Integer id) {
        friends.accept(CurrentUser.require().userId(), id);
    }

    @PostMapping("/decline/{id}")
    public void decline(@PathVariable("id") Integer id) {
        friends.decline(CurrentUser.require().userId(), id);
    }

    @GetMapping
    public List<FriendshipView> list() {
        return friends.listFriends(CurrentUser.require().userId());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable("id") Integer id) {
        friends.remove(CurrentUser.require().userId(), id);
    }

    @GetMapping("/search")
    public List<UserSummary> search(@RequestParam("username") String username) {
        return friends.search(CurrentUser.require().userId(), username);
    }
}