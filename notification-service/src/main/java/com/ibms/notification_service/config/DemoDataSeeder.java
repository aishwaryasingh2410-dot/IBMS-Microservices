package com.ibms.notification_service.config;

import com.ibms.notification_service.entity.Notification;
import com.ibms.notification_service.repository.NotificationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final NotificationRepository notificationRepository;

    public DemoDataSeeder(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (notificationRepository.count() > 0) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setMessage("Your demo account is ready.");
        notification.setType("ACCOUNT");
        notificationRepository.save(notification);
    }
}
