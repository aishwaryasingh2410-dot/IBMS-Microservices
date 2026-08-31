package com.ibms.loan_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "notification-service",
        url = "http://localhost:8083"
)
public interface NotificationClient {

    @PostMapping("/notifications")
    void sendNotification(@RequestBody NotificationRequest request);

    class NotificationRequest {

        private Long userId;
        private String message;
        private String type;

        public NotificationRequest() {}

        public NotificationRequest(Long userId, String message, String type) {
            this.userId = userId;
            this.message = message;
            this.type = type;
        }

        public Long getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }
    }
}