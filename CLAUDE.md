# MoodTunes — CLAUDE.md

> **MAINTENANCE RULE:** Update this file in the same PR as any change to architecture, APIs, or dependencies. For file ownership and structure, update RESPONSIBILITIES.md.

## Project

Mood-based playlist generator. User types a mood → Spring Boot calls Flask → Flask calls Gemini AI for 15–20 songs → validates via ytmusicapi → returns YouTube Music URLs → Spring Boot persists to MySQL + saves raw JSON to `playlists/`.

Stack: React `:3000` · Spring Boot `:8080` · Flask `:5001` · MySQL `:3306`  
Packages: `com.moodtunes.{controller,service,repository,model,config}`  
Setup: see README.md

> **⚠ IMPORTANT — READ BEFORE WRITING ANY CODE:** Check [RESPONSIBILITIES.md](RESPONSIBILITIES.md) to find who owns the file(s) you are about to touch. Do not edit another person's files without coordinating first. Co-owned files have explicit boundary notes in that document.

## Constraints

- BCrypt only — never store or log plaintext passwords
- JWT required on all endpoints except `/api/auth/**`
- Gemini API key in Flask `.env` only — never commit, never expose to frontend or Java
- Flask binds to `localhost:5001` only
- CORS: `localhost:3000` only
- `application.properties`: placeholder values only in version control
- Never push directly to `main` — branch + PR always

## Testing

```bash
cd backend && mvn test       # Spring Boot unit + integration tests
cd flask-service && pytest   # Flask tests
```

Suites: Auth · Mood logging · Playlist generation · Playlist library · Friends · Sharing · E2E integration  
Full test specs: `Testing Plan_Revised Version.docx`

## Dependencies

| Layer | File | Packages |
|---|---|---|
| Backend | `pom.xml` | spring-boot-starter-{web,data-jpa,security,test}, mysql-connector-java, jjwt, gson |
| Frontend | `package.json` | react, react-router-dom, axios |
| Flask | `requirements.txt` | flask, google-generativeai, ytmusicapi |
