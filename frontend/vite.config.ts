import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    // Simula un DOM real en cada test (como si fuera el navegador)
    environment: 'jsdom',
    // Permite usar describe/it/expect sin importarlos explícitamente
    globals: true,
    // Carga matchers extra de jest-dom antes de cada test
    setupFiles: './src/setupTests.ts',
  },
})
