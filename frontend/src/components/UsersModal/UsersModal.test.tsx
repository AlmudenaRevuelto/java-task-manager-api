/**
 * UsersModal.test.tsx
 *
 * Tests for the UsersModal component.
 *
 * Mocked modules:
 *   - ../../api/adminApi  → avoids real HTTP calls; lets us control resolved values
 *   - react-hot-toast     → avoids DOM attachment noise in jsdom
 *   - ../Modal            → renders children directly, removing backdrop complexity
 *
 * Concepts:
 *   - vi.mock()           replaces a real module with a test double
 *   - waitFor()           waits for async state updates to settle
 *   - window.confirm spy  intercepts confirmation dialogs
 */
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import UsersModal from './UsersModal';
import * as adminApi from '../../api/adminApi';

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

vi.mock('../../api/adminApi', () => ({
  getUsers: vi.fn(),
  updateUserRole: vi.fn(),
  deleteUser: vi.fn(),
}));

vi.mock('react-hot-toast', () => ({
  default: { success: vi.fn(), error: vi.fn() },
}));

// Render Modal children directly so backdrop clicks don't interfere
vi.mock('../Modal', () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// ---------------------------------------------------------------------------
// Test data
// ---------------------------------------------------------------------------

const ADMIN_USER: adminApi.UserEntry = { id: 1, username: 'adminuser', role: 'ADMIN' };
const OTHER_USER: adminApi.UserEntry = { id: 2, username: 'otheruser', role: 'USER' };

// ---------------------------------------------------------------------------
// Helper
// ---------------------------------------------------------------------------

function renderModal(currentUsername = 'adminuser') {
  render(<UsersModal onClose={vi.fn()} currentUsername={currentUsername} />);
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('UsersModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default: getUsers resolves immediately with two users
    vi.mocked(adminApi.getUsers).mockResolvedValue([ADMIN_USER, OTHER_USER]);
  });

  // -------------------------------------------------------------------------
  // Loading & error states
  // -------------------------------------------------------------------------

  it('shows a loading indicator before the request resolves', () => {
    // Make getUsers hang so the loading state persists during the assertion
    vi.mocked(adminApi.getUsers).mockReturnValue(new Promise(() => {}));
    renderModal();
    expect(screen.getByText(/cargando usuarios/i)).toBeInTheDocument();
  });

  it('shows an error message when getUsers rejects', async () => {
    vi.mocked(adminApi.getUsers).mockRejectedValue(new Error('Network error'));
    renderModal();
    await waitFor(() =>
      expect(screen.getByText(/error al cargar/i)).toBeInTheDocument()
    );
  });

  // -------------------------------------------------------------------------
  // Rendering users
  // -------------------------------------------------------------------------

  it('renders all users after successful load', async () => {
    renderModal();
    await waitFor(() => {
      expect(screen.getByText('adminuser')).toBeInTheDocument();
      expect(screen.getByText('otheruser')).toBeInTheDocument();
    });
  });

  it('shows "(tú)" badge next to the current user', async () => {
    renderModal('adminuser');
    await waitFor(() => expect(screen.getByText('tú')).toBeInTheDocument());
  });

  it('does NOT show a role selector for the current user row', async () => {
    renderModal('adminuser');
    await waitFor(() => screen.getByText('adminuser'));
    // Only otheruser's row should have a <select>
    expect(screen.getAllByRole('combobox').length).toBe(1);
  });

  it('does NOT show a delete button for the current user row', async () => {
    renderModal('adminuser');
    await waitFor(() => screen.getByText('adminuser'));
    // Only one delete button — for otheruser
    expect(screen.getAllByTitle(/eliminar a/i).length).toBe(1);
  });

  // -------------------------------------------------------------------------
  // Role change
  // -------------------------------------------------------------------------

  it('calls updateUserRole and updates the table when the select changes', async () => {
    const updated: adminApi.UserEntry = { ...OTHER_USER, role: 'ADMIN' };
    vi.mocked(adminApi.updateUserRole).mockResolvedValueOnce(updated);

    renderModal();
    await waitFor(() => screen.getByText('otheruser'));

    const select = screen.getByRole('combobox') as HTMLSelectElement;
    await userEvent.selectOptions(select, 'ADMIN');

    await waitFor(() =>
      expect(adminApi.updateUserRole).toHaveBeenCalledWith(OTHER_USER.id, 'ADMIN')
    );
  });

  it('shows a toast error when updateUserRole fails', async () => {
    const toast = (await import('react-hot-toast')).default;
    vi.mocked(adminApi.updateUserRole).mockRejectedValueOnce(new Error('fail'));

    renderModal();
    await waitFor(() => screen.getByText('otheruser'));

    const select = screen.getByRole('combobox');
    await userEvent.selectOptions(select, 'ADMIN');

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith('No se pudo actualizar el rol')
    );
  });

  // -------------------------------------------------------------------------
  // Delete user
  // -------------------------------------------------------------------------

  it('calls deleteUser and removes the row when confirmed', async () => {
    vi.spyOn(window, 'confirm').mockReturnValueOnce(true);
    vi.mocked(adminApi.deleteUser).mockResolvedValueOnce(undefined);

    renderModal();
    await waitFor(() => screen.getByText('otheruser'));

    await userEvent.click(screen.getByTitle('Eliminar a otheruser'));

    await waitFor(() => {
      expect(adminApi.deleteUser).toHaveBeenCalledWith(OTHER_USER.id);
      expect(screen.queryByText('otheruser')).not.toBeInTheDocument();
    });
  });

  it('does NOT call deleteUser when the confirm dialog is cancelled', async () => {
    vi.spyOn(window, 'confirm').mockReturnValueOnce(false);

    renderModal();
    await waitFor(() => screen.getByText('otheruser'));

    await userEvent.click(screen.getByTitle('Eliminar a otheruser'));

    expect(adminApi.deleteUser).not.toHaveBeenCalled();
    expect(screen.getByText('otheruser')).toBeInTheDocument();
  });

  it('shows a toast error when deleteUser fails', async () => {
    const toast = (await import('react-hot-toast')).default;
    vi.spyOn(window, 'confirm').mockReturnValueOnce(true);
    vi.mocked(adminApi.deleteUser).mockRejectedValueOnce(new Error('fail'));

    renderModal();
    await waitFor(() => screen.getByText('otheruser'));

    await userEvent.click(screen.getByTitle('Eliminar a otheruser'));

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith('No se pudo eliminar el usuario')
    );
  });
});
