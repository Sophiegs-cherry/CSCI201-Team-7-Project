# AI Usage Log — Social Features (Friends & Playlist Sharing)

**Author:** Suzy Xu (Backend)
**Course:** CSCI 201 — Spring 2026
**Project:** MoodTunes
**Scope:** This log documents three concrete instances where I used Claude during development of the friends and playlist-sharing features. For each one I record the prompt, a summary of the AI response, the resulting code changes, and what I learned.

---

## Example 1 — Preventing duplicate friend requests in both directions

### Prompt
> I'm building a friendship feature in Spring Boot with JPA. My `friendships` table has `requester_id` and `addressee_id`. How do I prevent a user from sending a friend request to someone who already sent them one (or who they're already friends with), regardless of who initiated it? Is a unique constraint enough?

### AI Response (summary)
Claude pointed out that a unique constraint on `(requester_id, addressee_id)` only catches *exact* duplicates — it would still allow `(Alice → Bob)` and `(Bob → Alice)` to coexist as two separate rows. It recommended a layered approach:

1. Keep the unique constraint at the DB level for safety.
2. Add an application-level check that looks up *either direction* before inserting a new friendship.
3. Write that lookup as a custom `@Query` because Spring Data's derived query names can't cleanly express the symmetric OR condition.

It also suggested throwing `Conflict` (409) rather than `BadRequest` (400) since the issue is "this resource already exists" rather than malformed input.

### Code Changes
Added the unique constraint to the entity:

```java
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
public class Friendship { ... }
```

Added a symmetric lookup to `FriendshipRepository`:

```java
@Query("SELECT f FROM Friendship f " +
       "WHERE (f.requester.userId = :a AND f.addressee.userId = :b) " +
       "   OR (f.requester.userId = :b AND f.addressee.userId = :a)")
Optional<Friendship> findBetween(@Param("a") Integer a, @Param("b") Integer b);
```

Used it in `FriendService.sendRequest` to throw `Conflict` if any row already exists between the two users.

### What I learned
I originally thought the unique constraint would handle everything. The AI helped me see that for a *self-referencing* many-to-many relationship, you actually need symmetric uniqueness — and the database can't easily express that, so it has to live in the application layer. I also learned when to reach for `@Query` instead of fighting derived query names: compound OR conditions across the same join are exactly when method-name-based queries break down.

---

## Example 2 — Validating "must be friends" before sharing a playlist

### Prompt
> In my `SharingService`, before I save a `SharedPlaylist` row, I need to confirm the recipient is already an accepted friend of the sender. The friendship row could have been created in either direction. What's the cleanest way to write this check, and should I throw 400 or 403 if it fails?

### AI Response (summary)
Claude recommended reusing the `findBetween` repository method from the friend code, then explicitly checking `status == ACCEPTED`. It flagged a bug I would otherwise have shipped: if I only checked `findBetween(...).isPresent()`, a `PENDING` or `DECLINED` row would still pass the check and let the share go through.

On the error code, it argued for `400 Bad Request` rather than `403 Forbidden` because "not your friend yet" is a user-correctable input issue (they can send a friend request) rather than an authorization failure. It also recommended doing the check inside the per-recipient loop so the error message can name *which* recipient is the problem when sharing to multiple people at once.

### Code Changes
In `SharingService.share`:

```java
for (Integer recipientId : req.getRecipientIds()) {
    if (recipientId.equals(senderId)) continue;

    Friendship f = friendships.findBetween(senderId, recipientId)
            .orElseThrow(() -> new ApiExceptions.BadRequest(
                    "Recipient " + recipientId + " is not your friend"));
    if (f.getStatus() != Friendship.Status.ACCEPTED) {
        throw new ApiExceptions.BadRequest(
                "Recipient " + recipientId + " is not your friend");
    }
    // ... save SharedPlaylist
}
```

### What I learned
The status check was the part I almost missed. My original draft treated "row exists" as "they're friends," which isn't true while a request is pending. This taught me to be explicit about *which states are valid* instead of using row existence as a proxy. I also internalized the difference between 400 (caller can fix it) and 403 (caller is not allowed) — I'd been using them interchangeably before this.

---

## Example 3 — Designing the `FriendshipView` DTO with a "direction" field

### Prompt
> My friends-list endpoint needs to return friendships. The entity has `requester` and `addressee` fields, but from the current user's perspective they're sometimes one and sometimes the other. The frontend just wants to know "who's the other person and which way did the request go." How should I shape the DTO?

### AI Response (summary)
Claude suggested creating a `FriendshipView` DTO with four fields: `friendshipId`, `otherUser` (a separate `UserSummary` DTO), `status`, and `direction` ("SENT" or "RECEIVED" from the current user's perspective). It then walked through the mapping logic: if `friendship.requester.userId == currentUserId`, then `otherUser = addressee` and `direction = "SENT"`; otherwise the opposite.

The deeper point it made was that I should hide the `requester` / `addressee` asymmetry from the frontend entirely — the frontend shouldn't need to know my schema's internal direction in order to render a friend list. That's the job of the DTO layer.

### Code Changes
Created two new DTOs: `FriendshipView` and `UserSummary`. The mapping in `FriendService.listFriends`:

```java
for (Friendship f : all) {
    User other = f.getRequester().getUserId().equals(userId)
            ? f.getAddressee()
            : f.getRequester();
    out.add(new FriendshipView(
            f.getFriendshipId(),
            summary(other),
            f.getStatus().name(),
            f.getRequester().getUserId().equals(userId) ? "SENT" : "RECEIVED"));
}
```

### What I learned
This was my first real lesson in *not leaking the data model into the API*. My instinct was to return the entity directly, but Claude reframed the problem from the frontend's perspective: "they don't care who initiated; they care who the other person is." That reframing made the DTO obvious. I'll apply the same thinking to other endpoints — the schema can be asymmetric or weird, but the API should be clean.

---

## Summary

Across all three examples, the common thread was that AI was most useful for catching subtle correctness issues I would have missed on my own: symmetric uniqueness, status validation as a distinct step from existence, and data-model asymmetry leaking into the API. I used AI as a reviewer and design partner rather than a code generator — I wrote the bulk of the implementation, and the AI helped me anticipate edge cases I didn't have the experience to spot.
