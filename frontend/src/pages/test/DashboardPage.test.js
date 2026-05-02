import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import DashboardPage from '../DashboardPage';
import api from '../../api/axios';

// 1. Mock the API and Navigate
jest.mock('../../api/axios');
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedNavigate,
}));

const renderDashboard = () => {
  render(
    <BrowserRouter>
      <DashboardPage />
    </BrowserRouter>
  );
};

describe('DashboardPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders all mood options', () => {
    renderDashboard();
    expect(screen.getByText(/Happy/i)).toBeInTheDocument();
    expect(screen.getByText(/Focused/i)).toBeInTheDocument();
  });

  test('prevents picking more than 3 genres', () => {
    renderDashboard();
    const genres = ['Pop', 'Rock', 'Hip-Hop', 'Electronic'];
    
    // Click first 3
    fireEvent.click(screen.getByText('Pop'));
    fireEvent.click(screen.getByText('Rock'));
    fireEvent.click(screen.getByText('Hip-Hop'));

    // Check if 4th one is disabled (opacity 0.3 per your code)
    const electronicBtn = screen.getByText('Electronic');
    expect(electronicBtn).toHaveStyle('opacity: 0.3');
  });


  test('successfully calls API and navigates on valid submission', async () => {
    const mockPlaylist = { id: 1, tracks: [] };
    api.post.mockResolvedValueOnce({ data: mockPlaylist });

    renderDashboard();

    // 1. Select Mood
    fireEvent.click(screen.getByText('Happy'));
    
    // 2. Select Genre
    fireEvent.click(screen.getByText('Jazz'));

    // 3. Enter Context
    const textarea = screen.getByPlaceholderText(/Write about why/i);
    fireEvent.change(textarea, { target: { value: 'Working on my CS project' } });

    // 4. Submit
    fireEvent.click(screen.getByText(/Generate Playlist/i));

    await waitFor(() => {
      // Check if API was called with correct structure
      expect(api.post).toHaveBeenCalledWith('/api/playlists/generate', {
        mood: 'happy',
        musicPreferences: 'Jazz',
        context: 'Working on my CS project',
      });
      
      // Check if we navigated to the playlist page
      expect(mockedNavigate).toHaveBeenCalledWith('/playlist', expect.any(Object));
    });
  });
});