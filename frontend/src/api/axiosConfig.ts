/**
 * axiosConfig.ts
 *
 * Creates and exports a pre-configured Axios instance used by all API modules.
 * The base URL points to the Spring Boot backend.
 *
 * A request interceptor is registered to automatically attach the JWT token
 * stored in localStorage to every outgoing request as an Authorization Bearer header.
 * If no token is present, the request is sent without the header (public endpoints).
 */
import axios from "axios";

// Shared Axios instance with the backend base URL
const api = axios.create({
  baseURL: "http://localhost:8080",
});

// Attach the JWT token to every request if one exists in localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default api;