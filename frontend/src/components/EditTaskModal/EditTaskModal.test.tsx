/**
 * EditTaskModal.test.tsx
 *
 * Tests for the EditTaskModal component.
 *
 * Key technique — mocking modules:
 *   vi.mock('path')  replaces a real module with a fake version for the test.
 *   We mock:
 *     - ../../api/taskApi  → so no real HTTP calls are made
 *     - react-hot-toast    → so toasts don't throw errors in jsdom
 */
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import EditTaskModal from './EditTaskModal';
import * as taskApi from '../../api/taskApi';
import type { Task } from '../../types/task';

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

// Replace the real updateTask with a spy. We'll configure the resolved value
// inside each test that needs it.
vi.mock('../../api/taskApi', () => ({
  updateTask: vi.fn(),
}));

// react-hot-toast tries to attach to the DOM; mocking it avoids setup noise.
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// ---------------------------------------------------------------------------
// Test data
// ---------------------------------------------------------------------------

/** A minimal Task object that satisfies the interface. */
const sampleTask: Task = {
  id: 1,
  title: 'Sample task',
  description: 'A description',
  priority: 'HIGH',
  completed: false,
  dueDate: '2026-12-31',
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

// ---------------------------------------------------------------------------
// Helper
// ---------------------------------------------------------------------------

/** Renders EditTaskModal with default no-op callbacks unless overridden. */
function renderModal(overrides?: {
  task?: Task;
  onClose?: () => void;
  onUpdated?: (t: Task) => void;
}) {
  const props = {
    task: sampleTask,
    onClose: vi.fn(),
    onUpdated: vi.fn(),
    ...overrides,
  };
  render(<EditTaskModal {...props} />);
  return props;
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('EditTaskModal', () => {
  // Reset all mocks between tests so spy call counts don't bleed over
  beforeEach(() => vi.clearAllMocks());

  // -------------------------------------------------------------------------
  // Rendering & pre-population
  // -------------------------------------------------------------------------

  it('renders the form title', () => {
    renderModal();
    expect(screen.getByText('Editar tarea')).toBeInTheDocument();
  });

  it('pre-populates the title input with the task title', () => {
    renderModal();
    // Look up the text input and verify its initial pre-populated value
    expect(screen.getByDisplayValue('Sample task')).toBeInTheDocument();
  });

  it('pre-populates the description textarea', () => {
    renderModal();
    expect(screen.getByDisplayValue('A description')).toBeInTheDocument();
  });

  it('pre-populates the priority select', () => {
    renderModal();
    // The <select> shows the current priority value
    expect(screen.getByDisplayValue('HIGH')).toBeInTheDocument();
  });

  it('pre-populates the due date input', () => {
    renderModal();
    expect(screen.getByDisplayValue('2026-12-31')).toBeInTheDocument();
  });

  it('shows the completed checkbox unchecked when task is pending', () => {
    renderModal();
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement;
    expect(checkbox.checked).toBe(false);
  });

  it('shows the completed checkbox checked when task is done', () => {
    renderModal({ task: { ...sampleTask, completed: true } });
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
  });

  // -------------------------------------------------------------------------
  // Validation
  // -------------------------------------------------------------------------

  it('does not submit and shows a toast when title is too short (< 3 chars)', async () => {
    // Import the toast mock so we can spy on it
    const toast = (await import('react-hot-toast')).default;

    renderModal();

    // Clear the title and type a 2-character string
    const titleInput = screen.getByDisplayValue('Sample task');
    await userEvent.clear(titleInput);
    await userEvent.type(titleInput, 'AB');

    await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }));

    // updateTask should NOT have been called
    expect(taskApi.updateTask).not.toHaveBeenCalled();
    // A toast error should have been triggered
    expect(toast.error).toHaveBeenCalledWith('El título debe tener al menos 3 caracteres');
  });

  // -------------------------------------------------------------------------
  // Successful submit
  // -------------------------------------------------------------------------

  it('calls updateTask and onUpdated on a valid submit', async () => {
    const updatedTask = { ...sampleTask, title: 'Updated title' };
    // Make the mock resolve with the updated task
    vi.mocked(taskApi.updateTask).mockResolvedValueOnce(updatedTask);

    const onUpdated = vi.fn();
    renderModal({ onUpdated });

    // Change the title
    const titleInput = screen.getByDisplayValue('Sample task');
    await userEvent.clear(titleInput);
    await userEvent.type(titleInput, 'Updated title');

    await userEvent.click(screen.getByRole('button', { name: /guardar cambios/i }));

    // Wait for the async submit to finish
    await waitFor(() => {
      expect(taskApi.updateTask).toHaveBeenCalledWith(
        sampleTask.id,
        expect.objectContaining({ title: 'Updated title' })
      );
      expect(onUpdated).toHaveBeenCalledWith(updatedTask);
    });
  });

  // -------------------------------------------------------------------------
  // Cancel / close
  // -------------------------------------------------------------------------

  it('calls onClose when the Cancel button is clicked', async () => {
    const onClose = vi.fn();
    renderModal({ onClose });

    await userEvent.click(screen.getByRole('button', { name: /cancelar/i }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
