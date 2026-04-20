/**
 * Modal.tsx
 *
 * Generic overlay modal wrapper used by both CreateTaskForm and EditTaskModal.
 *
 * Behaviour:
 *  - Clicking the dark backdrop (modal-overlay) closes the modal via onClose.
 *  - Clicking anywhere inside the white card (modal-content) stops propagation
 *    so the backdrop click handler is not triggered.
 *  - An ✖ close button is rendered in the top-right corner of the card.
 *
 * Styling and the fade-in animation are provided by Modal.css.
 */
import "./Modal.css";

interface ModalProps {
  /** Content to render inside the modal card. */
  children: React.ReactNode;
  /** Called when the user closes the modal (backdrop click or ✖ button). */
  onClose: () => void;
  /** Optional extra CSS class applied to the modal-content div (e.g. "modal-wide"). */
  className?: string;
}

export default function Modal({ children, onClose, className }: ModalProps) {
  return (
    // Clicking the dark backdrop closes the modal
    <div className="modal-overlay" onClick={onClose}>
      {/* Stop clicks inside the card from bubbling up to the backdrop */}
      <div className={`modal-content${className ? ` ${className}` : ""}`} onClick={(e) => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose} aria-label="Close modal">
          ✖
        </button>
        {children}
      </div>
    </div>
  );
}
