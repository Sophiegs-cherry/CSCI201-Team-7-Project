# MoodTunes

CSCI 201 · Professor Papa · Spring 2026 · Team 7

React + Spring Boot + Flask AI web app that generates mood-based playlists via Gemini AI, validated against YouTube Music.

## Architecture

```
React :3000  →  Spring Boot :8080  →  Flask :5001  →  Gemini AI + YouTube Music
                      ↕
                  MySQL :3306
```

## Setup

**Prerequisites:** Java 17+, Python 3.9+, Node 18+, MySQL 8, Maven 3.8+

### Local configuration

These files are intentionally not committed and must be created locally:

| File | What to set |
|---|---|
| `backend/src/main/resources/application.properties` | MySQL username/password, `jwt.secret`, optional port/CORS/Flask URL overrides |
| `flask-service/.env` | `GEMINI_API_KEY` |

Create them from the examples:

```bash
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
cp flask-service/.env.example flask-service/.env
```

Then edit:

```properties
# backend/src/main/resources/application.properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
jwt.secret=CHANGE_ME_TO_A_32_PLUS_CHARACTER_SECRET
jwt.expiration=86400000
```

```bash
# flask-service/.env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

`jwt.secret` should be a private random string with at least 32 characters. Do not commit your real `application.properties` or `.env`.

### Run locally

Run these from the repository root, usually in separate terminal windows:

```bash
# 1. MySQL
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS moodtunes;"

# 2. Flask (:5001)
(cd flask-service && python3 -m venv .venv)
(cd flask-service && .venv/bin/pip install -r requirements.txt)
(cd flask-service && .venv/bin/python app.py)

# 3. Spring Boot (:8080)
(cd backend && mvn spring-boot:run)

# 4. React (:3000)
(cd frontend && npm install)
(cd frontend && npm start)
```

## Run Tests

Run each test suite from the repository root:

```bash
# Backend Spring Boot tests
(cd backend && mvn test)
```

```bash
# Flask AI service tests
(cd flask-service && .venv/bin/python -m pytest)
```

If you do not have the Flask virtual environment yet:

```bash
(cd flask-service && python3 -m venv .venv)
(cd flask-service && .venv/bin/pip install -r requirements.txt pytest pytest-mock)
(cd flask-service && .venv/bin/python -m pytest)
```

```bash
# Frontend React tests
(cd frontend && npm test -- --watchAll=false)
```

To run one frontend test file:

```bash
(cd frontend && npm test -- LoginPage.test.js --watchAll=false)
```

The backend test configuration includes the JVM flag needed for Java 25 Mockito/Byte Buddy compatibility. On Java 17, the same `mvn test` command also works.

## API Endpoints

All endpoints except `/api/auth/**` require `Authorization: Bearer <JWT>`.

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register |
| POST | `/api/auth/login` | Login → JWT |
| POST | `/api/moods/log` | Log mood |
| GET | `/api/moods/history` | Mood history |
| POST | `/api/playlists/generate` | Generate AI playlist |
| POST | `/api/playlists/save` | Save to library |
| GET | `/api/playlists/library` | List saved playlists |
| GET | `/api/playlists/{id}` | Playlist detail + tracks for owner or shared recipient |
| POST | `/api/playlists/share` | Share with accepted friends |
| GET | `/api/playlists/shared` | Playlists shared with me |
| POST | `/api/friends/request` | Send friend request |
| GET | `/api/friends/requests` | Incoming requests |
| GET | `/api/friends/requests/sent` | Sent requests |
| POST | `/api/friends/accept/{id}` | Accept request |
| POST | `/api/friends/decline/{id}` | Decline request |
| GET | `/api/friends` | List friends |
| DELETE | `/api/friends/{id}` | Remove friend |
| GET | `/api/friends/search?username=` | Search users |

## Team Branches

| Branch | Owner |
|---|---|
| `trevor/frontend-auth` | Trevor |
| `simra/frontend-mood-playlist` | Simra |
| `matthew/frontend-library-social` | Matthew Kwok |
| `sophie/backend-auth` | Sophie Shim |
| `wenwei/backend-mood-playlist` | Wenwei Fu |
| `daniel/flask-ai-service` | Daniel Sim |
| `sid/backend-java-flask-integration` | Sid Goyal |
| `suzy/backend-social-testing` | Suzy Xu |

Branch off `main`. Merge back via PR only. See [RESPONSIBILITIES.md](RESPONSIBILITIES.md) for per-person file ownership.
