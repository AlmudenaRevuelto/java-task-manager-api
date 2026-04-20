/**
 * authApi.ts
 *
 * Provides functions for interacting with the authentication endpoints
 * of the Spring Boot backend (/auth/*).
 */
import api from "./axiosConfig";

/** Shape of the response returned by the login endpoint. */
interface LoginResponse {
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
export const login = async (username: string, password: string): Promise<LoginResponse> => {
  const response = await api.post<LoginResponse>("/auth/login", { username, password });
  return response.data;
};
