package com.ibms.transaction_service;

import com.ibms.transaction_service.dto.CreateAccountRequest;
import com.ibms.transaction_service.entity.Account;
import com.ibms.transaction_service.feign.AuthClient;
import com.ibms.transaction_service.repository.AccountRepository;
import com.ibms.transaction_service.service.AccountService;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTests {

    private AccountRepository accountRepository;
    private AuthClient authClient;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        authClient = mock(AuthClient.class);
        accountService = new AccountService(accountRepository, authClient);
    }

    @Test
    void accountCreationChecksUserAndStartsAtZero() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount(new CreateAccountRequest(7L));

        verify(authClient).getUser(7L);
        assertEquals(7L, account.getUserId());
        assertEquals(BigDecimal.ZERO.setScale(2), account.getBalance());
        assertTrue(account.getAccountNumber().startsWith("IBMS-"));
        assertTrue(account.getAccountNumber().length() <= 40);
    }

    @Test
    void accountCreationReturnsNotFoundWhenAuthDoesNotFindUser() {
        Request request = Request.create(
                Request.HttpMethod.GET, "http://auth-service/auth/users/7", Map.of(),
                null, StandardCharsets.UTF_8);
        when(authClient.getUser(7L)).thenThrow(new FeignException.NotFound(
                "User not found", request, null, Map.of()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccount(new CreateAccountRequest(7L)));

        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(accountRepository);
    }

    @Test
    void accountCreationReturnsServiceUnavailableWhenAuthIsDown() {
        Request request = Request.create(
                Request.HttpMethod.GET, "http://auth-service/auth/users/7", Map.of(),
                null, StandardCharsets.UTF_8);
        when(authClient.getUser(7L)).thenThrow(new RetryableException(
                503, "Connection refused", Request.HttpMethod.GET,
                new java.net.ConnectException("Auth unavailable"), (Long) null, request));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.createAccount(new CreateAccountRequest(7L)));

        assertEquals(503, exception.getStatusCode().value());
        verifyNoInteractions(accountRepository);
    }
}
