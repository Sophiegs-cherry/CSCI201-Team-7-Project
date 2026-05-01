import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    displayName: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // Basic client-side validation
    const usernameRegex = /^[a-zA-Z0-9_]{3,30}$/;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;

    if (!usernameRegex.test(form.username)) {
      setError("Username must be 3-30 characters, alphanumeric or underscores.");
      return;
    }
    if (!emailRegex.test(form.email)) {
      setError("Please enter a valid email address.");
      return;
    }
    if (!passwordRegex.test(form.password)) {
      setError("Password must be at least 8 characters and include upper, lower, number, and special character.");
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        displayName: form.displayName,
      });
      navigate("/login");
    } catch (err) {
      setError(err?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    display: "block",
    width: "100%",
    margin: "0.75rem 0",
    padding: "0.75rem 1rem",
    backgroundColor: "rgba(255, 255, 255, 0.05)",
    color: "white",
    border: "1px solid rgba(255, 255, 255, 0.2)",
    borderRadius: "8px",
    fontSize: "1rem",
    transition: "all 0.3s ease",
    boxSizing: "border-box",
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
          Create Account
        </h1>
        <p style={{
          textAlign: "center",
          color: "rgba(255, 255, 255, 0.7)",
          marginBottom: "2rem",
          fontSize: "1rem",
        }}>
          Join MoodTunes today
        </p>

        {error && (
          <p style={{ color: "salmon", textAlign: "center", marginBottom: "1rem" }}>
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <input
            style={inputStyle}
            placeholder="Username"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
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
            style={inputStyle}
            placeholder="Email"
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
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
            style={inputStyle}
            type="password"
            placeholder="Password"
            value={form.password}
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

          <input
            style={inputStyle}
            type="password"
            placeholder="Confirm Password"
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
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
            style={inputStyle}
            placeholder="Display Name (optional)"
            value={form.displayName}
            onChange={(e) => setForm({ ...form, displayName: e.target.value })}
            onFocus={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.1)";
              e.target.style.borderColor = "rgba(102, 126, 234, 0.5)";
            }}
            onBlur={(e) => {
              e.target.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
              e.target.style.borderColor = "rgba(255, 255, 255, 0.2)";
            }}
          />

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
            {loading ? "Creating..." : "Create Account"}
          </button>
        </form>

        <p style={{
          textAlign: "center",
          color: "rgba(255, 255, 255, 0.7)",
          marginTop: "1.5rem",
          fontSize: "0.95rem",
        }}>
          Already have an account?{" "}
          <span
            onClick={() => navigate("/login")}
            style={{
              color: "#667eea",
              cursor: "pointer",
              fontWeight: "600",
              textDecoration: "none",
            }}
          >
            Log In
          </span>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;