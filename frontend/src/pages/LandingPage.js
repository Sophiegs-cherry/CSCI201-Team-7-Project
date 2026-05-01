import React from 'react';
import { useNavigate } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="landing-page">
      {/* Navigation Bar */}
      <nav className="landing-nav">
        <div className="nav-brand">
          <span className="brand-icon">🎵</span>
          <span className="brand-name">MoodTunes</span>
        </div>
        <div className="nav-actions">
          <button className="nav-btn login-btn" onClick={() => navigate('/login')}>
            Log In
          </button>
          <button className="nav-btn signup-btn" onClick={() => navigate('/register')}>
            Sign Up
          </button>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">Your Mood. Your Music.</h1>
          <p className="hero-subtitle">
            Tell us how you feel and get a personalized playlist powered by AI
          </p>
          <div className="hero-buttons">
            <button className="cta-primary" onClick={() => navigate('/register')}>
              Sign Up
            </button>
            <button className="cta-secondary" onClick={() => navigate('/login')}>
              Log In
            </button>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="how-it-works">
        <h2 className="section-title">How It Works</h2>
        <div className="steps-grid">
          <div className="step-card">
            <div className="step-number">1</div>
            <h3>Share Your Mood</h3>
            <p>Type how you're feeling in a text box</p>
          </div>
          <div className="step-card">
            <div className="step-number">2</div>
            <h3>AI Generates Songs</h3>
            <p>Gemini AI picks 15-20 songs matching your vibe</p>
          </div>
          <div className="step-card">
            <div className="step-number">3</div>
            <h3>Listen on YouTube</h3>
            <p>Every song has a clickable YouTube Music link</p>
          </div>
        </div>
      </section>

      {/* Interactive Preview Section */}
      <section className="preview-section">
        <h2 className="section-title">Try It Out</h2>
        <p className="section-subtitle">See how mood selection works</p>
        
        <div className="mood-grid-preview">
          <div className="mood-card">
            <span className="mood-icon">☀️</span>
            <span className="mood-label">Happy</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">🌊</span>
            <span className="mood-label">Calm</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">⚡</span>
            <span className="mood-label">Energetic</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">🌧️</span>
            <span className="mood-label">Sad</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">🌸</span>
            <span className="mood-label">Romantic</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">🔥</span>
            <span className="mood-label">Angry</span>
          </div>
          <div className="mood-card mood-card-highlight">
            <span className="mood-icon">🌀</span>
            <span className="mood-label">Anxious</span>
          </div>
          <div className="mood-card">
            <span className="mood-icon">🎯</span>
            <span className="mood-label">Focused</span>
          </div>
        </div>

        {/* Sample Playlist Preview */}
        <div className="sample-playlist">
          <h3 className="playlist-title">Feeling Happy Playlist</h3>
          <div className="playlist-preview-box">
            <div className="track-item">
              <span className="track-number">1.</span>
              <span className="track-info">Walking on Sunshine — Katrina & The Waves</span>
            </div>
            <div className="track-item">
              <span className="track-number">2.</span>
              <span className="track-info">Happy — Pharrell Williams</span>
            </div>
            <div className="track-item">
              <span className="track-number">3.</span>
              <span className="track-info">Good as Hell — Lizzo</span>
            </div>
            <p className="more-tracks">+ 15 more songs with YouTube Music links...</p>
          </div>
        </div>

        <button className="cta-primary cta-large" onClick={() => navigate('/register')}>
          Create Your Playlist Now
        </button>
      </section>
    </div>
  );
};

export default LandingPage;