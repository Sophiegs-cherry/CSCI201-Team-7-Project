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
    backgroundColor: "#111",
    borderBottom: "1px solid #222",
  };

  const linkStyle = {
    color: "#ccc",
    marginRight: "1rem",
    textDecoration: "none",
  };

  return (
    <nav style={navStyle}>
      <div style={{ color: "white" }}>MoodTunes</div>

      <div>
        {isAuthenticated ? (
          <>
            <Link style={linkStyle} to="/dashboard">Dashboard</Link>
            <button
              onClick={logout}
              style={{ background: "#222", color: "white", border: "none", padding: "0.5rem 1rem" }}
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