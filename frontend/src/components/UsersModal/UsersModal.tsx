/**
 * UsersModal.tsx
 *
 * Admin-only modal that lists all registered users in a table.
 * Each row provides:
 *  - A role selector (USER / ADMIN) that calls PUT /admin/users/{id}/role on change.
 *  - A delete button (🗑) that calls DELETE /admin/users/{id} after confirmation.
 *
 * The logged-in admin's own row shows a "(tú)" badge, a read-only role badge
 * instead of the selector, and no delete button — preventing accidental
 * self-demotion or self-deletion.
 *
 * Props:
 *  - onClose         — called when the user dismisses the modal.
 *  - currentUsername — username of the logged-in admin, used to identify
 *                      and protect the admin's own row.
 */
import { useEffect, useState } from "react";
import Modal from "../Modal";
import { getUsers, updateUserRole, deleteUser, type UserEntry } from "../../api/adminApi";
import toast from "react-hot-toast";
import "./UsersModal.css";

interface Props {
  onClose: () => void;
  currentUsername: string | null;
}

export default function UsersModal({ onClose, currentUsername }: Props) {
  const [users, setUsers] = useState<UserEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  // Tracks which user IDs are currently being saved (disables their select)
  const [saving, setSaving] = useState<Set<number>>(new Set());
  // Tracks which user IDs are currently being deleted (disables their row)
  const [deleting, setDeleting] = useState<Set<number>>(new Set());

  // Fetch all users when the modal opens
  useEffect(() => {
    getUsers()
      .then(setUsers)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  /** Updates a user's role in the backend and reflects the change locally. */
  const handleRoleChange = async (userId: number, newRole: "USER" | "ADMIN") => {
    setSaving((prev) => new Set(prev).add(userId));
    try {
      const updated = await updateUserRole(userId, newRole);
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      toast.success("Rol actualizado");
    } catch {
      toast.error("No se pudo actualizar el rol");
    } finally {
      setSaving((prev) => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  /** Deletes a user after confirmation and removes the row from the list. */
  const handleDelete = async (userId: number, username: string) => {
    if (!window.confirm(`¿Eliminar al usuario "${username}"? Esta acción no se puede deshacer.`)) {
      return;
    }
    setDeleting((prev) => new Set(prev).add(userId));
    try {
      await deleteUser(userId);
      setUsers((prev) => prev.filter((u) => u.id !== userId));
      toast.success(`Usuario "${username}" eliminado`);
    } catch {
      toast.error("No se pudo eliminar el usuario");
    } finally {
      setDeleting((prev) => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  return (
    <Modal onClose={onClose} className="modal-wide">
      <div className="users-modal-content">
        <h2>Gestión de usuarios</h2>

        {loading && <p className="users-modal-status">Cargando usuarios...</p>}
        {error && <p className="users-modal-status">Error al cargar los usuarios.</p>}

        {!loading && !error && (
          <div className="users-table-wrapper">
            <table className="users-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Usuario</th>
                  <th>Rol</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const isCurrentUser = user.username === currentUsername;
                  return (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>
                        {user.username}
                        {isCurrentUser && <span className="you-badge">tú</span>}
                      </td>
                      <td>
                        {isCurrentUser ? (
                          /* Admins cannot change their own role to avoid accidental lock-out */
                          <span className={`role-badge ${user.role === "ADMIN" ? "admin" : "user"}`}>
                            {user.role}
                          </span>
                        ) : (
                          <select
                            className="role-select"
                            value={user.role}
                            disabled={saving.has(user.id) || deleting.has(user.id)}
                            onChange={(e) =>
                              handleRoleChange(user.id, e.target.value as "USER" | "ADMIN")
                            }
                          >
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                          </select>
                        )}
                      </td>
                      <td>
                        {!isCurrentUser && (
                          <button
                            className="btn-delete-user"
                            disabled={deleting.has(user.id) || saving.has(user.id)}
                            onClick={() => handleDelete(user.id, user.username)}
                            title={`Eliminar a ${user.username}`}
                          >
                            {deleting.has(user.id) ? "…" : "🗑"}
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Modal>
  );
}
