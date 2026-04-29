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

  const inputStyle = {
    display: "block",
    margin: "10px auto",
    padding: "10px",
    width: "250px",
    backgroundColor: "#111",
    color: "white",
    border: "1px solid #333",
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await login(form, remember);
    navigate("/dashboard");
  };

  return (
    <form onSubmit={handleSubmit} style={{ textAlign: "center", marginTop: "100px", color: "white" }}>
      <h2>Login</h2>

      <input style={inputStyle} placeholder="Username or Email"
        onChange={(e) => setForm({ ...form, usernameOrEmail: e.target.value })} />

      <input style={inputStyle} type="password" placeholder="Password"
        onChange={(e) => setForm({ ...form, password: e.target.value })} />

      <label style={{ color: "#aaa" }}>
        <input type="checkbox" onChange={() => setRemember(!remember)} />
        Remember Me
      </label>

      <br />

      <button style={{ marginTop: "10px", padding: "10px 20px", background: "#222", color: "white", border: "none" }}>
        Login
      </button>
    </form>
  );
}

export default LoginPage;