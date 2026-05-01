import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import LibraryPage from './pages/LibraryPage';
import PlaylistDetailPage from './pages/PlaylistDetailPage';
import FriendsPage from './pages/FriendsPage';
import SharedWithMePage from './pages/SharedWithMePage';

function Navbar() {
  return (
    <nav style={{
      background: '#1976D2',
      padding: '0 24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      height: '52px',
    }}>
      <span style={{ color: 'white', fontWeight: 'bold', fontSize: '1.1rem' }}>MoodTunes</span>
      <div style={{ display: 'flex', gap: '24px' }}>
        {['Dashboard', 'Library', 'Friends', 'Shared', 'History', 'Logout'].map(item => (
          <Link
            key={item}
            to={item === 'Dashboard' ? '/' : `/${item.toLowerCase()}`}
            style={{ color: 'white', textDecoration: 'none', fontSize: '0.95rem' }}
          >
            {item}
          </Link>
        ))}
      </div>
    </nav>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/library" element={<LibraryPage />} />
        <Route path="/playlist/:id" element={<PlaylistDetailPage />} />
        <Route path="/friends" element={<FriendsPage />} />
        <Route path="/shared" element={<SharedWithMePage />} />
        <Route path="/" element={<LibraryPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;