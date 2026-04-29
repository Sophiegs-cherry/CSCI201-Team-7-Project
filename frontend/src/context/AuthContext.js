import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axios";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null);

  useEffect(() => {
    const stored =
      localStorage.getItem("token") || sessionStorage.getItem("token");
    if (stored) setToken(stored);
  }, []);

  const login = async (data, remember) => {
    const res = await api.post("/api/auth/login", data);
    const jwt = res.data.token;

    if (remember) {
      localStorage.setItem("token", jwt);
    } else {
      sessionStorage.setItem("token", jwt);
    }

    setToken(jwt);
  };

  const register = async (data) => {
    await api.post("/api/auth/register", data);
  };

  const logout = () => {
    localStorage.removeItem("token");
    sessionStorage.removeItem("token");
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        isAuthenticated: !!token,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);