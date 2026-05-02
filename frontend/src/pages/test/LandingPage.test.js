import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LandingPage from '../LandingPage';

// 1. Mock useNavigate from react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedNavigate,
}));

const renderLandingPage = () => {
  render(
    <BrowserRouter>
      <LandingPage />
    </BrowserRouter>
  );
};

describe('LandingPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders main hero title and subtitle', () => {
    renderLandingPage();
    expect(screen.getByText(/Your Mood. Your Music./i)).toBeInTheDocument();
    expect(screen.getByText(/personalized playlist powered by AI/i)).toBeInTheDocument();
  });

  test('renders the "How It Works" steps', () => {
    renderLandingPage();
    expect(screen.getByText(/Share Your Mood/i)).toBeInTheDocument();
    expect(screen.getByText(/AI Generates Songs/i)).toBeInTheDocument();
    expect(screen.getByText(/Listen on YouTube/i)).toBeInTheDocument();
  });

  test('navigates to login when Login button is clicked', () => {
    renderLandingPage();
    // There are multiple Login buttons (nav and hero), we can pick the first one
    const loginBtns = screen.getAllByRole('button', { name: /Log In/i });
    fireEvent.click(loginBtns[0]);
    expect(mockedNavigate).toHaveBeenCalledWith('/login');
  });

  test('navigates to register when Sign Up button is clicked', () => {
    renderLandingPage();
    const signUpBtns = screen.getAllByRole('button', { name: /Sign Up/i });
    fireEvent.click(signUpBtns[0]);
    expect(mockedNavigate).toHaveBeenCalledWith('/register');
  });

  test('renders the interactive mood preview icons', () => {
    renderLandingPage();
    expect(screen.getByText('Happy')).toBeInTheDocument();
    expect(screen.getByText('Romantic')).toBeInTheDocument();
    expect(screen.getByText('Focused')).toBeInTheDocument();
  });
});