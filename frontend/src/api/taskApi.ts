/**
 * taskApi.ts
 *
 * Functions for interacting with the task endpoints of the Spring Boot backend.
 * All requests are sent through the shared Axios instance in axiosConfig.ts,
 * which automatically attaches the JWT token from localStorage.
 *
 * Endpoints used:
 *  GET    /tasks        — paginated list with optional filters
 *  POST   /tasks        — create a new task
 *  PUT    /tasks/{id}   — update an existing task
 *  DELETE /tasks/{id}   — permanently delete a task
 */
import api from "./axiosConfig";
import type { Task, Priority } from "../types/task";

/**
 * Optional query parameters supported by the GET /tasks endpoint.
 * All fields are optional; omitting them returns the full unfiltered list.
 */
export interface TaskFilters {
  /** Free-text search matched against task title and description (case-insensitive). */
  search?: string;
  /** Return only tasks with this priority level. */
  priority?: Priority;
  /** When true returns only completed tasks; false returns only pending ones. */
  completed?: boolean;
}

/**
 * The paginated response shape returned by GET /tasks.
 * Mirrors the relevant fields of Spring's Page<TaskResponse>.
 */
export interface TaskPage {
  /** Tasks on the current page. */
  content: Task[];
  /** Zero-based index of the current page. */
  number: number;
  /** Total number of pages available with the current filters. */
  totalPages: number;
  /** Total number of tasks matching the current filters. */
  totalElements: number;
}

/**
 * Fetches a single page of tasks for the authenticated user.
 * Filters and pagination parameters are forwarded to the backend as query params.
 *
 * @param filters - Optional search, priority, and completion filters.
 * @param page    - Zero-based page index (default 0).
 * @param size    - Number of tasks per page (default 12).
 * @returns A TaskPage object containing the task array and pagination metadata.
 */
export const getTasks = async (
  filters: TaskFilters = {},
  page = 0,
  size = 12
): Promise<TaskPage> => {
  const params: Record<string, string> = {
    page: String(page),
    size: String(size),
  };
  if (filters.search) params.search = filters.search;
  if (filters.priority) params.priority = filters.priority;
  if (filters.completed !== undefined) params.completed = String(filters.completed);
  const response = await api.get("/tasks", { params });
  return {
    content: response.data.content,
    number: response.data.number,
    totalPages: response.data.totalPages,
    totalElements: response.data.totalElements,
  };
};

/**
 * Permanently deletes the task with the given ID.
 * The backend enforces ownership — users can only delete their own tasks.
 *
 * @param id - Numeric ID of the task to delete.
 */
export const deleteTask = async (id: number): Promise<void> => {
  await api.delete(`/tasks/${id}`);
};

/**
 * Updates an existing task with new field values.
 * All updatable fields must be provided; the backend replaces the entire task.
 *
 * @param id   - Numeric ID of the task to update.
 * @param task - New values for the task fields.
 * @returns The updated Task object as returned by the backend.
 */
export const updateTask = async (
  id: number,
  task: {
    title: string;
    description?: string;
    priority: Priority;
    dueDate?: string;
    completed: boolean;
  }
): Promise<Task> => {
  const response = await api.put(`/tasks/${id}`, task);
  return response.data;
};

/**
 * Creates a new task. The `completed` flag is always set to false on creation.
 *
 * @param task - Title, optional description, priority, and optional due date.
 * @returns The newly created Task object returned by the backend.
 */
export const createTask = async (task: {
  title: string;
  description?: string;
  priority: Priority;
  dueDate?: string;
}): Promise<Task> => {
  const response = await api.post("/tasks", {
    ...task,
    completed: false,
  });
  return response.data;
};