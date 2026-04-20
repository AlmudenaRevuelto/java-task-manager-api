/**
 * Modal.test.tsx
 *
 * Tests for the Modal wrapper component.
 *
 * Concepts used:
 *  - render()        Renders the component into a virtual DOM.
 *  - screen          Lets us query for elements by role, text, label, etc.
 *  - userEvent       Simulates real user interactions (clicks, typing…).
 *  - vi.fn()         Creates a mock function so we can check if it was called.
 *  - expect(…)       Makes assertions about what happened.
 */
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Modal from './Modal';

describe('Modal', () => {
  // -----------------------------------------------------------------------
  // Rendering
  // -----------------------------------------------------------------------

  it('renders its children inside the modal card', () => {
    // Arrange & Act
    render(
      <Modal onClose={() => {}}>
        <p>Hello from inside the modal</p>
      </Modal>
    );

    // Assert — the text we passed as children should be visible
    expect(screen.getByText('Hello from inside the modal')).toBeInTheDocument();
  });

  it('renders the close (✖) button', () => {
    render(<Modal onClose={() => {}}>content</Modal>);

    // The button has aria-label="Close modal" so we can find it accessibly
    expect(screen.getByRole('button', { name: /close modal/i })).toBeInTheDocument();
  });

  // -----------------------------------------------------------------------
  // Interactions — close behaviour
  // -----------------------------------------------------------------------

  it('calls onClose when the ✖ button is clicked', async () => {
    // vi.fn() creates a "spy": a fake function that records every call
    const handleClose = vi.fn();
    render(<Modal onClose={handleClose}>content</Modal>);

    await userEvent.click(screen.getByRole('button', { name: /close modal/i }));

    // The spy should have been called exactly once
    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('calls onClose when the dark backdrop is clicked', async () => {
    const handleClose = vi.fn();
    const { container } = render(<Modal onClose={handleClose}>content</Modal>);

    // The backdrop is the outermost div with class "modal-overlay"
    const backdrop = container.querySelector('.modal-overlay')!;
    await userEvent.click(backdrop);

    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('does NOT call onClose when clicking inside the modal card', async () => {
    const handleClose = vi.fn();
    render(
      <Modal onClose={handleClose}>
        <p>Card content</p>
      </Modal>
    );

    // Click directly on the text inside the card — the stopPropagation should
    // prevent this from bubbling up to the backdrop handler
    await userEvent.click(screen.getByText('Card content'));

    expect(handleClose).not.toHaveBeenCalled();
  });
});
