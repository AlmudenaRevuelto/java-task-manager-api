/**
 * RegisterModal.tsx
 *
 * Modal form for creating a new user account via POST /auth/register.
 * Shown from the login page when the user clicks "Crear cuenta".
 *
 * Props:
 *  - onClose     — called when the user cancels or clicks outside the modal.
 *  - onRegistered — called with the returned JWT token after a successful registration
 *                   so the caller can log the user in immediately.
 */
import { useState } from "react";
import Modal from "../Modal";
import { register } from "../../api/authApi";
import toast from "react-hot-toast";
import "./RegisterModal.css";

interface AuthInfo {
  token: string;
  username: string;
  role: string;
}

interface Props {
  onClose: () => void;
  /** Receives the full auth payload returned by the backend after successful registration. */
  onRegistered: (data: AuthInfo) => void;
}

export default function RegisterModal({ onClose, onRegistered }: Props) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);

  /**
   * Validates the form and submits the registration request.
   * On success, the JWT token is passed back to the parent via onRegistered().
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (username.trim().length < 3) {
      toast.error("El nombre de usuario debe tener al menos 3 caracteres");
      return;
    }
    if (password.length < 6) {
      toast.error("La contraseña debe tener al menos 6 caracteres");
      return;
    }
    if (password !== confirmPassword) {
      toast.error("Las contraseñas no coinciden");
      return;
    }

    setLoading(true);
    try {
      const data = await register(username.trim(), password);
      toast.success(`Cuenta creada. ¡Bienvenido, ${data.username}!`);
      onRegistered({ token: data.token, username: data.username, role: data.role });
    } catch {
      toast.error("No se pudo crear la cuenta. El nombre de usuario puede estar en uso.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal onClose={onClose}>
      <form className="register-form" onSubmit={handleSubmit}>
        <h2>Crear cuenta</h2>

        {/* Username field */}
        <div className="register-field">
          <label>Nombre de usuario</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Mínimo 3 caracteres"
            required
            autoFocus
          />
        </div>

        {/* Password field */}
        <div className="register-field">
          <label>Contraseña</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Mínimo 6 caracteres"
            required
          />
        </div>

        {/* Confirm password field */}
        <div className="register-field">
          <label>Confirmar contraseña</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="Repite la contraseña"
            required
          />
        </div>

        <div className="register-actions">
          <button type="button" className="btn-secondary" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Creando..." : "Crear cuenta"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
