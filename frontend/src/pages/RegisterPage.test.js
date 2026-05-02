import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import RegisterPage from './RegisterPage';

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

const mockRegister = jest.fn();
jest.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    register: mockRegister,
  }),
}));

const renderRegister = () => render(<RegisterPage />);

describe('RegisterPage Unit Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows error for invalid username', async () => {
    renderRegister();
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'ab' } });
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'Password1!' } });
    fireEvent.change(screen.getByPlaceholderText('Confirm Password'), { target: { value: 'Password1!' } });
    fireEvent.submit(screen.getByPlaceholderText('Username').closest('form'));
    expect(await screen.findByText(/Username must be 3-30 characters/i)).toBeInTheDocument();
  });

  test('shows error when passwords do not match', async () => {
    renderRegister();
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'validuser' } });
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'Password1!' } });
    fireEvent.change(screen.getByPlaceholderText('Confirm Password'), { target: { value: 'Different1!' } });
    fireEvent.submit(screen.getByPlaceholderText('Username').closest('form'));
    expect(await screen.findByText(/Passwords do not match/i)).toBeInTheDocument();
  });

  test('shows error for invalid email', async () => {
    renderRegister();
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'validuser' } });
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'notanemail' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'Password1!' } });
    fireEvent.change(screen.getByPlaceholderText('Confirm Password'), { target: { value: 'Password1!' } });
    fireEvent.submit(screen.getByPlaceholderText('Username').closest('form'));
    expect(await screen.findByText(/valid email address/i)).toBeInTheDocument();
  });

  test('calls register and navigates on valid submission', async () => {
    mockRegister.mockResolvedValueOnce({});
    renderRegister();
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'validuser' } });
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'Password1!' } });
    fireEvent.change(screen.getByPlaceholderText('Confirm Password'), { target: { value: 'Password1!' } });
    fireEvent.submit(screen.getByPlaceholderText('Username').closest('form'));
    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        username: 'validuser',
        email: 'test@test.com',
        password: 'Password1!',
        displayName: '',
      });
    });
  });
});