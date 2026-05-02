import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import PlaylistDetailPage from './PlaylistDetailPage';
import api from '../api/axios';

// 1. Mock API and ShareModal
jest.mock('../api/axios');
jest.mock('./ShareModal', () => ({ onClose }) => (
  <div data-testid="share-modal">
    <button onClick={onClose}>Close Modal</button>
  </div>
));

const mockPlaylist = {
  playlistId: 'pl-999',
  title: 'Late Night Study',
  createdAt: '2026-05-01T12:00:00Z',
  tracks: [
    {
      trackId: 't1',
      trackOrder: 1,
      trackName: 'Lo-fi Beat',
      artistName: 'Study Girl',
      youtubeMusicUrl: 'https://music.youtube.com/1'
    }
  ],
  mood: { contextNote: 'Deep focus mode' }
};

// Helper to render with specific router state
const renderWithRouter = (initialEntries = ['/playlist/pl-999']) => {
  render(
    <MemoryRouter initialEntries={initialEntries}>
      <Routes>
        <Route path="/playlist/:id" element={<PlaylistDetailPage />} />
      </Routes>
    </MemoryRouter>
  );
};

describe('PlaylistDetailPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders loading state initially', () => {
    api.get.mockReturnValue(new Promise(() => {}));
    renderWithRouter();
    expect(screen.getByText(/Loading.../i)).toBeInTheDocument();
  });

  test('renders playlist details and tracks successfully', async () => {
    api.get.mockResolvedValueOnce({ data: mockPlaylist });
    renderWithRouter();

    expect(await screen.findByText('Late Night Study')).toBeInTheDocument();
    expect(screen.getByText(/May 1, 2026/i)).toBeInTheDocument();
    expect(screen.getByText('Lo-fi Beat')).toBeInTheDocument();
    expect(screen.getByText('Study Girl')).toBeInTheDocument();
    expect(screen.getByText(/"Deep focus mode"/i)).toBeInTheDocument();
    
    const ytLink = screen.getByRole('link', { name: /YouTube Music/i });
    expect(ytLink).toHaveAttribute('href', 'https://music.youtube.com/1');
  });

  test('shows share button for owned playlists and opens modal', async () => {
    api.get.mockResolvedValueOnce({ data: { ...mockPlaylist, shared: false } });
    renderWithRouter();

    const shareBtn = await screen.findByText(/Share with Friends/i);
    fireEvent.click(shareBtn);

    expect(screen.getByTestId('share-modal')).toBeInTheDocument();
    
    // Test closing modal
    fireEvent.click(screen.getByText('Close Modal'));
    expect(screen.queryByTestId('share-modal')).not.toBeInTheDocument();
  });

  test('displays shared message when accessed via shared link', async () => {
    const sharedPlaylist = {
      ...mockPlaylist,
      shared: true,
      sharedBy: 'MusicLover22',
      sharedMessage: 'Check this out!'
    };
    api.get.mockResolvedValueOnce({ data: sharedPlaylist });
    
    // Simulate navigating from the 'shared' page
    renderWithRouter([{ 
      pathname: '/playlist/pl-999', 
      state: { from: 'shared' } 
    }]);

    expect(await screen.findByText(/Message from MusicLover22: "Check this out!"/i)).toBeInTheDocument();
    expect(screen.getByText(/Back to Shared/i)).toBeInTheDocument();
    // Share button should be hidden for shared view
    expect(screen.queryByText(/Share with Friends/i)).not.toBeInTheDocument();
  });

  test('handles API error gracefully', async () => {
    api.get.mockRejectedValueOnce(new Error('Fetch failed'));
    renderWithRouter();

    expect(await screen.findByText(/Could not load this playlist/i)).toBeInTheDocument();
  });
});