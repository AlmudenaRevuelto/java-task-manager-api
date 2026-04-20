/**
 * EditTaskModal.tsx
 *
 * Modal form pre-populated with the values of an existing task.
 * The parent passes the task object; each field is initialised from it via useState.
 * On submission, the updated task is sent to PUT /tasks/{id} and the parent is
 * notified via onUpdated() so it can update its local task list without a full reload.
 *
 * Styling is provided by EditTaskModal.css.
 * The Modal overlay wrapper (backdrop + close button) is from components/Modal.
 */
import { useState } from "react";
import type { Task, Priority } from "../../types/task";
import { updateTask } from "../../api/taskApi";
import Modal from "../Modal";
import toast from "react-hot-toast";
import "./EditTaskModal.css";

interface Props {
  /** The task whose data should pre-populate the form fields. */
  task: Task;
  /** Called when the user dismisses the modal without saving. */
  onClose: () => void;
  /** Called with the updated task object after a successful save. */
  onUpdated: (updated: Task) => void;
}

export default function EditTaskModal({ task, onClose, onUpdated }: Props) {
  // Each field is initialised from the task prop.
  // The `key` prop on this component (set by TaskPage) ensures a fresh mount
  // whenever a different task is selected, re-running these initialisers.
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description ?? "");
  const [priority, setPriority] = useState<Priority>(task.priority);
  const [dueDate, setDueDate] = useState(task.dueDate ?? "");
  const [completed, setCompleted] = useState(task.completed);
  const [loading, setLoading] = useState(false);

  /**
   * Validates the title, then calls PUT /tasks/{id} with the current field values.
   * Shows a toast for success or failure, and calls onUpdated on success.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (title.trim().length < 3) {
      toast.error("El título debe tener al menos 3 caracteres");
      return;
    }
    setLoading(true);
    try {
      const updated = await updateTask(task.id, {
        title,
        // Send undefined instead of an empty string so the backend stores null
        description: description || undefined,
        priority,
        dueDate: dueDate || undefined,
        completed,
      });
      toast.success("Tarea actualizada");
      onUpdated(updated);
    } catch {
      toast.error("Error al actualizar la tarea");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <form onSubmit={handleSubmit} className="edit-form">
        <h2>Editar tarea</h2>

        <div className="edit-field">
          <label>Título</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className="edit-field">
          <label>Descripción</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="edit-row">
          <div className="edit-field">
            <label>Prioridad</label>
            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </div>

          <div className="edit-field">
            <label>Fecha límite</label>
            <input
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
            />
          </div>
        </div>

        <label className="edit-checkbox">
          <input
            type="checkbox"
            checked={completed}
            onChange={(e) => setCompleted(e.target.checked)}
          />
          Marcar como completada
        </label>

        <div className="edit-actions">
          <button type="button" className="btn-secondary" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Guardando..." : "Guardar cambios"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
