/**
 * adminApi.ts
 *
 * API functions for the admin endpoints (GET/PUT /admin/users).
 * All requests are automatically authenticated via the JWT interceptor
 * configured in axiosConfig.ts.
 */
import api from "./axiosConfig";

/** Shape of a user entry returned by GET /admin/users. */
export interface UserEntry {
  id: number;
  username: string;
  role: "USER" | "ADMIN";
}

/**
 * Fetches the list of all registered users.
 * Requires ADMIN role — the backend will return 403 otherwise.
 */
export const getUsers = async (): Promise<UserEntry[]> => {
  const response = await api.get<UserEntry[]>("/admin/users");
  return response.data;
};

/**
 * Updates the role of a user.
 *
 * @param id   - The user's ID.
 * @param role - The new role to assign ("USER" or "ADMIN").
 * @returns The updated user entry.
 */
export const updateUserRole = async (id: number, role: "USER" | "ADMIN"): Promise<UserEntry> => {
  const response = await api.put<UserEntry>(`/admin/users/${id}/role`, { role });
  return response.data;
};

/**
 * Deletes a user account and all their tasks.
 * The backend prevents an admin from deleting their own account.
 *
 * @param id - The user's ID.
 */
export const deleteUser = async (id: number): Promise<void> => {
  await api.delete(`/admin/users/${id}`);
};
