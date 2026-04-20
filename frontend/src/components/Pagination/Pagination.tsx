/**
 * Pagination.tsx
 *
 * Renders previous/next arrow buttons, numbered page buttons and a total count
 * label. Returns null when there is only one page so the parent doesn't need to
 * guard the render itself.
 *
 * Styling is provided by TaskPage.css (pagination, page-btn, page-btn-active,
 * page-info classes).
 */

interface Props {
  /** Zero-based index of the currently visible page. */
  currentPage: number;
  /** Total number of pages available for the current query. */
  totalPages: number;
  /** Total number of items matching the current query (shown as a label). */
  totalElements: number;
  /** Called with the new page index when the user clicks a page control. */
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
}: Props) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        className="page-btn"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
      >
        &#8249;
      </button>

      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          className={`page-btn${currentPage === i ? " page-btn-active" : ""}`}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}

      <button
        className="page-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
      >
        &#8250;
      </button>

      <span className="page-info">
        {totalElements} tarea{totalElements !== 1 ? "s" : ""}
      </span>
    </div>
  );
}
