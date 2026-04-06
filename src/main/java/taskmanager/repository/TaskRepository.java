package com.almudena.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.almudena.taskmanager.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}