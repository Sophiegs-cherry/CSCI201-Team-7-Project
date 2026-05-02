package com.moodtunes.controller;

import com.moodtunes.dto.FriendRequestDto;
import com.moodtunes.dto.FriendshipView;
import com.moodtunes.dto.UserSummary;
import com.moodtunes.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public FriendshipView request(@Valid @RequestBody FriendRequestDto req, Principal principal) {
        return friends.sendRequest(principal.getName(), req.getTargetUserId());
    }

    @GetMapping("/requests")
    public List<FriendshipView> incomingRequests(Principal principal) {
        return friends.incomingRequests(principal.getName());
    }

    @GetMapping("/requests/sent")
    public List<FriendshipView> sentRequests(Principal principal) {
        return friends.outgoingRequests(principal.getName());
    }

    @PostMapping("/accept/{id}")
    public void accept(@PathVariable("id") Integer id, Principal principal) {
        friends.accept(principal.getName(), id);
    }

    @PostMapping("/decline/{id}")
    public void decline(@PathVariable("id") Integer id, Principal principal) {
        friends.decline(principal.getName(), id);
    }

    @GetMapping
    public List<FriendshipView> list(Principal principal) {
        return friends.listFriends(principal.getName());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable("id") Integer id, Principal principal) {
        friends.remove(principal.getName(), id);
    }

    @GetMapping("/search")
    public List<UserSummary> search(@RequestParam("username") String username, Principal principal) {
        return friends.search(principal.getName(), username);
    }
}
