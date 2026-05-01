import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
 
const MOOD_COLORS = {
  happy:     { color: "#FBBF24", emoji: "☀️" },
  calm:      { color: "#38BDF8", emoji: "🌊" },
  energetic: { color: "#F87171", emoji: "⚡" },
  sad:       { color: "#818CF8", emoji: "🌧️" },
  romantic:  { color: "#F472B6", emoji: "🌸" },
  angry:     { color: "#FB923C", emoji: "🔥" },
  anxious:   { color: "#A78BFA", emoji: "🌀" },
  focused:   { color: "#34D399", emoji: "🎯" },
};
 
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
 
function TrackRow({ track, index }) {
  const [imgError, setImgError] = useState(false);
  return (
    <div style={s.trackRow}>
      <span style={s.trackNum}>{index + 1}</span>
      <div style={s.albumArt}>
        {track.imageUrl && !imgError ? (
          <img src={track.imageUrl} alt="album" style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: 10 }} onError={() => setImgError(true)} />
        ) : (
          <span style={{ fontSize: 18 }}>🎵</span>
        )}
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={s.trackName}>{track.trackName}</div>
        <div style={s.trackArtist}>{track.artistName}</div>
      </div>
      {track.youtubeMusicUrl && (
        <a href={track.youtubeMusicUrl} target="_blank" rel="noopener noreferrer" style={s.ytLink} title="Open in YouTube Music">▶</a>
      )}
    </div>
  );
}
 
export default function PlaylistPage() {
  const location = useLocation();
  const navigate = useNavigate();
 
  const playlist   = location.state?.playlist;
  const moodId     = location.state?.moodId;
  const moodLabel  = location.state?.moodLabel;
  const moodColor  = location.state?.moodColor;
  const moodEmoji  = location.state?.moodEmoji;
  const contextNote = location.state?.contextNote;
 
  const [saved, setSaved] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
 
  if (!playlist) {
    return (
      <div style={{ ...s.page, display: "flex", alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 16 }}>
        <p style={{ color: "#888" }}>No playlist found.</p>
        <button onClick={() => navigate("/dashboard")} style={{ ...s.newBtn, cursor: "pointer" }}>← Back to Dashboard</button>
      </div>
    );
  }
 
  const moodKey = moodLabel?.toLowerCase() || "calm";
  const fallback = MOOD_COLORS[moodKey] || MOOD_COLORS.calm;
  const color = moodColor || fallback.color;
  const emoji = moodEmoji || fallback.emoji;
  const tracks = playlist.tracks || [];
 
  const handleSave = async () => {
    if (saved) return;
    setSaving(true); setSaveError("");
    try {
      const token = localStorage.getItem("token");
      await axios.post(
        "/api/playlists/save",
        {
          title: playlist.title || `${moodLabel} Vibes`,
          moodId,
          tracks: tracks.map((t, i) => ({
            trackName: t.trackName,
            artistName: t.artistName,
            youtubeMusicUrl: t.youtubeMusicUrl || "",
            trackOrder: t.trackOrder || i + 1,
          })),
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setSaved(true);
    } catch (err) {
      setSaveError(err.response?.data?.error || "Could not save. Please try again.");
    } finally { setSaving(false); }
  };
 
  return (
    <div style={s.page}>
      <Navbar />
 
      <div style={s.container}>
        <button onClick={() => navigate("/dashboard")} style={s.backLink}>← Generate another</button>
 
        <div style={s.headerCard}>
          <div style={{ ...s.glow, background: color }} />
          <div style={{ ...s.moodTag, color }}>{emoji} {moodLabel}</div>
          <h1 style={s.plTitle}>{playlist.title || `${moodLabel} Vibes`}</h1>
          <div style={s.metaRow}>
            <span>{tracks.length} songs</span>
            <span>·</span>
            {playlist.createdAt && (
              <span>{new Date(playlist.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}</span>
            )}
          </div>
          {contextNote && (
            <div style={{ ...s.ctxNote, borderLeftColor: color }}>"{contextNote}"</div>
          )}
          <div style={s.actionRow}>
            <button
              onClick={handleSave}
              disabled={saving || saved}
              style={{
                ...s.saveBtn,
                background: saved ? "#0d2e1f" : color + "22",
                border: `1.5px solid ${saved ? "#34d399" : color}`,
                color: saved ? "#34d399" : color,
                opacity: saving ? 0.6 : 1,
                cursor: saving || saved ? "default" : "pointer",
              }}
            >
              {saving ? "Saving..." : saved ? "✓ Saved to Library" : "💾 Save to Library"}
            </button>
            <button onClick={() => navigate("/dashboard")} style={{ ...s.newBtn, cursor: "pointer" }}>🎲 New Playlist</button>
          </div>
          {saveError && <div style={s.errorBox}>⚠️ {saveError}</div>}
        </div>
 
        <div style={s.trackList}>
          <div style={s.trackListHeader}>
            <span style={s.tlTitle}>Tracks</span>
            <span style={s.tlCount}>{tracks.length} songs</span>
          </div>
          {tracks.length === 0
            ? <div style={{ padding: "2rem", textAlign: "center", color: "#555", fontSize: 14 }}>No tracks found.</div>
            : tracks.sort((a, b) => (a.trackOrder || 0) - (b.trackOrder || 0)).map((track, i) => (
                <TrackRow key={track.trackId ?? i} track={track} index={i} />
              ))
          }
        </div>
 
        {saved && (
          <div style={s.savedBanner}>
            <span>✓ Playlist saved! View it in your </span>
            <button onClick={() => navigate("/library")} style={s.libLink}>Library →</button>
          </div>
        )}
      </div>
    </div>
  );
}
 
const s = {
  page: { background: "#0d0d14", minHeight: "100vh", fontFamily: "'Segoe UI', system-ui, sans-serif" },
  container: { maxWidth: 640, margin: "0 auto", padding: "28px 20px 60px", display: "flex", flexDirection: "column", gap: 20 },
  backLink: { background: "none", border: "none", color: "#888", fontSize: 13, cursor: "pointer", fontFamily: "inherit", alignSelf: "flex-start", padding: "4px 0" },
  headerCard: { background: "#16161f", borderRadius: 20, padding: 24, border: "1.5px solid #2a2a3a", position: "relative", overflow: "hidden" },
  glow: { position: "absolute", top: -60, right: -60, width: 200, height: 200, borderRadius: "50%", opacity: 0.15, filter: "blur(60px)", pointerEvents: "none" },
  moodTag: { fontSize: 11, fontWeight: 800, textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 10, display: "inline-flex", alignItems: "center", gap: 5 },
  plTitle: { fontSize: "clamp(1.3rem,4vw,1.7rem)", fontWeight: 800, color: "#fff", letterSpacing: "-0.03em", marginBottom: 6, lineHeight: 1.2 },
  metaRow: { display: "flex", alignItems: "center", gap: 8, fontSize: 13, color: "#666" },
  ctxNote: { marginTop: 14, padding: "12px 16px", background: "#ffffff08", borderLeft: "2px solid", borderRadius: "0 10px 10px 0", color: "#aaa", fontSize: 13, fontStyle: "italic", lineHeight: 1.6 },
  actionRow: { display: "flex", gap: 10, marginTop: 18, flexWrap: "wrap" },
  saveBtn: { display: "inline-flex", alignItems: "center", gap: 6, padding: "11px 20px", borderRadius: 12, fontWeight: 800, fontSize: 13, fontFamily: "inherit", transition: "all 0.2s" },
  newBtn: { padding: "11px 20px", borderRadius: 12, border: "1.5px solid #3a3a50", background: "#1e1e2a", color: "#ccc", fontWeight: 700, fontSize: 13, fontFamily: "inherit" },
  errorBox: { background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.3)", color: "#f87171", borderRadius: 10, padding: "10px 14px", fontSize: 13, marginTop: 10 },
  trackList: { background: "#16161f", borderRadius: 20, border: "1.5px solid #2a2a3a", overflow: "hidden" },
  trackListHeader: { display: "flex", justifyContent: "space-between", alignItems: "center", padding: "16px 20px", borderBottom: "1px solid #2a2a3a" },
  tlTitle: { fontSize: 11, fontWeight: 800, color: "#888", textTransform: "uppercase", letterSpacing: "0.1em" },
  tlCount: { fontSize: 12, color: "#666" },
  trackRow: { display: "flex", alignItems: "center", gap: 14, padding: "11px 20px", borderBottom: "1px solid #1e1e2a" },
  trackNum: { fontSize: 12, color: "#555", fontWeight: 600, minWidth: 18, textAlign: "right" },
  albumArt: { width: 42, height: 42, borderRadius: 10, background: "#222233", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, fontSize: 18 },
  trackName: { fontSize: 14, fontWeight: 700, color: "#eee", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" },
  trackArtist: { fontSize: 12, color: "#777", marginTop: 2 },
  ytLink: { color: "#ff4444", fontSize: 16, textDecoration: "none", flexShrink: 0, opacity: 0.8 },
  savedBanner: { background: "#0d2e1f", border: "1.5px solid #34d39966", color: "#34d399", borderRadius: 14, padding: "14px 18px", fontSize: 13, fontWeight: 600, display: "flex", alignItems: "center", gap: 8 },
  libLink: { background: "none", border: "none", color: "#34d399", fontWeight: 800, cursor: "pointer", fontFamily: "inherit", fontSize: "inherit", textDecoration: "underline", padding: 0 },
};