/**
 * CreateTaskForm.tsx
 *
 * Form rendered inside the Modal overlay for creating a new task.
 * Validates the title length client-side before calling the API,
 * then notifies the parent via onCreated() so the task list can reload.
 *
 * Styling is provided by CreateTaskForm.css.
 */
import { useState } from "react";
import { createTask } from "../../api/taskApi";
import type { Priority } from "../../types/task";
import "./CreateTaskForm.css";
import toast from "react-hot-toast";

interface Props {
  /** Called after a task is successfully created so the parent can refresh. */
  onCreated: () => void;
}

export default function CreateTaskForm({ onCreated }: Props) {
    // Controlled form field state
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [priority, setPriority] = useState<Priority>("MEDIUM");
    const [dueDate, setDueDate] = useState("");

    const [loading, setLoading] = useState(false);
    // Inline validation/API error message shown below the form
    const [error, setError] = useState("");

    /**
     * Validates the form, calls the create API, and triggers onCreated on success.
     * Shows a toast notification for both success and error outcomes.
     */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(""); // Clear any previous error before retrying

        if (title.trim().length < 3) {
            setError("El título debe tener al menos 3 caracteres");
            return;
        }

        setLoading(true);
        try {
            await createTask({
                title,
                description,
                priority,
                // Send undefined instead of an empty string when no date is entered
                dueDate: dueDate || undefined,
            });
            toast.success("Tarea creada correctamente 🎉");
            onCreated();
        } catch {
            toast.error("Error al crear la tarea");
            setError("Error al crear la tarea");
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="form">
            <h2>Nueva tarea</h2>
            {error && <p className="error">{error}</p>}

            <input
                type="text"
                placeholder="Título"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
            />

            <textarea
                placeholder="Descripción"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
            />

            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
            </select>

            <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
            />

            <button type="submit" disabled={loading}>
                {loading ? "Creando..." : "Crear tarea"}
            </button>
        </form>
    );
}
