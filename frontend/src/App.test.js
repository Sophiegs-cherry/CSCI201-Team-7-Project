import { render, screen } from '@testing-library/react';
import App from './App';

jest.mock('./context/AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: false,
    logout: jest.fn(),
  }),
}));

test('renders the landing page for unauthenticated users', () => {
  render(<App />);
  const linkElement = screen.getByText(/Your Mood. Your Music./i);
  expect(linkElement).toBeInTheDocument();
});
