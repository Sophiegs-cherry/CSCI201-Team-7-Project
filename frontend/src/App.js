import { BrowserRouter, Routes, Route } from "react-router-dom";
import DashboardPage from "./pages/DashboardPage";
import PlaylistPage from "./pages/PlaylistPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/playlist" element={<PlaylistPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;