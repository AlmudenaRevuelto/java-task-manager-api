/**
 * RegisterModal.test.tsx
 *
 * Tests for the RegisterModal component.
 *
 * Mocked modules:
 *   - ../../api/authApi   → avoids real HTTP calls
 *   - react-hot-toast     → avoids DOM attachment noise in jsdom
 *   - ../Modal            → renders children directly, removing backdrop complexity
 */
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import RegisterModal from './RegisterModal';
import * as authApi from '../../api/authApi';

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

vi.mock('../../api/authApi', () => ({
  register: vi.fn(),
}));

vi.mock('react-hot-toast', () => ({
  default: { success: vi.fn(), error: vi.fn() },
}));

vi.mock('../Modal', () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function renderModal(onClose = vi.fn(), onRegistered = vi.fn()) {
  render(<RegisterModal onClose={onClose} onRegistered={onRegistered} />);
  return { onClose, onRegistered };
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('RegisterModal', () => {
  beforeEach(() => vi.clearAllMocks());

  // -------------------------------------------------------------------------
  // Rendering
  // -------------------------------------------------------------------------

  it('renders the form heading', () => {
    renderModal();
    expect(screen.getByRole('heading', { name: /crear cuenta/i })).toBeInTheDocument();
  });

  it('renders username, password, and confirm-password fields', () => {
    renderModal();
    expect(screen.getByPlaceholderText(/mínimo 3 caracteres/i)).toBeInTheDocument();
    const passwordFields = document.querySelectorAll('input[type="password"]');
    expect(passwordFields).toHaveLength(2);
  });

  // -------------------------------------------------------------------------
  // Client-side validation
  // -------------------------------------------------------------------------

  it('shows a toast error when username is too short (< 3 chars)', async () => {
    const toast = (await import('react-hot-toast')).default;
    renderModal();

    // Only the username is too short; password fields are filled so that
    // HTML5 `required` validation does not block the form submission.
    await userEvent.type(screen.getByPlaceholderText(/mínimo 3 caracteres/i), 'ab');
    const fields = document.querySelectorAll('input[type="password"]');
    await userEvent.type(fields[0], 'password123');
    await userEvent.type(fields[1], 'password123');
    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }));

    expect(authApi.register).not.toHaveBeenCalled();
    expect(toast.error).toHaveBeenCalledWith(
      'El nombre de usuario debe tener al menos 3 caracteres'
    );
  });

  it('shows a toast error when password is too short (< 6 chars)', async () => {
    const toast = (await import('react-hot-toast')).default;
    renderModal();

    await userEvent.type(screen.getByPlaceholderText(/mínimo 3 caracteres/i), 'validname');
    // Fill both password fields so `required` does not block; the short
    // password triggers the custom validation before the mismatch check.
    const fields = document.querySelectorAll('input[type="password"]');
    await userEvent.type(fields[0], '123');
    await userEvent.type(fields[1], '123');
    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }));

    expect(authApi.register).not.toHaveBeenCalled();
    expect(toast.error).toHaveBeenCalledWith('La contraseña debe tener al menos 6 caracteres');
  });

  it('shows a toast error when passwords do not match', async () => {
    const toast = (await import('react-hot-toast')).default;
    renderModal();

    await userEvent.type(screen.getByPlaceholderText(/mínimo 3 caracteres/i), 'validname');
    const fields = document.querySelectorAll('input[type="password"]');
    await userEvent.type(fields[0], 'password123');
    await userEvent.type(fields[1], 'differentpass');
    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }));

    expect(authApi.register).not.toHaveBeenCalled();
    expect(toast.error).toHaveBeenCalledWith('Las contraseñas no coinciden');
  });

  // -------------------------------------------------------------------------
  // Successful submission
  // -------------------------------------------------------------------------

  it('calls register with trimmed username and calls onRegistered on success', async () => {
    const authData = { token: 'jwt-token', username: 'newuser', role: 'USER' };
    vi.mocked(authApi.register).mockResolvedValueOnce(authData);

    const { onRegistered } = renderModal();

    await userEvent.type(screen.getByPlaceholderText(/mínimo 3 caracteres/i), '  newuser  ');
    const fields = document.querySelectorAll('input[type="password"]');
    await userEvent.type(fields[0], 'password123');
    await userEvent.type(fields[1], 'password123');
    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }));

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith('newuser', 'password123');
      expect(onRegistered).toHaveBeenCalledWith(authData);
    });
  });

  // -------------------------------------------------------------------------
  // API failure
  // -------------------------------------------------------------------------

  it('shows a toast error and does NOT call onRegistered when register fails', async () => {
    const toast = (await import('react-hot-toast')).default;
    vi.mocked(authApi.register).mockRejectedValueOnce(new Error('Username taken'));

    const { onRegistered } = renderModal();

    await userEvent.type(screen.getByPlaceholderText(/mínimo 3 caracteres/i), 'takenuser');
    const fields = document.querySelectorAll('input[type="password"]');
    await userEvent.type(fields[0], 'password123');
    await userEvent.type(fields[1], 'password123');
    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }));

    await waitFor(() => {
      expect(onRegistered).not.toHaveBeenCalled();
      expect(toast.error).toHaveBeenCalledWith(
        'No se pudo crear la cuenta. El nombre de usuario puede estar en uso.'
      );
    });
  });

  // -------------------------------------------------------------------------
  // Cancel
  // -------------------------------------------------------------------------

  it('calls onClose when the cancel button is clicked', async () => {
    const { onClose } = renderModal();
    await userEvent.click(screen.getByRole('button', { name: /cancelar/i }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
