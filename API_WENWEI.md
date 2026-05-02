# MoodTunes — Wenwei's API Reference

Endpoints implemented in `wenwei/backend-mood-playlist`.  
Base URL: `http://localhost:8080`  
All endpoints require `Authorization: Bearer <JWT>` (token from `POST /api/auth/login`).

---

## Moods

### POST `/api/moods/log`

Record a mood entry for the authenticated user.

**Request Body** (`application/json`)

| Field | Type | Required | Description |
|---|---|---|---|
| `moodText` | string | yes | Free-text mood description |
| `musicPreferences` | string | no | Preferred genres / artists |
| `contextNote` | string | no | Journal-style emotional context |

```json
{
  "moodText": "I'm feeling happy and excited about my progress today",
  "musicPreferences": "I like indie pop and Taylor Swift",
  "contextNote": "Just finished a big project milestone"
}
```

**Response `200 OK`**

```json
{
  "moodId": 42,
  "moodText": "I'm feeling happy and excited about my progress today",
  "musicPreferences": "I like indie pop and Taylor Swift",
  "contextNote": "Just finished a big project milestone",
  "createdAt": "2026-04-26T14:30:00"
}
```

> **Note:** Save the returned `moodId` — it is required when calling `POST /api/playlists/save`.

---

### GET `/api/moods/history`

Return all mood entries for the authenticated user, newest first.

**Response `200 OK`**

```json
[
  {
    "moodId": 42,
    "moodText": "I'm feeling happy and excited about my progress today",
    "musicPreferences": "I like indie pop and Taylor Swift",
    "contextNote": "Just finished a big project milestone",
    "createdAt": "2026-04-26T14:30:00"
  },
  {
    "moodId": 37,
    "moodText": "Feeling calm and reflective",
    "musicPreferences": null,
    "contextNote": null,
    "createdAt": "2026-04-25T20:10:00"
  }
]
```

---

## Playlists

### POST `/api/playlists/save`

Persist a generated playlist (and its tracks) to the user's library.  
Intended to be called after `POST /api/playlists/generate` (Sid's endpoint) returns a track list.

**Request Body** (`application/json`)

| Field | Type | Required | Description |
|---|---|---|---|
| `title` | string | yes | Playlist title |
| `moodId` | int | no | ID of an existing mood that triggered this playlist |
| `mood` | string | no | Mood text used to create a new mood when `moodId` is omitted |
| `musicPreferences` | string | no | Optional preferences for a new mood when `moodId` is omitted |
| `context` | string | no | Optional context for a new mood when `moodId` is omitted |
| `tracks` | array | no | List of track objects (see below) |

Each track object:

| Field | Type | Required | Description |
|---|---|---|---|
| `trackName` | string | yes | Song title |
| `artistName` | string | yes | Artist name |
| `youtubeMusicUrl` | string | yes | Validated YouTube Music URL |
| `trackOrder` | int | yes | Position in playlist (1–20) |

```json
{
  "title": "Happy Vibes",
  "moodId": 42,
  "tracks": [
    {
      "trackName": "Walking on Sunshine",
      "artistName": "Katrina & The Waves",
      "youtubeMusicUrl": "https://music.youtube.com/watch?v=...",
      "trackOrder": 1
    },
    {
      "trackName": "Happy",
      "artistName": "Pharrell Williams",
      "youtubeMusicUrl": "https://music.youtube.com/watch?v=...",
      "trackOrder": 2
    }
  ]
}
```

**Response `201 Created`**

```json
{
  "playlistId": 15,
  "title": "Happy Vibes",
  "createdAt": "2026-04-26T14:31:00",
  "mood": {
    "moodId": 42,
    "moodText": "I'm feeling happy and excited about my progress today",
    "musicPreferences": "I like indie pop and Taylor Swift",
    "contextNote": "Just finished a big project milestone"
  },
  "tracks": [
    {
      "trackId": 101,
      "trackName": "Walking on Sunshine",
      "artistName": "Katrina & The Waves",
      "youtubeMusicUrl": "https://music.youtube.com/watch?v=...",
      "trackOrder": 1
    },
    {
      "trackId": 102,
      "trackName": "Happy",
      "artistName": "Pharrell Williams",
      "youtubeMusicUrl": "https://music.youtube.com/watch?v=...",
      "trackOrder": 2
    }
  ]
}
```

**Error Responses**

| Status | Body | Cause |
|---|---|---|
| `400` | `{"error": "mood is required"}` | both `moodId` and `mood` are missing/blank |
| `400` | `{"error": "title is required"}` | `title` is null or blank |

---

### GET `/api/playlists/library`

Return all saved playlists for the authenticated user as a summary list, newest first.

**Response `200 OK`**

```json
[
  {
    "playlistId": 15,
    "title": "Happy Vibes",
    "createdAt": "2026-04-26T14:31:00",
    "songCount": 18,
    "mood": "I'm feeling happy and excited about my progress today"
  },
  {
    "playlistId": 12,
    "title": "Calm Evening",
    "createdAt": "2026-04-25T20:11:00",
    "songCount": 16,
    "mood": "Feeling calm and reflective"
  }
]
```

> **Note:** This endpoint returns a summary (no track list). Use `GET /api/playlists/{id}` for the full track list.

---

### GET `/api/playlists/{id}`

Return full details of a single playlist including all tracks.  
The playlist owner can access it. A recipient can also access it if the playlist was shared with them.

**Path Parameter**

| Parameter | Type | Description |
|---|---|---|
| `id` | int | Playlist ID |

**Response `200 OK`**

```json
{
  "playlistId": 15,
  "title": "Happy Vibes",
  "createdAt": "2026-04-26T14:31:00",
  "mood": {
    "moodId": 42,
    "moodText": "I'm feeling happy and excited about my progress today",
    "musicPreferences": "I like indie pop and Taylor Swift",
    "contextNote": "Just finished a big project milestone"
  },
  "tracks": [
    {
      "trackId": 101,
      "trackName": "Walking on Sunshine",
      "artistName": "Katrina & The Waves",
      "youtubeMusicUrl": "https://music.youtube.com/watch?v=...",
      "trackOrder": 1
    }
  ]
}
```

**Error Responses**

| Status | Body | Cause |
|---|---|---|
| `404` | `{"error": "Playlist not found"}` | Playlist ID does not exist |
| `403` | `{"error": "Playlist not found or access denied"}` | Playlist belongs to another user and was not shared with the caller |

---

### POST `/api/playlists/share`

Share one of the authenticated user's playlists with one or more friends.

**Request Body** (`application/json`)

| Field | Type | Required | Description |
|---|---|---|---|
| `playlistId` | int | yes | ID of the playlist to share |
| `recipientIds` | int[] | yes | List of user IDs to share with |
| `message` | string | no | Optional personal message |

```json
{
  "playlistId": 15,
  "recipientIds": [3, 7, 12],
  "message": "This playlist really helped me today!"
}
```

**Response `200 OK`**

```json
{
  "message": "Playlist shared successfully"
}
```

**Error Responses**

| Status | Body | Cause |
|---|---|---|
| `400` | `{"error": "playlistId is required"}` | `playlistId` missing |
| `400` | `{"error": "recipientIds is required"}` | `recipientIds` is null or empty |
| `400` | `{"error": "Can only share with friends"}` | recipient is not an accepted friend |
| `403` | `{"error": "You do not own this playlist"}` | Caller does not own the playlist |

> **Note:** Invalid recipient IDs (user not found) are silently skipped — no error is returned for them.

---

### GET `/api/playlists/shared`

Return all playlists that other users have shared with the authenticated user, newest first.

**Response `200 OK`**

```json
[
  {
    "shareId": 5,
    "playlistId": 15,
    "title": "Happy Vibes",
    "senderUsername": "johndoe",
    "message": "This playlist really helped me today!",
    "sharedAt": "2026-04-26T15:00:00"
  },
  {
    "shareId": 3,
    "playlistId": 8,
    "title": "Workout Mix",
    "senderUsername": "alicesmith",
    "message": null,
    "sharedAt": "2026-04-24T10:30:00"
  }
]
```

> **Note:** To view the full track list of a shared playlist, call `GET /api/playlists/{playlistId}`. Shared-recipient access is supported.

---

## Authentication Header

All requests must include:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The JWT is returned by `POST /api/auth/login` (Sophie's endpoint). Protected routes reject missing or expired tokens before controller logic runs.

---

## Quick Reference

| Method | Path | Description | Owner |
|---|---|---|---|
| POST | `/api/moods/log` | Log a mood entry | Wenwei |
| GET | `/api/moods/history` | Get mood history (newest first) | Wenwei |
| POST | `/api/playlists/generate` | Generate AI playlist | Sid |
| POST | `/api/playlists/save` | Save playlist to library | Wenwei |
| GET | `/api/playlists/library` | List saved playlists (summary) | Wenwei |
| GET | `/api/playlists/{id}` | Get full playlist + tracks | Wenwei |
| POST | `/api/playlists/share` | Share playlist with friends | Wenwei |
| GET | `/api/playlists/shared` | Playlists shared with me | Wenwei |
