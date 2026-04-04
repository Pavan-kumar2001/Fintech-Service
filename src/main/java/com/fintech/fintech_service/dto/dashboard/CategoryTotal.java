package com.fintech.fintech_service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTotal {
    private String category;
    private BigDecimal total;
    private String type;   // "INCOME" or "EXPENSE"
}