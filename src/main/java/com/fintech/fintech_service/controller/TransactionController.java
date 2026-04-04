package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.transaction.TransactionRequest;
import com.fintech.fintech_service.dto.transaction.TransactionResponse;
import com.fintech.fintech_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest req, Authentication authentication) {
        // Extract the mobile from the JWT/Security context
        String mobile = authentication.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(req, mobile));
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

        Page<TransactionResponse> result = transactionService.getAll(type, category, from, to, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST','ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getByUserId(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(transactionService.getByUserId(userId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> getMyTransactions(Authentication authentication) {

        String mobile = authentication.getName();
        List<TransactionResponse> myTransactions = transactionService.getMyTransactions(mobile);

        return ResponseEntity.ok(Map.of(
                "message", myTransactions.isEmpty()
                        ? "No transactions found yet"
                        : "Transactions fetched successfully",
                "data", myTransactions
        ));
    }

    @PutMapping("/{id}") // This 'id'
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable("id") Long transaction_id, // Must map 'id' to 'transaction_id'
            @Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(transactionService.update(transaction_id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}