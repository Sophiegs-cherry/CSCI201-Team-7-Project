import React, { useState, useEffect } from 'react';
import './ShareModal.css';
import api from '../api/axios';

const avatarColors = ['#FBBF24', '#38BDF8', '#F87171', '#818CF8', '#F472B6', '#FB923C', '#34D399'];

function getInitials(displayName) {
  const name = String(displayName || '').trim();
  if (!name) return '?';
  return name.split(/\s+/).map(w => w[0]).join('').toUpperCase();
}

function getAvatarColor(index) {
  return avatarColors[index % avatarColors.length];
}

function ShareModal({ playlistId, playlistTitle = 'this playlist', onClose }) {
  const [friends, setFriends] = useState([]);
  const [selected, setSelected] = useState([]);
  const [message, setMessage] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');

useEffect(() => {
  api.get('/api/friends')
    .then(res => setFriends(res.data))
    .catch(() => setError('Could not load friends.'));
}, []);

  function toggleFriend(userId) {
    setSelected(prev =>
      prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
    );
  }

  function handleSend() {
    if (selected.length === 0) return;
    api.post('/api/playlists/share', { playlistId, recipientIds: selected, message })
      .then(() => setSent(true))
      .catch(() => setError('Failed to share playlist. Please try again.'));
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        {sent ? (
          <div className="modal-sent">
            <div className="sent-icon">✓</div>
            <h2>Playlist Shared!</h2>
            <p>Your playlist has been sent to {selected.length} friend{selected.length > 1 ? 's' : ''}.</p>
            <button className="btn-done" onClick={onClose}>Done</button>
          </div>
        ) : (
          <>
            <h2 className="modal-title">Share Playlist</h2>
            <p className="modal-subtitle">Select friends to share "{playlistTitle}" with</p>

            {error && <p style={{ color: '#f87171', fontSize: '0.85rem', marginBottom: 12 }}>{error}</p>}

            <div className="modal-friends">
              {friends.length === 0 && !error && (
                <p style={{ color: '#555', fontSize: '0.9rem' }}>No friends to share with yet.</p>
              )}
              {friends.map((friendship, i) => {
                const friend = friendship.user || friendship;
                const userId = friend.userId;
                const displayName = friend.displayName || friend.username || 'Unknown user';

                return (
                  <div
                    key={userId ?? friendship.friendshipId ?? i}
                    className={`modal-friend-row ${selected.includes(userId) ? 'selected' : ''}`}
                    onClick={() => userId && toggleFriend(userId)}
                  >
                    <div className="modal-checkbox">
                      {selected.includes(userId) ? '✓' : ''}
                    </div>
                    <div className="modal-avatar" style={{ background: getAvatarColor(i) }}>
                      {getInitials(displayName)}
                    </div>
                    <div className="modal-friend-info">
                      <span className="modal-friend-username">{friend.username || displayName}</span>
                      <span className="modal-friend-displayname">{displayName}</span>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="modal-message-section">
              <label className="modal-message-label">Add a message (optional)</label>
              <input
                type="text"
                className="modal-message-input"
                placeholder="This playlist really helped me today!"
                value={message}
                onChange={e => setMessage(e.target.value)}
              />
            </div>

            <div className="modal-actions">
              <button className="btn-cancel" onClick={onClose}>Cancel</button>
              <button
                className={`btn-send ${selected.length === 0 ? 'btn-send-disabled' : ''}`}
                onClick={handleSend}
              >
                Send
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default ShareModal;
