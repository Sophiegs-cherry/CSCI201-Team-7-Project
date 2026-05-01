import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar() {
  const { isAuthenticated, logout } = useAuth();
  const location = useLocation();

  if (location.pathname === "/" && !isAuthenticated) return null;

  const navStyle = {
    display: "flex",
    justifyContent: "space-between",
    padding: "1rem 2rem",
    backgroundColor: "#0f1427",
    borderBottom: "1px solid #1a1f3a",
  };

  const linkStyle = {
    color: "#aaa",
    marginRight: "1rem",
    textDecoration: "none",
    transition: "color 0.2s ease",
  };

  return (
    <nav style={navStyle}>
      <Link to="/" style={{ color: "white", textDecoration: "none" }}>MoodTunes</Link>

      <div>
        {isAuthenticated ? (
          <>
            <Link style={linkStyle} to="/dashboard">Dashboard</Link>
            <Link style={linkStyle} to="/library">Library</Link>
            <Link style={linkStyle} to="/friends">Friends</Link>
            <Link style={linkStyle} to="/shared">Shared</Link>
            <button
              onClick={logout}
              style={{ background: "#1a1f3a", color: "white", border: "1px solid #333", padding: "0.5rem 1rem", borderRadius: "6px", cursor: "pointer" }}
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link style={linkStyle} to="/login">Login</Link>
            <Link style={linkStyle} to="/register">Sign Up</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
