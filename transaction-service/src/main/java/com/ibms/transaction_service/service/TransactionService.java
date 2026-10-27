package com.ibms.transaction_service.service;

import com.ibms.transaction_service.dto.TransactionRequest;
import com.ibms.transaction_service.entity.Account;
import com.ibms.transaction_service.entity.Transaction;
import com.ibms.transaction_service.repository.AccountRepository;
import com.ibms.transaction_service.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction deposit(TransactionRequest request) {
        requirePositiveAmount(request.amount());
        Account account = lockAccount(request.accountId());
        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);
        return saveTransaction(request, "DEPOSIT");
    }

    @Transactional
    public Transaction withdraw(TransactionRequest request) {
        requirePositiveAmount(request.amount());
        Account account = lockAccount(request.accountId());
        ensureSufficientFunds(account, request.amount());
        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);
        return saveTransaction(request, "WITHDRAW");
    }

    @Transactional
    public Transaction transfer(TransactionRequest request) {
        requirePositiveAmount(request.amount());
        if (request.destinationAccountId() == null || request.destinationAccountId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A destination account is required");
        }
        if (request.accountId().equals(request.destinationAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination accounts must differ");
        }

        Map<Long, Account> accounts = accountRepository.findAllByIdForUpdate(
                        java.util.List.of(request.accountId(), request.destinationAccountId()))
                .stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        Account source = accounts.get(request.accountId());
        Account destination = accounts.get(request.destinationAccountId());
        if (source == null || destination == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source or destination account not found");
        }
        ensureSufficientFunds(source, request.amount());

        source.setBalance(source.getBalance().subtract(request.amount()));
        destination.setBalance(destination.getBalance().add(request.amount()));
        accountRepository.save(source);
        accountRepository.save(destination);
        return saveTransaction(request, "TRANSFER");
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private Account lockAccount(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must have at most two decimal places");
        }
    }

    private static void ensureSufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }
    }

    private Transaction saveTransaction(TransactionRequest request, String type) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.accountId());
        transaction.setDestinationAccountId(
                "TRANSFER".equals(type) ? request.destinationAccountId() : null);
        transaction.setType(type);
        transaction.setAmount(request.amount());
        transaction.setStatus("SUCCESS");
        return transactionRepository.save(transaction);
    }
}
