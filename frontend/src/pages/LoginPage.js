import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    usernameOrEmail: "",
    password: "",
  });

  const [remember, setRemember] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.usernameOrEmail.trim() || !form.password) {
      setError("Please enter username/email and password.");
      return;
    }

    setLoading(true);
    try {
      await login(form, remember);
      navigate("/dashboard");
    } catch (err) {
      setError(err?.message || "Failed to login. Please check credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: "100vh",
      background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "2rem",
    }}>
      <div style={{
        width: "100%",
        maxWidth: "450px",
        padding: "3rem",
        background: "rgba(26, 26, 46, 0.6)",
        backdropFilter: "blur(10px)",
        borderRadius: "16px",
        border: "1px solid rgba(255, 255, 255, 0.1)",
      }}>
        <h1 style={{
          fontSize: "2.5rem",
          fontWeight: "700",
          textAlign: "center",
          marginBottom: "0.5rem",
          background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          backgroundClip: "text",
        }}>
          Login
        </h1>
        <p style={{
          textAlign: "center",
          color: "rgba(255, 255, 255, 0.7)",
          marginBottom: "2rem",
          fontSize: "1rem",
        }}>
          Welcome back to MoodTunes
        </p>

        {error && (
          <p style={{ color: "salmon", textAlign: "center", marginBottom: "1rem" }}>
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <input
            style={{
              display: "block",
              width: "100%",
              margin: "1rem 0",
              padding: "0.75rem 1rem",
              backgroundColor: "rgba(255, 255, 255, 0.05)",
              color: "white",
              border: "1px solid rgba(255, 255, 255, 0.2)",
              borderRadius: "8px",
              fontSize: "1rem",
              transition: "all 0.3s ease",
              boxSizing: "border-box",
            }}
            placeholder="Username or Email"
            onChange={(e) => setForm({ ...form, usernameOrEmail: e.target.value })}
            onFocus={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.1)";
              e.target.style.borderColor = "rgba(102, 126, 234, 0.5)";
            }}
            onBlur={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
              e.target.style.borderColor = "rgba(255, 255, 255, 0.2)";
            }}
          />

          <input
            style={{
              display: "block",
              width: "100%",
              margin: "1rem 0",
              padding: "0.75rem 1rem",
              backgroundColor: "rgba(255, 255, 255, 0.05)",
              color: "white",
              border: "1px solid rgba(255, 255, 255, 0.2)",
              borderRadius: "8px",
              fontSize: "1rem",
              transition: "all 0.3s ease",
              boxSizing: "border-box",
            }}
            type="password"
            placeholder="Password"
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            onFocus={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.1)";
              e.target.style.borderColor = "rgba(102, 126, 234, 0.5)";
            }}
            onBlur={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
              e.target.style.borderColor = "rgba(255, 255, 255, 0.2)";
            }}
          />

          <label style={{
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            color: "rgba(255, 255, 255, 0.7)",
            margin: "1.5rem 0",
            cursor: "pointer",
            fontSize: "0.95rem",
          }}>
            <input
              type="checkbox"
              checked={remember}
              onChange={() => setRemember(!remember)}
              style={{
                cursor: "pointer",
                width: "18px",
                height: "18px",
              }}
            />
            Remember Me
          </label>

          <button
            type="submit"
            disabled={loading}
            style={{
              width: "100%",
              marginTop: "1.5rem",
              padding: "0.85rem",
              fontSize: "1.1rem",
              fontWeight: "600",
              background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              color: "white",
              border: "none",
              borderRadius: "8px",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
              transition: "all 0.3s ease",
            }}
            onMouseEnter={(e) => {
              if (!loading) {
                e.target.style.transform = "translateY(-2px)";
                e.target.style.boxShadow = "0 8px 20px rgba(102, 126, 234, 0.4)";
              }
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = "translateY(0)";
              e.target.style.boxShadow = "none";
            }}
          >
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <p style={{
          textAlign: "center",
          color: "rgba(255, 255, 255, 0.7)",
          marginTop: "1.5rem",
          fontSize: "0.95rem",
        }}>
          Don't have an account?{" "}
          <span
            onClick={() => navigate("/register")}
            style={{
              color: "#667eea",
              cursor: "pointer",
              fontWeight: "600",
              textDecoration: "none",
            }}
          >
            Sign Up
          </span>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;