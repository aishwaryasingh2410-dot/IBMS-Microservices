package com.ibms.transaction_service.feign;

import com.ibms.transaction_service.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${services.auth.url}")
public interface AuthClient {

    @GetMapping("/auth/users/{id}")
    UserDto getUser(@PathVariable Long id);
}