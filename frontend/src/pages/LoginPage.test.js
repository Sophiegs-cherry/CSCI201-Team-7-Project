import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from './LoginPage';
import { useAuth } from '../context/AuthContext';

// 1. Mock the hooks
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedNavigate,
}));

jest.mock('../context/AuthContext', () => ({
  useAuth: jest.fn(),
}));

const mockLogin = jest.fn();

const renderLoginPage = () => {
  render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  );
};

describe('LoginPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuth.mockReturnValue({ login: mockLogin });
  });

  test('renders login form elements', () => {
    renderLoginPage();
    expect(screen.getByPlaceholderText(/Username or Email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Remember Me/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Login/i })).toBeInTheDocument();
  });

  test('shows error if fields are empty on submit', async () => {
    renderLoginPage();
    fireEvent.click(screen.getByRole('button', { name: /Login/i }));
    
    expect(screen.getByText(/Please enter username\/email and password/i)).toBeInTheDocument();
    expect(mockLogin).not.toHaveBeenCalled();
  });

  test('successfully calls login and navigates to dashboard', async () => {
    mockLogin.mockResolvedValueOnce(); // Simulate successful login
    renderLoginPage();

    // Fill out form
    fireEvent.change(screen.getByPlaceholderText(/Username or Email/i), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Password/i), {
      target: { value: 'password123' },
    });
    
    // Toggle Remember Me
    fireEvent.click(screen.getByLabelText(/Remember Me/i));

    fireEvent.click(screen.getByRole('button', { name: /Login/i }));

    await waitFor(() => {
      // Check if login was called with form data and remember=true
      expect(mockLogin).toHaveBeenCalledWith(
        { usernameOrEmail: 'testuser', password: 'password123' },
        true
      );
      expect(mockedNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  test('displays API error message on login failure', async () => {
    const errorMessage = "Invalid credentials";
    mockLogin.mockRejectedValueOnce({ message: errorMessage });
    
    renderLoginPage();

    fireEvent.change(screen.getByPlaceholderText(/Username or Email/i), {
      target: { value: 'wronguser' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Password/i), {
      target: { value: 'wrongpass' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Login/i }));

    expect(await screen.findByText(errorMessage)).toBeInTheDocument();
    expect(mockedNavigate).not.toHaveBeenCalled();
  });

  test('navigates to register page when link is clicked', () => {
    renderLoginPage();
    const signUpLink = screen.getByText(/Sign Up/i);
    fireEvent.click(signUpLink);
    expect(mockedNavigate).toHaveBeenCalledWith('/register');
  });
});