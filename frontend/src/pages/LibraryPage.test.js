import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LibraryPage from './LibraryPage';
import api from '../api/axios';

// 1. Mock the API
jest.mock('../api/axios');

const mockPlaylists = [
  {
    playlistId: 'pl-123',
    title: 'Monday Morning Blues',
    mood: 'sad',
    moodText: 'Feeling a bit down',
    songCount: 15,
    createdAt: '2026-04-25T10:00:00Z'
  },
  {
    playlistId: 'pl-456',
    title: 'Gym Pump',
    mood: 'energetic',
    moodText: 'Ready to run',
    trackCount: 20, // Testing the fallback for songCount
    createdAt: '2026-04-26T10:00:00Z'
  }
];

const renderLibrary = () => {
  render(
    <BrowserRouter>
      <LibraryPage />
    </BrowserRouter>
  );
};

describe('LibraryPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('displays loading state initially', () => {
    // Make the promise hang indefinitely for this test
    api.get.mockReturnValue(new Promise(() => {}));
    renderLibrary();
    expect(screen.getByText(/Loading.../i)).toBeInTheDocument();
  });

  test('renders playlists from the API', async () => {
    api.get.mockResolvedValueOnce({ data: mockPlaylists });
    renderLibrary();

    // Verify titles appear
    expect(await screen.findByText('Monday Morning Blues')).toBeInTheDocument();
    expect(screen.getByText('Gym Pump')).toBeInTheDocument();

    // Verify metadata (Date formatting and song counts)
    expect(screen.getByText(/April 25, 2026/i)).toBeInTheDocument();
    expect(screen.getByText(/15 songs/i)).toBeInTheDocument();
    expect(screen.getByText(/20 songs/i)).toBeInTheDocument(); // checks trackCount fallback
  });

  test('shows empty state message when no playlists exist', async () => {
    api.get.mockResolvedValueOnce({ data: [] });
    renderLibrary();

    await waitFor(() => {
      expect(screen.getByText(/No saved playlists yet/i)).toBeInTheDocument();
    });
  });

  test('displays error message on API failure', async () => {
    api.get.mockRejectedValueOnce(new Error('Network Error'));
    renderLibrary();

    await waitFor(() => {
      expect(screen.getByText(/Could not load your library/i)).toBeInTheDocument();
    });
  });

  test('playlist cards have correct links', async () => {
    api.get.mockResolvedValueOnce({ data: [mockPlaylists[0]] });
    renderLibrary();

    const link = await screen.findByRole('link', { name: /View playlist/i });
    expect(link).toHaveAttribute('href', '/playlist/pl-123');
  });
});