package com.ibms.loan_service.config;

import com.ibms.loan_service.entity.LoanApplication;
import com.ibms.loan_service.repository.LoanApplicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final LoanApplicationRepository loanApplicationRepository;

    public DemoDataSeeder(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    @Override
    public void run(String... args) {
        if (loanApplicationRepository.count() > 0) {
            return;
        }

        LoanApplication loan = new LoanApplication();
        loan.setUserId(1L);
        loan.setAmount(5000.00);
        loan.setStatus("PENDING");
        loanApplicationRepository.save(loan);
    }
}
