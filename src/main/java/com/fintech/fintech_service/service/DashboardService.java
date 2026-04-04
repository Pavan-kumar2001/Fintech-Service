package com.fintech.fintech_service.service;

import com.fintech.fintech_service.dto.dashboard.DashboardSummary;
import com.fintech.fintech_service.dto.dashboard.ExportRequest;
import com.fintech.fintech_service.dto.dashboard.MonthlyTrend;
import com.fintech.fintech_service.dto.transaction.TransactionResponse;
import com.fintech.fintech_service.entity.Transaction;
import com.fintech.fintech_service.entity.User;
import com.fintech.fintech_service.enums.TransactionType;
import com.fintech.fintech_service.exception.ResourceNotFoundException;
import com.fintech.fintech_service.repository.TransactionRepository;
import com.fintech.fintech_service.repository.UserRepository;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public DashboardSummary getOverallSummary() {
        BigDecimal income  = orZero(transactionRepo.sumByType(TransactionType.INCOME));
        BigDecimal expense = orZero(transactionRepo.sumByType(TransactionType.EXPENSE));
        return new DashboardSummary(income, expense, income.subtract(expense));
    }

    public Map<String, BigDecimal> getOverallCategoryTotals() {
        return transactionRepo.sumByCategory().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    public List<MonthlyTrend> getOverallMonthlyTrends() {
        return transactionRepo.findMonthlyTrends().stream()
                .map(r -> toMonthlyTrend(r))
                .toList();
    }

    public List<TransactionResponse> getRecentTen(@PathVariable Long id) {
        return transactionRepo.findTop10ByCreatedByIdAndDeletedFalseOrderByDateDesc(id)
                .stream().map(this::toResponse).toList();
    }

    // ─────────────────────────────────────────────
    // INDIVIDUAL (user sees only their own data)
    // ─────────────────────────────────────────────

    public DashboardSummary getSummaryByMobile(String mobile) {
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal income = orZero(transactionRepo.sumByTypeAndUser(TransactionType.INCOME, user.getId()));
        BigDecimal expense = orZero(transactionRepo.sumByTypeAndUser(TransactionType.EXPENSE, user.getId()));

        return new DashboardSummary(income, expense, income.subtract(expense));
    }

    public Map<String, BigDecimal> getCategoryTotalsByMobile(String mobile) {
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepo.sumByCategoryAndUser(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    public List<MonthlyTrend> getMonthlyTrendsByMobile(String mobile) {
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepo.findMonthlyTrendsByUser(user.getId())
                .stream()
                .map(this::toMonthlyTrend)
                .toList();
    }

    public List<TransactionResponse> getRecentByMobile(String mobile) {
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepo
                .findTop10ByCreatedByIdAndDeletedFalseOrderByDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }
    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private void validateUser(Long userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private MonthlyTrend toMonthlyTrend(Object[] row) {
        int year     = ((Number) row[0]).intValue();
        int month    = ((Number) row[1]).intValue();
        BigDecimal income  = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        BigDecimal expense = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
        return new MonthlyTrend(
                year, month,
                Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                income, expense,
                income.subtract(expense)
        );
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setTransaction_id(t.getTransaction_id());
        r.setAmount(t.getAmount());
        r.setType(t.getType().name());
        r.setCategory(t.getCategory());
        r.setDate(t.getDate());
        r.setNotes(t.getNotes());
        r.setUserId(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null);
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }


    public byte[] exportToCsv(ExportRequest req) {
        TransactionType type = null;
        if (req.getType() != null) {
            type = TransactionType.valueOf(req.getType().toUpperCase());
        }

        List<Transaction> transactions = transactionRepo.findWithFilters(
                type,
                req.getCategory(),
                req.getFrom(),
                req.getTo()
        );

        StringBuilder csv = new StringBuilder();

        // Header row
        csv.append("transaction_id,amount,type,category,date,notes,created_by,created_at\n");

        // Data rows
        for (Transaction t : transactions) {
            csv.append(t.getTransaction_id()).append(",")
                    .append(t.getAmount()).append(",")
                    .append(t.getType().name()).append(",")
                    .append(escapeCsv(t.getCategory())).append(",")
                    .append(t.getDate()).append(",")
                    .append(escapeCsv(t.getNotes())).append(",")
                    .append(t.getCreatedBy() != null ? t.getCreatedBy().getUsername() : "").append(",")
                    .append(t.getCreatedAt())
                    .append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Wraps fields in quotes and escapes inner quotes to prevent CSV injection
    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public @Nullable DashboardSummary getSummaryByUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: "+id));

        BigDecimal income = orZero(transactionRepo.sumByTypeAndUser(TransactionType.INCOME, user.getId()));
        BigDecimal expense = orZero(transactionRepo.sumByTypeAndUser(TransactionType.EXPENSE, user.getId()));

        return new DashboardSummary(income, expense, income.subtract(expense));
    }

    public Map<String, BigDecimal> getCategoryTotalsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepo.sumByCategoryAndUser(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }

    public List<MonthlyTrend> getMonthlyTrendsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepo.findMonthlyTrendsByUser(user.getId())
                .stream()
                .map(this::toMonthlyTrend)
                .toList();
    }
}