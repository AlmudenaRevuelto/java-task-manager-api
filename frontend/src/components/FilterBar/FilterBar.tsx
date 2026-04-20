/**
 * FilterBar.tsx
 *
 * Search input with debounce support, priority chips and status chips.
 * This is a pure presentational component: all state lives in the parent
 * (TaskPage) and is passed down via props and callbacks.
 *
 * Styling is provided by TaskPage.css (filter-bar, search-*, chip-* classes).
 */
import type { Priority } from "../../types/task";

interface Props {
  /** Current raw search string controlled by the parent. */
  search: string;
  /** Called on every keystroke in the search input. */
  onSearchChange: (value: string) => void;
  /** Called when the clear (✖) button is clicked. */
  onClearSearch: () => void;
  /** Currently active priority filter; empty string means "all". */
  priority: Priority | "";
  /** Called when a priority chip is clicked. */
  onPriorityChange: (value: Priority | "") => void;
  /** Currently active status filter. */
  completedFilter: "all" | "pending" | "done";
  /** Called when a status chip is clicked. */
  onCompletedFilterChange: (value: "all" | "pending" | "done") => void;
}

export default function FilterBar({
  search,
  onSearchChange,
  onClearSearch,
  priority,
  onPriorityChange,
  completedFilter,
  onCompletedFilterChange,
}: Props) {
  return (
    <div className="filter-bar">
      {/* Search input */}
      <div className="search-wrapper">
        <span className="search-icon">🔍</span>
        <input
          type="text"
          className="search-input"
          placeholder="Buscar por título o descripción..."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
        />
        {search && (
          <button className="search-clear" onClick={onClearSearch} title="Limpiar">
            ✖
          </button>
        )}
      </div>

      {/* Filter chips */}
      <div className="filter-chips">
        <span className="filter-label">Prioridad:</span>
        <button className={`chip${priority === "" ? " chip-active" : ""}`} onClick={() => onPriorityChange("")}>Todas</button>
        <button className={`chip chip-low${priority === "LOW" ? " chip-active" : ""}`} onClick={() => onPriorityChange("LOW")}>LOW</button>
        <button className={`chip chip-medium${priority === "MEDIUM" ? " chip-active" : ""}`} onClick={() => onPriorityChange("MEDIUM")}>MEDIUM</button>
        <button className={`chip chip-high${priority === "HIGH" ? " chip-active" : ""}`} onClick={() => onPriorityChange("HIGH")}>HIGH</button>

        <span className="filter-sep" />

        <span className="filter-label">Estado:</span>
        <button className={`chip${completedFilter === "all" ? " chip-active" : ""}`} onClick={() => onCompletedFilterChange("all")}>Todas</button>
        <button className={`chip chip-pending${completedFilter === "pending" ? " chip-active" : ""}`} onClick={() => onCompletedFilterChange("pending")}>Pendientes</button>
        <button className={`chip chip-done${completedFilter === "done" ? " chip-active" : ""}`} onClick={() => onCompletedFilterChange("done")}>Completadas</button>
      </div>
    </div>
  );
}
