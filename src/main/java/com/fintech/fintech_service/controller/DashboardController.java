package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.dashboard.DashboardSummary;
import com.fintech.fintech_service.dto.dashboard.ExportRequest;
import com.fintech.fintech_service.dto.dashboard.MonthlyTrend;
import com.fintech.fintech_service.dto.transaction.TransactionResponse;
import com.fintech.fintech_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    //Over All
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<DashboardSummary> getOverallSummary() {
        log.info("Fetching overall dashboard summary");
        return ResponseEntity.ok(dashboardService.getOverallSummary());
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getOverallCategory() {
        log.info("Fetching overall category totals");
        return ResponseEntity.ok(dashboardService.getOverallCategoryTotals());
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<List<MonthlyTrend>> getOverallMonthlyTrends() {
        log.info("Fetching overall monthly trends");
        return ResponseEntity.ok(dashboardService.getOverallMonthlyTrends());
    }

    @GetMapping("/summary/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<DashboardSummary> getSummaryByUser(@PathVariable Long id) {
        log.info("Admin fetching summary for user id: {}", id);
        return ResponseEntity.ok(dashboardService.getSummaryByUser(id));
    }

    //User
    @GetMapping("/categories/{userId}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getCategoriesByUser(@PathVariable Long userId) {
        log.info("Admin fetching categories for user id: {}", userId);
        return ResponseEntity.ok(dashboardService.getCategoryTotalsByUser(userId));
    }

    @GetMapping("/monthly/{userId}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrendByUser(@PathVariable Long userId) {
        log.info("Admin fetching trends for user id: {}", userId);
        return ResponseEntity.ok(dashboardService.getMonthlyTrendsByUser(userId));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<DashboardSummary> getMySummary(Authentication authentication) {
        log.info("Fetching personal summary for logged-in user");
        String mobile = authentication.getName();
        return ResponseEntity.ok(dashboardService.getSummaryByMobile(mobile));
    }

    //Current Logged User
    @GetMapping("/my-categories")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getMyCategories(Authentication authentication) {
        log.info("Fetching personal category totals for logged-in user");
        String mobile = authentication.getName();
        return ResponseEntity.ok(dashboardService.getCategoryTotalsByMobile(mobile));
    }

    //Current Logged User
    @GetMapping("/my-trends")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<List<MonthlyTrend>> getMyTrends(Authentication authentication) {
        log.info("Fetching personal monthly trends for logged-in user");
        String mobile = authentication.getName();
        return ResponseEntity.ok(dashboardService.getMonthlyTrendsByMobile(mobile));
    }

    //Current Logged User
    @GetMapping("/my-recent")
    @PreAuthorize("hasAnyRole('USER','ANALYST','ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getMyRecent(Authentication authentication) {
        log.info("Fetching last 10 transactions for logged-in user");
        String mobile = authentication.getName();
        return ResponseEntity.ok(dashboardService.getRecentByMobile(mobile));
    }

   //EXPORT CSV FILE
    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<byte[]> exportCsv(@RequestBody(required = false) ExportRequest req) {
    if (req == null) req = new ExportRequest(); // allow empty body = no filters

    byte[] csvBytes = dashboardService.exportToCsv(req);

    String filename = "transactions_" + LocalDate.now() + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(csvBytes.length);

    return ResponseEntity.ok()
            .headers(headers)
            .body(csvBytes);
}
}
