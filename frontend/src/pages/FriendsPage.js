import React, { useState, useEffect } from 'react';
import './FriendsPage.css';
import api from '../api/axios';

const avatarColors = ['#FBBF24', '#38BDF8', '#F87171', '#818CF8', '#F472B6', '#FB923C', '#34D399'];

function getInitials(name) {
  if (!name) return '??';
  return name.split(' ').map(w => w[0]).join('').toUpperCase();
}

function getAvatarColor(index) {
  return avatarColors[index % avatarColors.length];
}

function FriendsPage() {
  const [activeTab, setActiveTab] = useState('friends');
  const [friends, setFriends] = useState([]);
  const [received, setReceived] = useState([]);
  const [sent, setSent] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [searchError, setSearchError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/api/friends'),
      api.get('/api/friends/requests'),
      api.get('/api/friends/requests/sent'),
    ])
      .then(([friendsRes, receivedRes, sentRes]) => {
        setFriends(friendsRes.data);
        setReceived(receivedRes.data);
        setSent(sentRes.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  function handleSearch() {
    if (!searchQuery.trim()) return;
    setSearchError('');
    setSearchResult(null);
    api.get(`/api/friends/search?username=${searchQuery}`)
      .then(res => setSearchResult(res.data))
      .catch(() => setSearchError('User not found.'));
  }

  function handleAddFriend(userId) {
    api.post('/api/friends/request', { targetUserId: userId })
      .then(() => {
        setSearchResult(null);
        setSearchQuery('');
      });
  }

  function handleAccept(friendshipId) {
    api.post(`/api/friends/accept/${friendshipId}`)
      .then(() => {
        const accepted = received.find(r => r.friendshipId === friendshipId);
        setReceived(received.filter(r => r.friendshipId !== friendshipId));
        if (accepted) setFriends(prev => [...prev, accepted]);
      });
  }

  function handleDecline(friendshipId) {
    api.post(`/api/friends/decline/${friendshipId}`)
      .then(() => setReceived(received.filter(r => r.friendshipId !== friendshipId)));
  }

  function handleRemove(friendshipId) {
    api.delete(`/api/friends/${friendshipId}`)
      .then(() => setFriends(friends.filter(f => f.friendshipId !== friendshipId)));
  }

  return (
    <div className="friends-container">
      <div className="friends-inner">
        <h1 className="friends-title">Friends</h1>

        <div className="search-bar">
          <input
            type="text"
            placeholder="Search users by username..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            className="search-input"
          />
          <button className="search-btn" onClick={handleSearch}>Search</button>
        </div>

        {searchError && <p style={{ color: '#f87171', marginBottom: 12 }}>{searchError}</p>}

        {searchResult && searchResult.length > 0 && (
          <div className="search-result">
            {searchResult.map((user, i) => (
              <div className="friend-card" key={user.userId}>
                <div className="friend-avatar" style={{ background: getAvatarColor(i) }}>
                  {getInitials(user.displayName || user.username)}
                </div>
                <div className="friend-info">
                  <span className="friend-username">{user.username}</span>
                  <span className="friend-displayname">{user.displayName}</span>
                </div>
                <button className="btn-add" onClick={() => handleAddFriend(user.userId)}>Add Friend</button>
              </div>
            ))}
          </div>
        )}

        <div className="tabs">
          <button className={`tab ${activeTab === 'friends' ? 'tab-active' : ''}`} onClick={() => setActiveTab('friends')}>
            My Friends ({friends.length})
          </button>
          <button className={`tab ${activeTab === 'received' ? 'tab-active' : ''}`} onClick={() => setActiveTab('received')}>
            Received ({received.length})
          </button>
          <button className={`tab ${activeTab === 'sent' ? 'tab-active' : ''}`} onClick={() => setActiveTab('sent')}>
            Sent ({sent.length})
          </button>
        </div>

        {loading && <p style={{ color: '#888' }}>Loading...</p>}

        <div className="tab-content">
          {activeTab === 'friends' && (
            <div className="friends-grid">
              {friends.length === 0 && !loading && <p style={{ color: '#555' }}>No friends yet. Search for users above!</p>}
              {friends.map((friend, i) => (
                <div key={friend.friendshipId} className="friend-card">
                  <div className="friend-avatar" style={{ background: getAvatarColor(i) }}>
                    {getInitials(friend.user?.displayName || friend.user?.username)}
                  </div>
                  <div className="friend-info">
                    <span className="friend-username">{friend.user?.username}</span>
                    <span className="friend-displayname">{friend.user?.displayName}</span>
                  </div>
                  <button className="btn-remove" onClick={() => handleRemove(friend.friendshipId)}>Remove</button>
                </div>
              ))}
            </div>
          )}

          {activeTab === 'received' && (
            <div className="friends-grid">
              {received.length === 0 && !loading && <p style={{ color: '#555' }}>No pending requests.</p>}
              {received.map((req, i) => (
                <div key={req.friendshipId} className="friend-card">
                  <div className="friend-avatar" style={{ background: getAvatarColor(i) }}>
                    {getInitials(req.user?.displayName || req.user?.username)}
                  </div>
                  <div className="friend-info">
                    <span className="friend-username">{req.user?.username}</span>
                    <span className="friend-displayname">{req.user?.displayName}</span>
                  </div>
                  <div className="request-actions">
                    <button className="btn-accept" onClick={() => handleAccept(req.friendshipId)}>Accept</button>
                    <button className="btn-decline" onClick={() => handleDecline(req.friendshipId)}>Decline</button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeTab === 'sent' && (
            <div className="friends-grid">
              {sent.length === 0 && !loading && <p style={{ color: '#555' }}>No sent requests.</p>}
              {sent.map((req, i) => (
                <div key={req.friendshipId} className="friend-card">
                  <div className="friend-avatar" style={{ background: getAvatarColor(i) }}>
                    {getInitials(req.user?.displayName || req.user?.username)}
                  </div>
                  <div className="friend-info">
                    <span className="friend-username">{req.user?.username}</span>
                    <span className="friend-displayname">{req.user?.displayName}</span>
                  </div>
                  <span className="pending-label">Pending</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default FriendsPage;