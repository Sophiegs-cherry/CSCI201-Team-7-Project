import { Link } from "react-router-dom";

function LandingPage() {
  const container = {
    color: "white",
    textAlign: "center",
    paddingTop: "100px",
  };

  const button = {
    margin: "10px",
    padding: "10px 20px",
    backgroundColor: "#222",
    color: "white",
    border: "none",
    cursor: "pointer",
  };

  return (
    <div style={container}>
      <h1>MoodTunes</h1>
      <p style={{ color: "#aaa" }}>Your Mood. Your Music.</p>

      <h3 style={{ marginTop: "40px" }}>How it works:</h3>
      <p style={{ color: "#aaa" }}>1. Enter your mood</p>
      <p style={{ color: "#aaa" }}>2. AI generates playlist</p>
      <p style={{ color: "#aaa" }}>3. Listen instantly</p>

      <div style={{ marginTop: "30px" }}>
        <Link to="/register"><button style={button}>Sign Up</button></Link>
        <Link to="/login"><button style={button}>Log In</button></Link>
      </div>
    </div>
  );
}

export default LandingPage;