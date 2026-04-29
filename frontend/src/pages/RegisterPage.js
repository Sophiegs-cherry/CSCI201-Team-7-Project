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

    if (form.password !== form.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    await register(form);
    navigate("/login");
  };

  return (
    <form onSubmit={handleSubmit} style={{ textAlign: "center", marginTop: "100px", color: "white" }}>
      <h2>Register</h2>

      <input style={inputStyle} placeholder="Username"
        onChange={(e) => setForm({ ...form, username: e.target.value })} />

      <input style={inputStyle} placeholder="Email"
        onChange={(e) => setForm({ ...form, email: e.target.value })} />

      <input style={inputStyle} type="password" placeholder="Password"
        onChange={(e) => setForm({ ...form, password: e.target.value })} />

      <input style={inputStyle} type="password" placeholder="Confirm Password"
        onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} />

      <input style={inputStyle} placeholder="Display Name (optional)"
        onChange={(e) => setForm({ ...form, displayName: e.target.value })} />

      <button style={{ marginTop: "10px", padding: "10px 20px", background: "#222", color: "white", border: "none" }}>
        Register
      </button>
    </form>
  );
}

export default RegisterPage;