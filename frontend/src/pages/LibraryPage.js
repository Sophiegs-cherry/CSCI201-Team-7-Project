import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './LibraryPage.css';
import api from '../api/axios';

const moodColors = {
  happy: '#FBBF24',
  calm: '#38BDF8',
  sad: '#818CF8',
  energetic: '#F87171',
  anxious: '#A78BFA',
  romantic: '#F472B6',
  angry: '#FB923C',
  focused: '#34D399',
};

function PlaylistCard({ playlist }) {
  const accentColor = moodColors[playlist.mood?.toLowerCase()] || '#607D8B';
  const songCount = playlist.songCount ?? playlist.trackCount ?? 0;
  const moodText = playlist.moodText ?? playlist.mood ?? 'Mood unavailable';

  return (
    <div className="playlist-card">
      <div className="card-accent" style={{ backgroundColor: accentColor }} />
      <div className="card-body">
        <h3 className="card-title">{playlist.title}</h3>
        <p className="card-meta">{new Date(playlist.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} · {songCount} songs</p>
        <p className="card-mood">"{moodText}"</p>
        <Link to={`/playlist/${playlist.playlistId}`} className="card-link">View playlist →</Link>
      </div>
    </div>
  );
}

function LibraryPage() {
  const [playlists, setPlaylists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

useEffect(() => {
  api.get('/api/playlists/library')
    .then(res => {
      setPlaylists(res.data);
      setLoading(false);
    })
    .catch(() => {
      setError('Could not load your library. Please try again.');
      setLoading(false);
    });
}, []);

  return (
    <div className="library-container">
      <div className="library-inner">
        <h1 className="library-title">My Library</h1>
        <p className="library-subtitle">Your saved playlists</p>

        {loading && <p style={{ color: '#888' }}>Loading...</p>}
        {error && <p style={{ color: '#f87171' }}>{error}</p>}

        {!loading && !error && playlists.length === 0 && (
          <p style={{ color: '#555' }}>No saved playlists yet. Generate one from the dashboard!</p>
        )}

        <div className="library-grid">
          {playlists.map(p => (
            <PlaylistCard key={p.playlistId} playlist={p} />
          ))}
        </div>
      </div>
    </div>
  );
}

export default LibraryPage;
