/**
 * main.tsx
 *
 * Application entry point. Mounts the React tree into the #root DOM element.
 *
 * Provider hierarchy (outermost → innermost):
 *  StrictMode — enables extra runtime warnings during development.
 *  App        — sets up AuthProvider, BrowserRouter and Routes.
 *  Toaster    — renders toast notification pop-ups for the whole app.
 */
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { Toaster } from "react-hot-toast";

/**
 * Combines the main App with the global toast notification container.
 * Kept as a separate component so the provider hierarchy stays readable.
 */
function AppWithToaster() {
  return (
    <>
      <App />
      <Toaster position="top-right" />
    </>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AppWithToaster />
  </StrictMode>
);