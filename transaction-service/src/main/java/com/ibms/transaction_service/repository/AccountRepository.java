package com.ibms.transaction_service.repository;

import com.ibms.transaction_service.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}