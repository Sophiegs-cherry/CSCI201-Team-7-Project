import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const MOODS = [
  { key: "happy",     label: "Happy",     emoji: "☀️", color: "#FBBF24" },
  { key: "calm",      label: "Calm",      emoji: "🌊", color: "#38BDF8" },
  { key: "energetic", label: "Energetic", emoji: "⚡", color: "#F87171" },
  { key: "sad",       label: "Sad",       emoji: "🌧️", color: "#818CF8" },
  { key: "romantic",  label: "Romantic",  emoji: "🌸", color: "#F472B6" },
  { key: "angry",     label: "Angry",     emoji: "🔥", color: "#FB923C" },
  { key: "anxious",   label: "Anxious",   emoji: "🌀", color: "#A78BFA" },
  { key: "focused",   label: "Focused",   emoji: "🎯", color: "#34D399" },
];

const GENRES = ["Pop","Rock","Hip-Hop","Electronic","Jazz","Indie","R&B","Classical","Metal","Lo-Fi"];

function Navbar({ active }) {
  const navigate = useNavigate();
  return (
    <nav style={nav.bar}>
      <span style={nav.brand} onClick={() => navigate("/dashboard")}>
        <span style={nav.dot} />
        MoodTunes
      </span>
      <div style={nav.links}>
        {["Dashboard","Library","Friends","History"].map((l) => (
          <span
            key={l}
            onClick={() => navigate("/" + l.toLowerCase())}
            style={{
              ...nav.link,
              color: active === l ? "#fff" : "rgba(255,255,255,0.4)",
              fontWeight: active === l ? 700 : 500,
            }}
          >
            {l}
          </span>
        ))}
        <span style={{ ...nav.link, color: "rgba(255,255,255,0.4)" }}>Logout</span>
      </div>
    </nav>
  );
}

const nav = {
  bar: { display: "flex", alignItems: "center", justifyContent: "space-between", padding: "16px 28px", borderBottom: "1px solid rgba(255,255,255,0.08)", background: "#0d0d14" },
  brand: { color: "#fff", fontWeight: 800, fontSize: 18, letterSpacing: "-0.03em", display: "flex", alignItems: "center", gap: 8, cursor: "pointer" },
  dot: { width: 8, height: 8, borderRadius: "50%", background: "#a78bfa", display: "inline-block" },
  links: { display: "flex", gap: 28 },
  link: { fontSize: 14, cursor: "pointer", transition: "color 0.15s" },
};

export default function DashboardPage() {
  const navigate = useNavigate();
  const [selectedMood, setSelectedMood] = useState(null);
  const [selectedGenres, setSelectedGenres] = useState([]);
  const [contextNote, setContextNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const toggleGenre = (genre) => {
    if (selectedGenres.includes(genre)) {
      setSelectedGenres(selectedGenres.filter((g) => g !== genre));
    } else if (selectedGenres.length < 3) {
      setSelectedGenres([...selectedGenres, genre]);
    }
  };

  const handleGenerate = async () => {
    if (!selectedMood) { setError("Please select a mood first."); return; }
    setError("");
    setLoading(true);
    try {
      const token = localStorage.getItem("token");
      const headers = { Authorization: `Bearer ${token}` };

      const moodText = `I'm feeling ${selectedMood.label.toLowerCase()}${
        selectedGenres.length > 0 ? `, and I enjoy ${selectedGenres.join(", ")}` : ""
      }`;
      const musicPreferences = selectedGenres.length > 0 ? selectedGenres.join(", ") : undefined;

      const moodResponse = await axios.post(
        "/api/moods/log",
        { moodText, musicPreferences, contextNote: contextNote || undefined },
        { headers }
      );
      const moodId = moodResponse.data.moodId;

      const playlistResponse = await axios.post(
        "/api/playlists/generate",
        { moodId },
        { headers }
      );

      navigate("/playlist", {
        state: {
          playlist: playlistResponse.data,
          moodId,
          moodLabel: selectedMood.label,
          moodColor: selectedMood.color,
          moodEmoji: selectedMood.emoji,
          contextNote,
        },
      });
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const m = selectedMood;

  return (
    <div style={s.page}>
      <Navbar active="Dashboard" />

      <div style={s.container}>
        <div>
          <h1 style={s.title}>How are you feeling<br />today?</h1>
          <p style={s.subtitle}>Select your mood to generate a personalized playlist</p>
        </div>

        <div style={s.moodGrid}>
          {MOODS.map((mood) => {
            const isSelected = selectedMood?.key === mood.key;
            return (
              <button
                key={mood.key}
                onClick={() => { setSelectedMood(mood); setError(""); }}
                style={{
                  ...s.moodBtn,
                  background: isSelected ? "#0d0d14" : "#16161f",
                  border: `1.5px solid ${isSelected ? mood.color : "#2a2a3a"}`,
                  boxShadow: isSelected ? `0 0 18px ${mood.color}44` : "none",
                  transform: isSelected ? "translateY(-3px)" : "none",
                }}
              >
                <span style={{ fontSize: 26, lineHeight: 1 }}>{mood.emoji}</span>
                <span style={{ ...s.moodLabel, color: isSelected ? mood.color : "#ccc" }}>
                  {mood.label}
                </span>
              </button>
            );
          })}
        </div>

        {m && (
          <div style={{ ...s.selectedBar, borderColor: m.color + "66", color: m.color }}>
            <span style={{ fontSize: 14 }}>{m.emoji}</span>
            <span>{m.label} selected</span>
          </div>
        )}

        <div>
          <div style={s.secLabel}>
            Genre preferences <span style={s.secHint}>· pick up to 3</span>
          </div>
          <div style={s.genreGrid}>
            {GENRES.map((genre) => {
              const isSelected = selectedGenres.includes(genre);
              const disabled = !isSelected && selectedGenres.length >= 3;
              return (
                <button
                  key={genre}
                  onClick={() => !disabled && toggleGenre(genre)}
                  style={{
                    ...s.genreChip,
                    background: isSelected ? "#a78bfa" : "#ffffff",
                    color: isSelected ? "#fff" : "#111",
                    opacity: disabled ? 0.3 : 1,
                    cursor: disabled ? "not-allowed" : "pointer",
                  }}
                >
                  {genre}
                </button>
              );
            })}
          </div>
        </div>

        <div>
          <div style={s.secLabel}>
            What's on your mind? <span style={s.secHint}>· optional</span>
          </div>
          <textarea
            value={contextNote}
            onChange={(e) => setContextNote(e.target.value)}
            placeholder="Write about why you're feeling this way..."
            maxLength={500}
            rows={4}
            style={s.textarea}
          />
          <div style={s.charCount}>{contextNote.length}/500</div>
        </div>

        {error && <div style={s.errorBox}>⚠️ {error}</div>}

        <button
          onClick={handleGenerate}
          disabled={loading || !selectedMood}
          style={{
            ...s.genBtn,
            background: m ? `linear-gradient(135deg, ${m.color}, ${m.color}bb)` : "#2a2a3a",
            boxShadow: m ? `0 6px 20px ${m.color}44` : "none",
            color: m ? "#000" : "#666",
            opacity: loading || !selectedMood ? 0.4 : 1,
            cursor: loading || !selectedMood ? "not-allowed" : "pointer",
          }}
        >
          {loading ? "⏳ Generating..." : "🎵 Generate Playlist"}
        </button>
      </div>
    </div>
  );
}

const s = {
  page: { background: "#0d0d14", minHeight: "100vh", fontFamily: "'Segoe UI', system-ui, sans-serif" },
  container: { maxWidth: 640, margin: "0 auto", padding: "32px 20px 60px", display: "flex", flexDirection: "column", gap: 28 },
  title: { fontSize: "clamp(1.5rem,4vw,1.9rem)", fontWeight: 800, color: "#fff", letterSpacing: "-0.04em", lineHeight: 1.2 },
  subtitle: { fontSize: 14, color: "#888", marginTop: 6 },
  moodGrid: { display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 8 },
  moodBtn: { display: "flex", flexDirection: "column", alignItems: "center", gap: 7, padding: "16px 8px", borderRadius: 16, cursor: "pointer", transition: "all 0.2s", fontFamily: "inherit" },
  moodLabel: { fontSize: 12, fontWeight: 700, letterSpacing: "0.02em" },
  selectedBar: { display: "flex", alignItems: "center", justifyContent: "center", padding: "10px 20px", borderRadius: 12, fontSize: 13, fontWeight: 700, gap: 6, border: "1.5px solid", background: "transparent" },
  secLabel: { fontSize: 12, fontWeight: 800, color: "#bbb", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 12 },
  secHint: { fontWeight: 500, color: "#666", textTransform: "none", letterSpacing: 0 },
  genreGrid: { display: "flex", flexWrap: "wrap", gap: 8 },
  genreChip: { padding: "9px 18px", borderRadius: 999, border: "none", fontSize: 13, fontWeight: 700, fontFamily: "inherit", transition: "all 0.15s" },
  textarea: { width: "100%", padding: "14px 16px", borderRadius: 14, border: "1.5px solid #2a2a3a", fontSize: 14, fontFamily: "inherit", color: "#eee", background: "#16161f", resize: "none", outline: "none", lineHeight: 1.6, boxSizing: "border-box" },
  charCount: { fontSize: 11, color: "#555", textAlign: "right", marginTop: 5 },
  errorBox: { background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.3)", color: "#f87171", borderRadius: 10, padding: "10px 14px", fontSize: 13 },
  genBtn: { width: "100%", padding: 16, borderRadius: 14, border: "none", fontSize: 15, fontWeight: 800, fontFamily: "inherit", letterSpacing: "0.01em", transition: "all 0.2s" },
};