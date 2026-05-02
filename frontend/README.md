# MoodTunes Frontend

React app for MoodTunes. It runs on `http://localhost:3000` and talks to the Spring Boot API at `http://localhost:8080`.

## Scripts

```bash
npm install
npm start
```

Runs the development server on port `3000`.

```bash
npm test -- --watchAll=false
```

Runs the React/Jest test suite once. Page and modal tests live in `src/pages/test/`.

```bash
npm test -- LoginPage.test.js --watchAll=false
```

Runs one test file.

```bash
npm run build
```

Creates a production build in `build/`.

## App Structure

| Path | Purpose |
|---|---|
| `src/App.js` | Routes and protected routes |
| `src/api/axios.js` | Axios client with JWT authorization header |
| `src/context/AuthContext.js` | Auth state, login, logout, token storage |
| `src/components/Navbar.js` | Shared navigation |
| `src/pages/` | App pages and page-specific CSS |
| `src/pages/test/` | React Testing Library page/component tests |
