package com.fintech.fintech_service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTrend {
    private int year;
    private int month;
    private String monthName;       // e.g. "January"
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal net;

    public void setMonthName(String displayName) {
    }
}
