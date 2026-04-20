/**
 * task.ts
 *
 * Defines the TypeScript interface that represents a Task object
 * as returned by the Spring Boot backend (TaskResponse DTO).
 *
 * Optional fields (marked with ?) may be absent in the API response
 * when the user did not provide them during task creation.
 */

/** The three priority levels supported by the backend. */
export type Priority = "LOW" | "MEDIUM" | "HIGH";

/** A single task as returned by the backend API. */
export interface Task {
  /** Unique identifier assigned by the database. */
  id: number;
  /** Short descriptive name of the task. */
  title: string;
  /** Optional longer description of what the task involves. */
  description?: string;
  /** Whether the task has been marked as done. */
  completed: boolean;
  /** Importance level of the task. */
  priority: Priority;
  /** Optional deadline for the task (ISO-8601 date string). */
  dueDate?: string;
  /** ISO-8601 timestamp of when the task was created. */
  createdAt: string;
  /** ISO-8601 timestamp of the last update to the task. */
  updatedAt: string;
}