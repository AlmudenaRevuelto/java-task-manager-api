/**
 * TaskCard.tsx
 *
 * Presentational card that displays a single task's summary.
 * Clicking the card opens the edit modal (via onSelect).
 * The delete button stops propagation so it doesn't also trigger onSelect.
 *
 * Styling is inherited from TaskPage.css (badge-*, status-*, btn-delete classes).
 */
import type { Task } from "../../types/task";

interface Props {
  /** The task to display. */
  task: Task;
  /** Called with the task when the card body is clicked. */
  onSelect: (task: Task) => void;
  /** Called with the task ID when the delete button is clicked. */
  onDelete: (id: number) => void;
}

export default function TaskCard({ task, onSelect, onDelete }: Props) {
  return (
    <div className="task-card" onClick={() => onSelect(task)}>
      <h3 className="task-title">{task.title}</h3>

      {task.description && (
        <p className="task-description">{task.description}</p>
      )}

      <div className="task-footer">
        <span className={`badge badge-${task.priority}`}>{task.priority}</span>
        <span className={`status ${task.completed ? "status-done" : "status-pending"}`}>
          {task.completed ? "Completada" : "Pendiente"}
        </span>
        <button
          className="btn-delete"
          onClick={(e) => { e.stopPropagation(); onDelete(task.id); }}
          title="Eliminar tarea"
        >
          🗑
        </button>
      </div>
    </div>
  );
}
