import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ShareModal from '../ShareModal';
import api from '../../api/axios';

// 1. Mock the API
jest.mock('../../api/axios');

const mockFriends = [
  { userId: 'u1', username: 'alex_music', displayName: 'Alex Smith' },
  { userId: 'u2', username: 'jamie_beats', displayName: 'Jamie Doe' }
];

const defaultProps = {
  playlistId: 'pl-123',
  playlistTitle: 'Summer Vibes',
  onClose: jest.fn()
};

describe('ShareModal Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('fetches and displays friends list on mount', async () => {
    api.get.mockResolvedValueOnce({ data: mockFriends });
    render(<ShareModal {...defaultProps} />);

    expect(await screen.findByText('alex_music')).toBeInTheDocument();
    expect(screen.getByText('Jamie Doe')).toBeInTheDocument();
    // Check initials logic (Alex Smith -> AS)
    expect(screen.getByText('AS')).toBeInTheDocument();
    expect(screen.getByText('JD')).toBeInTheDocument();
  });

  test('toggles friend selection when clicked', async () => {
    api.get.mockResolvedValueOnce({ data: mockFriends });
    render(<ShareModal {...defaultProps} />);

    const friendRow = await screen.findByText('alex_music');
    
    // Select friend
    fireEvent.click(friendRow);
    const checkbox = screen.getAllByText('✓')[0]; 
    expect(checkbox).toBeInTheDocument();

    // Deselect friend
    fireEvent.click(friendRow);
    expect(screen.queryByText('✓')).not.toBeInTheDocument();
  });

  test('handles successful share submission', async () => {
    api.get.mockResolvedValueOnce({ data: mockFriends });
    api.post.mockResolvedValueOnce({ data: { success: true } });
    
    render(<ShareModal {...defaultProps} />);

    const friendRow = await screen.findByText('alex_music');
    fireEvent.click(friendRow);

    const messageInput = screen.getByPlaceholderText(/really helped me today/i);
    fireEvent.change(messageInput, { target: { value: 'Check this out!' } });

    const sendBtn = screen.getByRole('button', { name: /Send/i });
    fireEvent.click(sendBtn);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/playlists/share', {
        playlistId: 'pl-123',
        recipientIds: ['u1'],
        message: 'Check this out!'
      });
      expect(screen.getByText(/Playlist Shared!/i)).toBeInTheDocument();
    });

    // Close modal after success
    fireEvent.click(screen.getByRole('button', { name: /Done/i }));
    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  test('displays error message if friend fetch fails', async () => {
    api.get.mockRejectedValueOnce(new Error('Failed'));
    render(<ShareModal {...defaultProps} />);

    expect(await screen.findByText(/Could not load friends/i)).toBeInTheDocument();
  });

  test('closes modal when clicking overlay but not the card', () => {
    api.get.mockResolvedValueOnce({ data: [] });
    const { container } = render(<ShareModal {...defaultProps} />);

    const overlay = container.querySelector('.modal-overlay');
    const card = container.querySelector('.modal-card');

    // Click card (should NOT close)
    fireEvent.click(card);
    expect(defaultProps.onClose).not.toHaveBeenCalled();

    // Click overlay (should close)
    fireEvent.click(overlay);
    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });
});