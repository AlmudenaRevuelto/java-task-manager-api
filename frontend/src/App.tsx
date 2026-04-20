/**
 * App.tsx
 *
 * Root component of the application.
 * Configures client-side routing and wraps the component tree with
 * the AuthProvider so that authentication state is available globally.
 *
 * Route structure:
 *  /login  → LoginPage          (public)
 *  /tasks  → TaskPage           (private — requires a valid JWT token)
 *  *       → redirect to /login (catch-all)
 */
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import LoginPage from "./pages/LoginPage";
import TaskPage from "./pages/TaskPage";

/**
 * PrivateRoute
 *
 * A route guard that renders its children only when the user is authenticated
 * (i.e. a JWT token exists in the auth context). Otherwise, it redirects
 * the browser to the /login page, preserving the current history entry.
 */
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { token } = useAuth();
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

/** Application entry point — sets up providers and routes. */
function App() {
  return (
    // AuthProvider must wrap BrowserRouter so that PrivateRoute can call useAuth()
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public route: login form */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected route: task list — accessible only when authenticated */}
          <Route
            path="/tasks"
            element={
              <PrivateRoute>
                <TaskPage />
              </PrivateRoute>
            }
          />

          {/* Redirect any unmatched path to the login page */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;