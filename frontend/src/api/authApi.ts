/**
 * authApi.ts
 *
 * Provides functions for interacting with the authentication endpoints
 * of the Spring Boot backend (/auth/*).
 */
import api from "./axiosConfig";

/** Shape of the response returned by the login and register endpoints. */
interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

/**
 * Sends a login request with the given credentials.
 * On success, the backend returns a JWT token along with the username and role.
 *
 * @param username - The user's username.
 * @param password - The user's password.
 * @returns An object containing the JWT token, username, and role.
 */
export const login = async (username: string, password: string): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/login", { username, password });
  return response.data;
};

/**
 * Registers a new user account.
 * On success, the backend returns a JWT token so the user is immediately logged in.
 *
 * @param username - The desired username. Must be unique.
 * @param password - The plain-text password (BCrypt-encoded on the server).
 * @returns An object containing the JWT token, username, and role.
 */
export const register = async (username: string, password: string): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/register", { username, password });
  return response.data;
};
