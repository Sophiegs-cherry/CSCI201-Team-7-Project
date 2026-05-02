import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import PlaylistPage from '../PlaylistPage';
import api from '../../api/axios';

// 1. Mock the API
jest.mock('../../api/axios');

const mockGeneratedPlaylist = {
  title: 'Rainy Day Jazz',
  tracks: [
    {
      trackName: 'Blue in Green',
      artistName: 'Miles Davis',
      youtubeMusicUrl: 'https://music.youtube.com/miles',
      trackOrder: 1
    }
  ]
};

const mockLocationState = {
  playlist: mockGeneratedPlaylist,
  moodLabel: 'Sad',
  moodId: 'mood-123',
  contextNote: 'Watching the rain'
};

const renderWithState = (state = mockLocationState) => {
  render(
    <MemoryRouter initialEntries={[{ pathname: '/playlist', state }]}>
      <Routes>
        <Route path="/playlist" element={<PlaylistPage />} />
        <Route path="/dashboard" element={<div>Dashboard Page</div>} />
        <Route path="/library" element={<div>Library Page</div>} />
      </Routes>
    </MemoryRouter>
  );
};

describe('PlaylistPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders "No playlist found" if state is missing', () => {
    renderWithState(null);
    expect(screen.getByText(/No playlist found/i)).toBeInTheDocument();
  });

  test('displays playlist info from location state', () => {
    renderWithState();
    
    expect(screen.getByText('Rainy Day Jazz')).toBeInTheDocument();
    expect(screen.getByText(/SAD/i)).toBeInTheDocument(); // Mood tag
    expect(screen.getByText(/"Watching the rain"/i)).toBeInTheDocument();
    expect(screen.getByText('Blue in Green')).toBeInTheDocument();
    expect(screen.getByText('Miles Davis')).toBeInTheDocument();
  });

  test('successfully saves playlist to library', async () => {
    api.post.mockResolvedValueOnce({ data: { success: true } });
    renderWithState();

    const saveBtn = screen.getByText(/Save to Library/i);
    fireEvent.click(saveBtn);

    expect(saveBtn).toHaveTextContent(/Saving.../i);

    await waitFor(() => {
      // Verify API call structure matches handleSave logic
      expect(api.post).toHaveBeenCalledWith('/api/playlists/save', expect.objectContaining({
        title: 'Rainy Day Jazz',
        moodId: 'mood-123',
        context: 'Watching the rain'
      }));
      
      // Check for success state UI
      expect(screen.getByText(/✓ Saved to Library/i)).toBeInTheDocument();
      expect(screen.getByText(/Playlist saved!/i)).toBeInTheDocument();
    });
  });

  test('handles save error from API', async () => {
    const errorMsg = "Database connection failed";
    api.post.mockRejectedValueOnce({
      response: { data: { error: errorMsg } }
    });
    
    renderWithState();
    fireEvent.click(screen.getByText(/Save to Library/i));

    await waitFor(() => {
      expect(screen.getByText(`⚠️ ${errorMsg}`)).toBeInTheDocument();
    });
  });

  test('renders fallback emoji/color if mood is unknown', () => {
    const stateWithUnknownMood = {
      ...mockLocationState,
      moodLabel: 'UnknownMood'
    };
    renderWithState(stateWithUnknownMood);
    
    // "Calm" is the fallback mood in your code
    expect(screen.getByText(/🌊/i)).toBeInTheDocument();
  });

  test('navigates to library when success banner link is clicked', async () => {
    api.post.mockResolvedValueOnce({ data: {} });
    renderWithState();

    fireEvent.click(screen.getByText(/Save to Library/i));
    
    const libLink = await screen.findByText(/Library →/i);
    fireEvent.click(libLink);

    expect(screen.getByText('Library Page')).toBeInTheDocument();
  });
});