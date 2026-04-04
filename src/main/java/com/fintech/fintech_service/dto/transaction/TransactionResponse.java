package com.fintech.fintech_service.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private BigDecimal amount;
    private String type;        // "INCOME" or "EXPENSE"
    private String category;
    private LocalDate date;
    private String notes;
    private Long userId;   // email of the user who created it
    private LocalDateTime createdAt;
}