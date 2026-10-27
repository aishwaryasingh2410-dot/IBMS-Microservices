package com.ibms.notification_service.controller;

import com.ibms.notification_service.entity.Notification;
import com.ibms.notification_service.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        return repository.save(notification);
    }

    @GetMapping("/{userId}")
    public java.util.List<Notification> getByUser(@PathVariable Long userId) {
        return repository.findAll()
                .stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();
    }
}