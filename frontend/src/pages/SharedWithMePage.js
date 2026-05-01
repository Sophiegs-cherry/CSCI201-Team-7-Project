import React, { useState, useEffect } from 'react';
import './SharedWithMePage.css';
import api from '../api/axios';

const avatarColors = ['#FBBF24', '#38BDF8', '#F87171', '#818CF8', '#F472B6', '#FB923C', '#34D399'];

function getAvatarColor(index) {
  return avatarColors[index % avatarColors.length];
}

function SharedWithMePage() {
  const [shared, setShared] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

useEffect(() => {
  api.get('/api/playlists/shared')
    .then(res => {
      setShared(res.data);
      setLoading(false);
    })
    .catch(() => {
      setError('Could not load shared playlists. Please try again.');
      setLoading(false);
    });
}, []);

  return (
    <div className="shared-container">
      <h1 className="shared-title">Shared With Me</h1>
      <p className="shared-subtitle">Playlists your friends have sent you</p>

      {loading && <p style={{ color: '#888' }}>Loading...</p>}
      {error && <p style={{ color: '#f87171' }}>{error}</p>}

      {!loading && !error && shared.length === 0 && (
        <div className="shared-empty">
          <p>No playlists shared with you yet.</p>
        </div>
      )}

      {!loading && !error && shared.length > 0 && (
        <div className="shared-list">
          {shared.map((item, i) => (
  <div key={item.shareId} className="shared-card">
    <div className="shared-sender">
      <div className="sender-avatar" style={{ background: getAvatarColor(i) }}>
        {item.senderUsername ? item.senderUsername.slice(0, 2).toUpperCase() : '??'}
      </div>
      <div className="sender-info">
        <span className="sender-name">{item.senderUsername}</span>
        <span className="sender-date">shared on {new Date(item.sharedAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</span>
      </div>
    </div>

    <div className="shared-playlist-info">
      <h3 className="shared-playlist-title">{item.title}</h3>
      {item.message && <div className="shared-message">"{item.message}"</div>}
    </div>

    <a href={`/playlist/${item.playlistId}`} className="shared-view-btn">
      View Playlist →
    </a>
  </div>
))}
        </div>
      )}
    </div>
  );
}

export default SharedWithMePage;
