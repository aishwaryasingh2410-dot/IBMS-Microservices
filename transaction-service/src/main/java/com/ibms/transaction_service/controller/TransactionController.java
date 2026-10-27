package com.ibms.transaction_service.controller;

import com.ibms.transaction_service.entity.Transaction;
import com.ibms.transaction_service.dto.TransactionRequest;
import com.ibms.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public Transaction deposit(@Valid @RequestBody TransactionRequest request) {
        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    public Transaction withdraw(@Valid @RequestBody TransactionRequest request) {
        return transactionService.withdraw(request);
    }

    @PostMapping("/transfer")
    public Transaction transfer(@Valid @RequestBody TransactionRequest request) {
        return transactionService.transfer(request);
    }

    @GetMapping("/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }
}