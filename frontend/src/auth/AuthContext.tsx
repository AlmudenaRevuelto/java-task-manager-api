/**
 * AuthContext.tsx
 *
 * Provides a React context that serves as the single source of truth
 * for authentication state across the application.
 *
 * The JWT token is persisted in localStorage so that it survives page reloads.
 * All localStorage reads/writes for the token are centralised here;
 * no other file should access the token key directly.
 *
 * Exports:
 *  - AuthProvider  — wraps the component tree to make auth state available.
 *  - useAuth()     — hook to consume token, login(), and logout() anywhere.
 */
import { createContext, useContext, useState } from "react";

/** Shape of the values exposed by the auth context. */
interface AuthContextType {
  /** The current JWT token, or null when the user is not authenticated. */
  token: string | null;
  /** The logged-in username, or null when not authenticated. */
  username: string | null;
  /** The logged-in user's role ("USER" | "ADMIN"), or null when not authenticated. */
  role: string | null;
  /** Persists the token, username and role in localStorage and updates React state. */
  login: (info: { token: string; username: string; role: string }) => void;
  /** Removes the token from localStorage and clears React state. */
  logout: () => void;
}

// Create the context with undefined as the default (signals missing provider)
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * AuthProvider
 *
 * Wrap the application (or a subtree) with this component to give descendant
 * components access to authentication state via useAuth().
 * Initialises the token from localStorage so the user stays logged in on reload.
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  // Initialise from localStorage to persist login across page refreshes
  const [token, setToken] = useState<string | null>(localStorage.getItem("token"));
  const [username, setUsername] = useState<string | null>(localStorage.getItem("username"));
  const [role, setRole] = useState<string | null>(localStorage.getItem("role"));

  /** Store the token, username and role — called after a successful login. */
  const login = ({ token, username, role }: { token: string; username: string; role: string }) => {
    localStorage.setItem("token", token);
    localStorage.setItem("username", username);
    localStorage.setItem("role", role);
    setToken(token);
    setUsername(username);
    setRole(role);
  };

  /** Clear the token and update state — called on logout. */
  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    setToken(null);
    setUsername(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider value={{ token, username, role, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth
 *
 * Custom hook that returns the current auth context value.
 * Throws if called outside of an AuthProvider to catch misconfiguration early.
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}