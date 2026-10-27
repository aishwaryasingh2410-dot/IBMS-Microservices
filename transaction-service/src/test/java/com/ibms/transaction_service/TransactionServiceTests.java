package com.ibms.transaction_service;

import com.ibms.transaction_service.dto.TransactionRequest;
import com.ibms.transaction_service.entity.Account;
import com.ibms.transaction_service.entity.Transaction;
import com.ibms.transaction_service.repository.AccountRepository;
import com.ibms.transaction_service.repository.TransactionRepository;
import com.ibms.transaction_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTests {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionService(accountRepository, transactionRepository);
    }

    @Test
    void depositUpdatesBalanceAndRecordsTransaction() {
        Account account = account(1L, "100.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = transactionService.deposit(
                new TransactionRequest(1L, null, new BigDecimal("25.50")));

        assertEquals(new BigDecimal("125.50"), account.getBalance());
        assertEquals("DEPOSIT", transaction.getType());
        assertEquals(new BigDecimal("25.50"), transaction.getAmount());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawalRejectsInsufficientFundsWithoutSaving() {
        Account account = account(1L, "10.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.withdraw(
                        new TransactionRequest(1L, null, new BigDecimal("10.01"))));

        assertEquals(422, exception.getStatusCode().value());
        assertEquals(new BigDecimal("10.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferMovesFundsAndStoresDestination() {
        Account source = account(1L, "80.00");
        Account destination = account(2L, "20.00");
        when(accountRepository.findAllByIdForUpdate(List.of(1L, 2L)))
                .thenReturn(List.of(source, destination));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = transactionService.transfer(
                new TransactionRequest(1L, 2L, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("50.00"), source.getBalance());
        assertEquals(new BigDecimal("50.00"), destination.getBalance());
        assertEquals("TRANSFER", transaction.getType());
        assertEquals(2L, transaction.getDestinationAccountId());
        verify(accountRepository).save(source);
        verify(accountRepository).save(destination);
    }

    private static Account account(Long id, String balance) {
        Account account = new Account();
        org.springframework.test.util.ReflectionTestUtils.setField(account, "id", id);
        account.setBalance(new BigDecimal(balance));
        return account;
    }
}
