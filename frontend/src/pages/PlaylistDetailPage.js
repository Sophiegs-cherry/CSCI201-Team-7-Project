import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import './PlaylistDetailPage.css';
import ShareModal from './ShareModal';
import api from '../api/axios';

function PlaylistDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [playlist, setPlaylist] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
  api.get(`/api/playlists/${id}`)
    .then(res => {
      setPlaylist(res.data);
      setLoading(false);
    })
    .catch(() => {
      setError('Could not load this playlist. Please try again.');
      setLoading(false);
    });
}, [id]);

  if (loading) return <div className="detail-container" style={{ color: '#888' }}>Loading...</div>;
  if (error) return <div className="detail-container" style={{ color: '#f87171' }}>{error}</div>;
  if (!playlist) return null;

  const cameFromShared = location.state?.from === 'shared' || playlist.shared === true;
  const backPath = cameFromShared ? '/shared' : '/library';
  const backLabel = cameFromShared ? '← Back to Shared' : '← Back to Library';
  const sharedMessage = location.state?.sharedMessage || playlist.sharedMessage;
  const senderUsername = location.state?.senderUsername || playlist.sharedBy;
  const contextNote = playlist.mood?.contextNote;

  return (
    <div className="detail-container">
      <button className="back-btn" onClick={() => navigate(backPath)}>{backLabel}</button>

      <div className="detail-header">
        <h1 className="detail-title">{playlist.title}</h1>
        <p className="detail-meta">
          Generated on {new Date(playlist.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} · {playlist.tracks.length} songs
        </p>
        {cameFromShared && sharedMessage && (
          <div className="detail-context">
            {senderUsername ? `Message from ${senderUsername}: ` : 'Message: '}"{sharedMessage}"
          </div>
        )}
        {contextNote && <div className="detail-context">"{contextNote}"</div>}
        {!cameFromShared && (
          <div className="detail-actions">
            <button className="btn-share" onClick={() => setShowModal(true)}>Share with Friends</button>
          </div>
        )}
      </div>

      <div className="track-list">
        {playlist.tracks.map(track => (
          <div key={track.trackId} className="track-row">
            <span className="track-order">{track.trackOrder}</span>
            <div className="track-info">
              <span className="track-name">{track.trackName}</span>
              <span className="track-artist">{track.artistName}</span>
            </div>
            <a
              href={track.youtubeMusicUrl}
              target="_blank"
              rel="noreferrer"
              className="yt-link"
            >
              ▶ YouTube Music
            </a>
          </div>
        ))}
      </div>

      {showModal && (
        <ShareModal
          playlistId={playlist.playlistId}
          playlistTitle={playlist.title}
          onClose={() => setShowModal(false)}
        />
      )}
    </div>
  );
}

export default PlaylistDetailPage;
