package com.ibms.transaction_service.service;

import com.ibms.transaction_service.dto.CreateAccountRequest;
import com.ibms.transaction_service.entity.Account;
import com.ibms.transaction_service.feign.AuthClient;
import com.ibms.transaction_service.repository.AccountRepository;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthClient authClient;

    public AccountService(AccountRepository accountRepository, AuthClient authClient) {
        this.accountRepository = accountRepository;
        this.authClient = authClient;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        try {
            authClient.getUser(request.userId());
        } catch (FeignException.NotFound exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", exception);
        } catch (RetryableException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Auth service is unavailable", exception);
        }
        Account account = new Account();
        account.setUserId(request.userId());
        account.setAccountNumber("IBMS-" + UUID.randomUUID().toString().replace("-", ""));
        account.setBalance(BigDecimal.ZERO.setScale(2));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
