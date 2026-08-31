package com.ibms.transaction_service.controller;

import com.ibms.transaction_service.entity.Transaction;
import com.ibms.transaction_service.repository.TransactionRepository;
import com.ibms.transaction_service.feign.AuthClient;
import com.ibms.transaction_service.dto.UserDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AuthClient authClient;

    public TransactionController(TransactionRepository transactionRepository,
                                 AuthClient authClient) {
        this.transactionRepository = transactionRepository;
        this.authClient = authClient;
    }

    @PostMapping("/deposit")
    public Transaction deposit(@RequestBody Transaction transaction) {

        UserDto user = authClient.getUser(transaction.getAccountId());

        transaction.setType("DEPOSIT");
        transaction.setStatus("SUCCESS");

        return transactionRepository.save(transaction);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@RequestBody Transaction transaction) {

        UserDto user = authClient.getUser(transaction.getAccountId());

        transaction.setType("WITHDRAW");
        transaction.setStatus("SUCCESS");

        return transactionRepository.save(transaction);
    }

    @PostMapping("/transfer")
    public Transaction transfer(@RequestBody Transaction transaction) {

        UserDto user = authClient.getUser(transaction.getAccountId());

        transaction.setType("TRANSFER");
        transaction.setStatus("SUCCESS");

        return transactionRepository.save(transaction);
    }

    @GetMapping("/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionRepository.findById(id).orElse(null);
    }
}