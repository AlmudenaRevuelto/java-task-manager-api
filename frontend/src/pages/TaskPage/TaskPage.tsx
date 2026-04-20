/**
 * TaskPage.tsx
 *
 * Main protected view, rendered after a successful login.
 * Displays the authenticated user's tasks in a responsive 3-column grid.
 *
 * Features:
 *  - Real-time search (debounced 300 ms) across task title and description.
 *  - Filter chips for priority (LOW / MEDIUM / HIGH) and status (pending / done).
 *  - All filtering is delegated to the backend via GET /tasks query params.
 *  - Click a card to open the EditTaskModal pre-populated with its data.
 *  - Delete button on each card removes the task immediately from the list.
 *  - Pagination controls (12 tasks per page) at the bottom of the grid.
 *
 * Styling is provided by TaskPage.css.
 */
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Task, Priority } from "../../types/task";
import { getTasks, deleteTask, type TaskFilters, type TaskPage } from "../../api/taskApi";
import { useAuth } from "../../auth/AuthContext";
import toast from "react-hot-toast";
import "./TaskPage.css";
import CreateTaskForm from "../../components/CreateTaskForm";
import Modal from "../../components/Modal";
import EditTaskModal from "../../components/EditTaskModal";
import TaskCard from "../../components/TaskCard";
import FilterBar from "../../components/FilterBar";
import Pagination from "../../components/Pagination";

export default function TaskPage() {
  const navigate = useNavigate();
  // logout() from AuthContext clears the token from state and localStorage
  const { logout } = useAuth();

  // Task list fetched from the backend
  const [tasks, setTasks] = useState<Task[]>([]);
  // Controls visibility of the "create task" modal
  const [showForm, setShowForm] = useState(false);
  // The task currently open in the edit modal, or null when no modal is shown
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);

  // --- Filter state ---
  // Raw search string bound directly to the input
  const [search, setSearch] = useState("");
  // Debounced copy of search — updated 300 ms after the user stops typing
  const [debouncedSearch, setDebouncedSearch] = useState("");
  // Priority chip selection; empty string means "all priorities"
  const [priority, setPriority] = useState<Priority | "">("")
  
  // Completion chip selection
  const [completedFilter, setCompletedFilter] = useState<"all" | "pending" | "done">("all");
  // Incrementing this counter triggers a fresh fetch while keeping filters active
  const [reloadKey, setReloadKey] = useState(0);

  // --- Pagination state ---
  // Zero-based current page index sent to the backend
  const [currentPage, setCurrentPage] = useState(0);
  // Total pages returned by the last backend response
  const [totalPages, setTotalPages] = useState(0);
  // Total number of matching tasks (used for the counter label)
  const [totalElements, setTotalElements] = useState(0);

  /**
   * Triggers a fresh fetch without clearing the active filters.
   * Resets to page 0 so a newly created task is always visible.
   */
  const loadTasks = () => {
    setCurrentPage(0);
    setReloadKey((k) => k + 1);
  };

  /**
   * Deletes a task by ID and removes it from the local list on success.
   * Optimistically skips a full reload for a faster UI response.
   */
  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      setTasks(prev => prev.filter(t => t.id !== id));
      toast.success("Tarea eliminada");
    } catch {
      toast.error("Error al eliminar la tarea");
    }
  };

  // Sync debouncedSearch with a 300 ms delay so we don't fire an API request
  // on every keystroke — only after the user has paused typing.
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 300);
    return () => clearTimeout(timer); // Cancel the timer if search changes again
  }, [search]);

  // Re-fetch tasks whenever the debounced search text, a filter chip, the
  // current page, or the reloadKey changes.
  useEffect(() => {
    const filters: TaskFilters = {};
    if (debouncedSearch.trim()) filters.search = debouncedSearch.trim();
    if (priority) filters.priority = priority;
    if (completedFilter === "pending") filters.completed = false;
    if (completedFilter === "done") filters.completed = true;
    getTasks(filters, currentPage).then((page: TaskPage) => {
      setTasks(page.content);
      setTotalPages(page.totalPages);
      setTotalElements(page.totalElements);
    });
  }, [debouncedSearch, priority, completedFilter, reloadKey, currentPage]);

  /** Applies a filter change and resets to page 0 so results start from the top. */
  const handleFilterChange = (fn: () => void) => {
    fn();
    setCurrentPage(0);
  };

  /** Clears the JWT token from AuthContext and redirects to the login page. */
  const handleLogout = () => {
    logout();           // Removes token from AuthContext and localStorage
    navigate("/login"); // Send the user back to the login screen
  };

  return (
    <div className="container">

      {selectedTask && (
        <EditTaskModal
          key={selectedTask.id}
          task={selectedTask}
          onClose={() => setSelectedTask(null)}
          onUpdated={(updated) => {
            setTasks(prev => prev.map(t => t.id === updated.id ? updated : t));
            setSelectedTask(null);
          }}
        />
      )}

      {showForm && (
        <Modal onClose={() => setShowForm(false)}>
          <CreateTaskForm
            onCreated={() => {
              loadTasks();
              setShowForm(false);
            }}
          />
        </Modal>
      )}
      {/* Page header with title and logout button */}
      <div className="header">
        <h1>Tasks</h1>

        <div className="actions">
          <button className={showForm ? "btn-cancel" : "btn-new-task"} onClick={() => setShowForm(!showForm)}>
            {showForm ? "✖ Cancelar" : "+ Nueva tarea"}
          </button>

          <button className="btn-logout" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </div>

      <FilterBar
        search={search}
        onSearchChange={setSearch}
        onClearSearch={() => { setSearch(""); setCurrentPage(0); }}
        priority={priority}
        onPriorityChange={(v) => handleFilterChange(() => setPriority(v))}
        completedFilter={completedFilter}
        onCompletedFilterChange={(v) => handleFilterChange(() => setCompletedFilter(v))}
      />

      {/* 3-column grid of task cards */}
      <div className="task-grid">
        {tasks.length === 0 ? (
          <div className="empty-state">
            <span className="empty-state-icon">📋</span>
            <span>No se encontraron tareas</span>
          </div>
        ) : tasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            onSelect={setSelectedTask}
            onDelete={handleDelete}
          />
        ))}
      </div>

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={setCurrentPage}
      />
    </div>
  );
}
