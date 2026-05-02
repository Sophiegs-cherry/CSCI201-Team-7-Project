import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import FriendsPage from './FriendsPage';
import api from '../api/axios';

// 1. Mock the API
jest.mock('../api/axios');

const mockFriendsData = [
  { friendshipId: 101, user: { username: 'jdoe', displayName: 'John Doe' } }
];

const mockRequestsData = [
  { friendshipId: 202, user: { username: 'suzy_q', displayName: 'Suzy Q' } }
];

describe('FriendsPage Unit Tests', () => {
  beforeEach(() => {
    // Mock the initial three calls in the useEffect
    api.get.mockImplementation((url) => {
      if (url === '/api/friends') return Promise.resolve({ data: mockFriendsData });
      if (url === '/api/friends/requests') return Promise.resolve({ data: mockRequestsData });
      if (url === '/api/friends/requests/sent') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('not found'));
    });
  });

  test('loads and displays initial friends list', async () => {
    render(<FriendsPage />);
    
    // Check that the tab label shows the correct count
    const friendTab = await screen.findByText(/My Friends \(1\)/i);
    expect(friendTab).toBeInTheDocument();

    // Check that the specific username is rendered in the grid
    expect(screen.getByText('jdoe')).toBeInTheDocument();
  });

  test('switches tabs and displays received requests', async () => {
    render(<FriendsPage />);

    // Click the Received tab
    const receivedTab = await screen.findByText(/Received \(1\)/i);
    fireEvent.click(receivedTab);

    // Verify the user in the received request is visible
    expect(screen.getByText('suzy_q')).toBeInTheDocument();
    expect(screen.getByText('Accept')).toBeInTheDocument();
  });

  test('performs user search successfully', async () => {
  const searchResult = [{ userId: 5, username: 'tester', displayName: 'Test User' }];
  
  render(<FriendsPage />);

  // Wait for initial load to complete first
  await screen.findByText(/My Friends/i);

  // NOW set up the search mock
  api.get.mockResolvedValueOnce({ data: searchResult });

  const searchInput = screen.getByPlaceholderText(/Search users/i);
  const searchBtn = screen.getByText('Search');

  fireEvent.change(searchInput, { target: { value: 'tester' } });
  fireEvent.click(searchBtn);

  await waitFor(() => {
    expect(api.get).toHaveBeenCalledWith('/api/friends/search?username=tester');
    expect(screen.getByText('Test User')).toBeInTheDocument();
  });
});

  test('calls remove friend API when button is clicked', async () => {
    api.delete.mockResolvedValueOnce({});
    render(<FriendsPage />);

    const removeBtn = await screen.findByText('Remove');
    fireEvent.click(removeBtn);

    expect(api.delete).toHaveBeenCalledWith('/api/friends/101');
  });
});