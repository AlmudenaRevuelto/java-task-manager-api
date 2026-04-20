import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    // In development, proxy API calls to the local Spring Boot backend so
    // axios can use relative URLs (same as in the Docker/nginx setup).
    proxy: {
      '/auth':  'http://localhost:8080',
      '/tasks': 'http://localhost:8080',
    },
  },

  test: {
    // Use a real DOM environment for every test (browser-like behaviour)
    environment: 'jsdom',
    // Allow describe/it/expect without explicit imports
    globals: true,
    // Load extra jest-dom matchers before each test file
    setupFiles: './src/setupTests.ts',
  },
})
