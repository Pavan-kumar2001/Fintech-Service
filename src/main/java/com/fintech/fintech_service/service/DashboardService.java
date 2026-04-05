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
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public DashboardSummary getOverallSummary() {
        log.info("Fetching overall dashboard summary");
        BigDecimal income  = orZero(transactionRepo.sumByType(TransactionType.INCOME));
        BigDecimal expense = orZero(transactionRepo.sumByType(TransactionType.EXPENSE));
        DashboardSummary dashboardSummary = new DashboardSummary(income, expense, income.subtract(expense));
        log.info("Overall summary — income: {}, expense: {}, net: {}",
                income, expense, dashboardSummary.getNetBalance());
        return dashboardSummary ;
    }

    public Map<String, BigDecimal> getOverallCategoryTotals() {
        log.info("Fetching overall category totals");
        Map<String, BigDecimal> result = transactionRepo.sumByCategory().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
        log.info("Overall category totals fetched — categories count: {}", result.size());
        return result;
    }

    public List<MonthlyTrend> getOverallMonthlyTrends() {
        log.info("Fetching overall monthly trends");
        List<MonthlyTrend> trends  = transactionRepo.findMonthlyTrends().stream()
                .map(r -> toMonthlyTrend(r))
                .toList();
        log.info("Overall monthly trends fetched — months count: {}", trends.size());
        return trends;
    }

    public List<TransactionResponse> getRecentTen(@PathVariable Long id) {
        log.info("Fetching recent 10 transactions for user id: {}", id);
        List<TransactionResponse> result = transactionRepo.findTop10ByCreatedByIdAndDeletedFalseOrderByDateDesc(id)
                .stream().map(this::toResponse).toList();
        log.info("Recent transactions fetched for user id: {} — count: {}", id, result.size());
        return result;
    }

    // ─────────────────────────────────────────────
    // INDIVIDUAL (user sees only their own data)
    // ─────────────────────────────────────────────

    public DashboardSummary getSummaryByMobile(String mobile) {
        log.debug("Fetching personal summary for mobile: {}", mobile);
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.warn("User not found for personal summary — mobile: {}", mobile);
                    return new ResourceNotFoundException("User not found");});

        BigDecimal income = orZero(transactionRepo.sumByTypeAndUser(TransactionType.INCOME, user.getId()));
        BigDecimal expense = orZero(transactionRepo.sumByTypeAndUser(TransactionType.EXPENSE, user.getId()));
        DashboardSummary summary = new DashboardSummary(income, expense, income.subtract(expense));
        log.info("Personal summary fetched — user id: {}, income: {}, expense: {}, net: {}",
                user.getId(), income, expense, summary.getNetBalance());
        return summary;
    }

    public Map<String, BigDecimal> getCategoryTotalsByMobile(String mobile) {
        log.debug("Fetching personal category totals for mobile: {}", mobile);
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.warn("User not found for personal categories — mobile: {}", mobile);
                    return new ResourceNotFoundException("User not found");
                });

        Map<String, BigDecimal> result = transactionRepo.sumByCategoryAndUser(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
        log.info("Personal category totals fetched — user id: {}, categories: {}",
                user.getId(), result.size());
        return result;
    }

    public List<MonthlyTrend> getMonthlyTrendsByMobile(String mobile) {
        log.debug("Fetching personal monthly trends for mobile: {}", mobile);
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.warn("User not found for personal trends — mobile: {}", mobile);
                    return new ResourceNotFoundException("User not found");
                });

        List<MonthlyTrend> trends = transactionRepo.findMonthlyTrendsByUser(user.getId())
                .stream()
                .map(this::toMonthlyTrend)
                .toList();
        log.info("Personal monthly trends fetched — user id: {}, months: {}",
                user.getId(), trends.size());
        return trends;
    }

    public List<TransactionResponse> getRecentByMobile(String mobile) {
        log.debug("Fetching recent 10 transactions for mobile: {}", mobile);
        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() ->{
                    log.warn("User not found for recent transactions — mobile: {}", mobile);
                      return   new ResourceNotFoundException("User not found");});

        List<TransactionResponse> result = transactionRepo
                .findTop10ByCreatedByIdAndDeletedFalseOrderByDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        log.info("Recent 10 transactions fetched — user id: {}, count: {}",
                user.getId(), result.size());
        return result;
    }



    public @Nullable DashboardSummary getSummaryByUser(Long id) {
        log.debug("Admin fetching summary for user id: {}", id);
        User user = userRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for summary — id: {}", id);
                    return new ResourceNotFoundException("User not found with Id: "+id);
                });

        BigDecimal income = orZero(transactionRepo.sumByTypeAndUser(TransactionType.INCOME, user.getId()));
        BigDecimal expense = orZero(transactionRepo.sumByTypeAndUser(TransactionType.EXPENSE, user.getId()));
        DashboardSummary summary = new DashboardSummary(income, expense, income.subtract(expense));
        log.info("Summary fetched for user id: {} — income: {}, expense: {}, net: {}",
                id, income, expense, summary.getNetBalance());
        return summary;
    }

    public Map<String, BigDecimal> getCategoryTotalsByUser(Long userId) {
        log.debug("Admin fetching category totals for user id: {}", userId);
        User user = userRepo.findById(userId)
                .orElseThrow(() ->{
                    log.warn("User not found for category totals — id: {}", userId);
                    return new ResourceNotFoundException("User not found");});

        Map<String, BigDecimal> result = transactionRepo.sumByCategoryAndUser(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (BigDecimal) r[1]
                ));
        log.info("Category totals fetched for user id: {} — categories: {}",
                userId, result.size());
        return result;
    }

    public List<MonthlyTrend> getMonthlyTrendsByUser(Long userId) {
        log.debug("Admin fetching monthly trends for user id: {}", userId);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for monthly trends — id: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        List<MonthlyTrend> trends = transactionRepo.findMonthlyTrendsByUser(user.getId())
                .stream()
                .map(this::toMonthlyTrend)
                .toList();

        log.info("Monthly trends fetched for user id: {} — months: {}",
                userId, trends.size());
        return trends;

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
            csv.append(t.getTransactionId()).append(",")
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

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

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
        r.setTransactionId(t.getTransactionId());
        r.setAmount(t.getAmount());
        r.setType(t.getType().name());
        r.setCategory(t.getCategory());
        r.setDate(t.getDate());
        r.setNotes(t.getNotes());
        r.setUserId(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null);
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }

}