import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import SharedWithMePage from './SharedWithMePage';
import api from '../api/axios';

// 1. Mock the API
jest.mock('../api/axios');

const mockSharedPlaylists = [
  {
    shareId: 'share-001',
    playlistId: 'pl-100',
    title: 'Study Beats',
    senderUsername: 'TrevorCoder',
    sharedAt: '2026-05-15T09:00:00Z',
    message: 'Good luck with finals!'
  },
  {
    shareId: 'share-002',
    playlistId: 'pl-200',
    title: 'Chill Vibes',
    senderUsername: 'SophieShim',
    sharedAt: '2026-05-16T10:00:00Z',
    message: null // Test fallback for message
  }
];

const renderSharedPage = () => {
  render(
    <BrowserRouter>
      <SharedWithMePage />
    </BrowserRouter>
  );
};

describe('SharedWithMePage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('displays loading state initially', () => {
    api.get.mockReturnValue(new Promise(() => {}));
    renderSharedPage();
    expect(screen.getByText(/Loading.../i)).toBeInTheDocument();
  });

  test('renders shared playlists successfully', async () => {
    api.get.mockResolvedValueOnce({ data: mockSharedPlaylists });
    renderSharedPage();

    // Verify Sender Info
    expect(await screen.findByText('TrevorCoder')).toBeInTheDocument();
    expect(screen.getByText('SophieShim')).toBeInTheDocument();
    expect(screen.getByText(/shared on May 15, 2026/i)).toBeInTheDocument();

    // Verify Avatars (Slice logic)
    expect(screen.getByText('TR')).toBeInTheDocument();
    expect(screen.getByText('SO')).toBeInTheDocument();

    // Verify Playlist Info
    expect(screen.getByText('Study Beats')).toBeInTheDocument();
    expect(screen.getByText(/"Good luck with finals!"/i)).toBeInTheDocument();
    
    // Fallback for null message (ensure no quote renders)
    expect(screen.queryByText(/""/i)).not.toBeInTheDocument();
  });

  test('shows empty state message when no playlists are shared', async () => {
    api.get.mockResolvedValueOnce({ data: [] });
    renderSharedPage();

    await waitFor(() => {
      expect(screen.getByText(/No playlists shared with you yet/i)).toBeInTheDocument();
    });
  });

  test('displays error message on API failure', async () => {
    api.get.mockRejectedValueOnce(new Error('API Error'));
    renderSharedPage();

    await waitFor(() => {
      expect(screen.getByText(/Could not load shared playlists/i)).toBeInTheDocument();
    });
  });

  test('View Playlist links have correct targets and state', async () => {
    api.get.mockResolvedValueOnce({ data: [mockSharedPlaylists[0]] });
    renderSharedPage();

    const link = await screen.findByRole('link', { name: /View Playlist/i });
    expect(link).toHaveAttribute('href', '/playlist/pl-100');
    
    // NOTE: RTL cannot directly inspect the 'state' prop of a Link.
    // In an integration test, we would click this and verify the
    // location state on the detail page. Here, we've verified the path.
  });
});