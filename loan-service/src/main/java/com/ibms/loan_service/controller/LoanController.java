package com.ibms.loan_service.controller;

import com.ibms.loan_service.entity.LoanApplication;
import com.ibms.loan_service.repository.LoanApplicationRepository;
import com.ibms.loan_service.feign.NotificationClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanApplicationRepository repository;
    private final NotificationClient notificationClient;

    public LoanController(LoanApplicationRepository repository,
                          NotificationClient notificationClient) {
        this.repository = repository;
        this.notificationClient = notificationClient;
    }

    @PostMapping("/apply")
    public LoanApplication apply(@RequestBody LoanApplication loan) {

        loan.setStatus("PENDING");

        LoanApplication saved = repository.save(loan);

        notificationClient.sendNotification(
                new NotificationClient.NotificationRequest(
                        loan.getUserId(),
                        "Loan application submitted successfully",
                        "LOAN"
                )
        );

        return saved;
    }

    @GetMapping("/{id}")
    public LoanApplication getLoan(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }
}