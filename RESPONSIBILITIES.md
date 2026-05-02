# MoodTunes — Responsibilities & File Structure

> **MAINTENANCE RULE:** Update this file whenever files are added, moved, renamed, or reassigned. Single source of truth for file ownership and structure. Referenced from CLAUDE.md.
>
> **Rule for agents:** Do not edit a file owned by someone else without coordinating first. Co-owned files (e.g. `PlaylistController.java`) have an explicit boundary noted in the owner sections below.

## File Structure

```
CSCI201-Team-7-Project/
├── README.md
├── CLAUDE.md
├── RESPONSIBILITIES.md
├── frontend/
│   ├── package.json
│   ├── public/
│   └── src/
│       ├── index.js
│       ├── App.js
│       ├── api/axios.js
│       ├── context/AuthContext.js
│       ├── components/Navbar.js
│       └── pages/
│           ├── LandingPage.js
│           ├── LoginPage.js
│           ├── RegisterPage.js
│           ├── DashboardPage.js
│           ├── PlaylistPage.js
│           ├── LibraryPage.js
│           ├── PlaylistDetailPage.js
│           ├── FriendsPage.js
│           ├── SharedWithMePage.js
│           └── ShareModal.js
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/moodtunes/
│       │   ├── MoodTunesApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   └── FlaskConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── MoodController.java
│       │   │   ├── PlaylistController.java
│       │   │   └── FriendController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── PlaylistGenerationService.java
│       │   │   ├── FlaskClientService.java
│       │   │   ├── JsonFileSaveService.java
│       │   │   └── FriendService.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── MoodRepository.java
│       │   │   ├── PlaylistRepository.java
│       │   │   ├── PlaylistTrackRepository.java
│       │   │   ├── FriendshipRepository.java
│       │   │   └── SharedPlaylistRepository.java
│       │   └── model/
│       │       ├── User.java
│       │       ├── Mood.java
│       │       ├── Playlist.java
│       │       ├── PlaylistTrack.java
│       │       ├── Friendship.java
│       │       └── SharedPlaylist.java
│       ├── main/resources/application.properties.example
│       ├── main/resources/application.properties  ← local only, not committed
│       └── test/java/com/moodtunes/
├── flask-service/
│   ├── app.py
│   ├── requirements.txt
│   └── .env.example
└── playlists/                    ← auto-created at runtime, not committed
```

---

---

## Trevor — Frontend: Auth Flow & App Shell
**Branch:** `trevor/frontend-auth`

| File | Purpose |
|---|---|
| `frontend/package.json` | npm config |
| `frontend/public/` | static assets |
| `frontend/src/index.js` | app entry point |
| `frontend/src/App.js` | root router + protected route wrapper |
| `frontend/src/api/axios.js` | Axios instance with JWT `Authorization` header interceptor |
| `frontend/src/context/AuthContext.js` | global auth state (JWT store, login/logout helpers) |
| `frontend/src/components/Navbar.js` | shared nav bar, hidden on landing page for guests |
| `frontend/src/pages/LandingPage.js` | guest landing: 3-step explainer + Sign Up / Log In CTAs |
| `frontend/src/pages/LoginPage.js` | login form with Remember Me |
| `frontend/src/pages/RegisterPage.js` | registration form + client-side validation |

---

## Simra — Frontend: Mood Input & Playlist Display
**Branch:** `simra/frontend-mood-playlist`

| File | Purpose |
|---|---|
| `frontend/src/pages/DashboardPage.js` | mood text box, clickable example tags, optional music preference + context inputs, Submit button |
| `frontend/src/pages/PlaylistPage.js` | generated playlist display (title, mood, 15–20 tracks with YouTube links), save-to-library button |

---

## Matthew Kwok — Frontend: Library & Social UI
**Branch:** `matthew/frontend-library-social`

| File | Purpose |
|---|---|
| `frontend/src/pages/LibraryPage.js` | saved playlists as mood-colored cards (title, date, song count) |
| `frontend/src/pages/PlaylistDetailPage.js` | full playlist detail: mood context, track list, YouTube Music links |
| `frontend/src/pages/FriendsPage.js` | username search bar, My Friends / Received Requests / Sent Requests tabs |
| `frontend/src/pages/SharedWithMePage.js` | playlists shared with the logged-in user, with sender info + message |
| `frontend/src/pages/ShareModal.js` | modal: friend checkboxes, optional message, Cancel / Send |

---

## Sophie Shim — Backend: Authentication
**Branch:** `sophie/backend-auth`

| File | Purpose |
|---|---|
| `backend/pom.xml` | Maven deps + build config |
| `backend/src/main/java/com/moodtunes/MoodTunesApplication.java` | Spring Boot entry point |
| `backend/src/main/java/com/moodtunes/config/SecurityConfig.java` | filter chain: permits `/api/auth/**`, blocks all else |
| `backend/src/main/java/com/moodtunes/config/CorsConfig.java` | allows `localhost:3000` |
| `backend/src/main/java/com/moodtunes/controller/AuthController.java` | `POST /api/auth/register`, `POST /api/auth/login` |
| `backend/src/main/java/com/moodtunes/service/AuthService.java` | BCrypt hashing, JWT generation + validation |
| `backend/src/main/java/com/moodtunes/repository/UserRepository.java` | `findByUsername()`, `findByEmail()` |
| `backend/src/main/java/com/moodtunes/model/User.java` | `@Entity users`: userId, username, email, passwordHash, displayName, profilePicturePath, createdAt |
| `backend/src/main/resources/application.properties.example` | committed placeholder backend config; copy to `application.properties` locally |
| `backend/src/main/resources/application.properties` | local DB/JWT/runtime config only; ignored and not committed |

---

## Wenwei Fu — Backend: Mood & Playlist Persistence
**Branch:** `wenwei/backend-mood-playlist`

| File | Purpose |
|---|---|
| `backend/src/main/java/com/moodtunes/controller/MoodController.java` | `POST /api/moods/log`, `GET /api/moods/history` |
| `backend/src/main/java/com/moodtunes/controller/PlaylistController.java` | `POST /api/playlists/save`, `GET /api/playlists/library`, `GET /api/playlists/{id}`, `POST /api/playlists/share`, `GET /api/playlists/shared` (**`/generate` endpoint belongs to Sid**) |
| `backend/src/main/java/com/moodtunes/repository/MoodRepository.java` | `findByUserIdOrderByCreatedAtDesc()` |
| `backend/src/main/java/com/moodtunes/repository/PlaylistRepository.java` | `findByUserIdOrderByCreatedAtDesc()` |
| `backend/src/main/java/com/moodtunes/repository/PlaylistTrackRepository.java` | `findByPlaylistId()` |
| `backend/src/main/java/com/moodtunes/model/Mood.java` | `@Entity moods`: moodId, user, moodText, musicPreferences, contextNote, createdAt |
| `backend/src/main/java/com/moodtunes/model/Playlist.java` | `@Entity playlists`: playlistId, user, mood, title, createdAt |
| `backend/src/main/java/com/moodtunes/model/PlaylistTrack.java` | `@Entity playlist_tracks`: trackId, playlist, trackName, artistName, youtubeMusicUrl, trackOrder (1–20) |

---

## Daniel Sim — Flask AI Microservice
**Branch:** `daniel/flask-ai-service`

Owns the entire `flask-service/` directory.

| File | Purpose |
|---|---|
| `flask-service/app.py` | `POST /generate-playlist`: receives `{mood, musicPreferences, context}`, builds Gemini prompt, calls Gemini API, parses response, validates each track via ytmusicapi, returns `[{trackName, artistName, youtubeMusicUrl}]` |
| `flask-service/requirements.txt` | `flask`, `google-generativeai`, `ytmusicapi` |
| `flask-service/.env.example` | template — copy to `.env`, set `GEMINI_API_KEY` |

Flask runs at `localhost:5001`. Never expose it publicly. Key must stay in `.env` only.

---

## Sid Goyal — Backend: Java→Flask Integration
**Branch:** `sid/backend-java-flask-integration`

| File | Purpose |
|---|---|
| `backend/src/main/java/com/moodtunes/config/FlaskConfig.java` | Flask base URL (`localhost:5001`) + timeout settings |
| `backend/src/main/java/com/moodtunes/controller/PlaylistController.java` | `POST /api/playlists/generate` endpoint only (**coordinate with Wenwei on the shared file**) |
| `backend/src/main/java/com/moodtunes/service/PlaylistGenerationService.java` | orchestrates: calls FlaskClientService → calls JsonFileSaveService → persists to DB via Wenwei's repos |
| `backend/src/main/java/com/moodtunes/service/FlaskClientService.java` | `RestTemplate` POST to Flask, parses track list, handles connection failures + timeouts with 503 fallback |
| `backend/src/main/java/com/moodtunes/service/JsonFileSaveService.java` | writes `playlists/playlist-{yyyyMMdd-HHmmss}.json` after every generation |
| `playlists/` | auto-created at runtime; gitignored; not served to users |

---

## Suzy Xu — Backend: Social Features & Integration Testing
**Branch:** `suzy/backend-social-testing`

| File | Purpose |
|---|---|
| `backend/src/main/java/com/moodtunes/controller/FriendController.java` | all `/api/friends/**` endpoints |
| `backend/src/main/java/com/moodtunes/service/FriendService.java` | request, accept, decline, list, remove, search; blocks self-friend and duplicate requests |
| `backend/src/main/java/com/moodtunes/repository/FriendshipRepository.java` | `findByAddresseeIdAndStatus()`, `findByRequesterIdOrAddresseeId()` |
| `backend/src/main/java/com/moodtunes/repository/SharedPlaylistRepository.java` | `findByRecipientIdOrderBySharedAtDesc()` |
| `backend/src/main/java/com/moodtunes/model/Friendship.java` | `@Entity friendships`: requester, addressee, status (PENDING/ACCEPTED/DECLINED), unique(requester, addressee) |
| `backend/src/main/java/com/moodtunes/model/SharedPlaylist.java` | `@Entity shared_playlists`: playlist, sender, recipient, optional message, sharedAt |
| `backend/src/test/java/com/moodtunes/` | all test files — coordinates with each owner for unit tests; leads integration tests 7.1–7.3 |
