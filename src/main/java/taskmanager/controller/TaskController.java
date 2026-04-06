package com.almudena.taskmanager.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.almudena.taskmanager.model.Task;
import com.almudena.taskmanager.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Task create(@RequestBody Task task) {
        System.out.println("Recibido task: " + task.getTitle() + ", completed=" + task.isCompleted());
        return service.create(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @RequestBody Task task) {
        return service.update(id, task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}