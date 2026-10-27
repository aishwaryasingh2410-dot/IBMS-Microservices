package com.ibms.transaction_service.config;

import com.ibms.transaction_service.entity.Account;
import com.ibms.transaction_service.entity.Transaction;
import com.ibms.transaction_service.repository.AccountRepository;
import com.ibms.transaction_service.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DemoDataSeeder(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            Account account = new Account();
            account.setUserId(1L);
            account.setAccountNumber("IBMS-DEMO-0001");
            account.setBalance(new BigDecimal("250.00"));
            accountRepository.save(account);
        }

        if (transactionRepository.count() == 0) {
            Transaction transaction = new Transaction();
            transaction.setAccountId(1L);
            transaction.setType("DEPOSIT");
            transaction.setAmount(new BigDecimal("250.00"));
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
        }
    }
}
