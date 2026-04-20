/**
 * LoginPage.tsx
 *
 * Renders the login form and handles the authentication flow:
 *  1. The user submits their username and password.
 *  2. The credentials are sent to the backend via loginApi (POST /auth/login).
 *  3. On success, the returned JWT token is stored through AuthContext.login().
 *  4. The user is redirected to the /tasks page.
 *  5. On failure, an error message is shown inline.
 *
 * Styling is provided by LoginPage.css.
 */
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { login as loginApi } from "../../api/authApi";
import "./LoginPage.css";

export default function LoginPage() {
  const navigate = useNavigate();
  // login() from AuthContext persists the token and updates global auth state
  const { login } = useAuth();

  // Controlled input state
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  // Holds an error message when login fails
  const [error, setError] = useState("");

  /**
   * Handles form submission:
   * - Calls the authentication API with the entered credentials.
   * - Stores the JWT token and navigates to the task list on success.
   * - Displays an error message if the request fails (wrong credentials, network error, etc.).
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); // Clear any previous error before retrying
    try {
      const data = await loginApi(username, password);
      login(data.token); // Persist token via AuthContext
      navigate("/tasks"); // Redirect to the protected task list
    } catch {
      setError("Usuario o contraseña incorrectos");
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <h2>Iniciar sesión</h2>
        <form onSubmit={handleSubmit}>
          {/* Username field */}
          <div className="form-group">
            <label>Usuario</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          {/* Password field */}
          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {/* Inline error message shown only when login fails */}
          {error && <p className="login-error">{error}</p>}

          <button type="submit" className="login-btn">
            Entrar
          </button>
        </form>
      </div>
    </div>
  );
}
