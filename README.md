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

```bash
# 1. MySQL
mysql -u root -p -e "CREATE DATABASE moodtunes;"

# 2. Flask
cd flask-service && pip install -r requirements.txt
python app.py          # :5001

# 3. Spring Boot
cd backend && mvn spring-boot:run   # :8080

# 4. React
cd frontend && npm install && npm start   # :3000
```

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
| GET | `/api/playlists/{id}` | Playlist detail + tracks |
| POST | `/api/playlists/share` | Share with friends |
| GET | `/api/playlists/shared` | Playlists shared with me |
| POST | `/api/friends/request` | Send friend request |
| GET | `/api/friends/requests` | Incoming requests |
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
