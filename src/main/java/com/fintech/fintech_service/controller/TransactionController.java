package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.transaction.TransactionRequest;
import com.fintech.fintech_service.dto.transaction.TransactionResponse;
import com.fintech.fintech_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);


    private final TransactionService transactionService;


    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest req, Authentication authentication) {
        log.info("Creating transaction - type={}, amount={}, category={}",
                req.getType(), req.getAmount(), req.getCategory());
        // Extract the mobile from the JWT/Security context
        String mobile = authentication.getName();
        TransactionResponse response = transactionService.create(req, mobile);
        log.info("Transaction created with id: {}", response.getTransactionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all transactions ");
        Page<TransactionResponse> result = transactionService.getAll(type, category, from, to, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        log.info("Fetching transaction id: {}", id);
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST','ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getByUserId(@PathVariable("id") Long userId) {
        log.info("Fetching transactions for user id: {}", userId);
        return ResponseEntity.ok(transactionService.getByUserId(userId));
    }



    //Current Logged User
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> getMyTransactions(Authentication authentication) {
        log.info("Fetching transactions for logged-in user");
        String mobile = authentication.getName();
        List<TransactionResponse> myTransactions = transactionService.getMyTransactions(mobile);

        return ResponseEntity.ok(Map.of(
                "message", myTransactions.isEmpty()
                        ? "No transactions found yet"
                        : "Transactions fetched successfully",
                "data", myTransactions
        ));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> getMyTransactionsByDate(
            Authentication authentication,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        String mobile = authentication.getName();
        log.info("Date-range transaction request — mobile: {}, fromDate: {}, toDate: {}",
                mobile, fromDate, toDate);

        // If toDate not provided, default to today
        if (toDate == null) {
            toDate = LocalDate.now();
            log.debug("toDate not provided — defaulting to today: {}", toDate);
        }

        List<TransactionResponse> myTransactions =
                transactionService.getMyTransactionsByDate(mobile, fromDate, toDate);

        if (myTransactions.isEmpty()) {
            log.info("No transactions found in date range — mobile: {}, from: {}, to: {}",
                    mobile, fromDate, toDate);
        } else {
            log.info("Transactions fetched — mobile: {}, from: {}, to: {}, count: {}",
                    mobile, fromDate, toDate, myTransactions.size());
        }

        return ResponseEntity.ok(Map.of(
                "message", myTransactions.isEmpty()
                        ? "No transactions found in the given date range"
                        : "Transactions fetched successfully",
                "data", myTransactions
        ));
    }

    @PutMapping("/{id}") // This 'id'
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody TransactionRequest req) {
        log.info("Updating transaction id: {}", transactionId);
        return ResponseEntity.ok(transactionService.update(transactionId, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("Soft deleting transaction id: {}", id);
        transactionService.delete(id);
        log.info("Transaction soft deleted: {}", id);
        return ResponseEntity.noContent().build();
    }
}