package com.ibms.auth_service.repository;

import com.ibms.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    java.util.Optional<User> findByEmail(String email);
}
