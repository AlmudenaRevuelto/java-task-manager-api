package com.almudena.taskmanager.service;

import org.springframework.stereotype.Service;
import java.util.List;

import com.almudena.taskmanager.model.Task;
import com.almudena.taskmanager.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task create(Task task) {
        return repository.save(task);
    }

    public Task update(Long id, Task task) {
        Task existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        existing.setTitle(task.getTitle());
        existing.setCompleted(task.isCompleted());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}