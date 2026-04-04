package com.fintech.fintech_service.repository;

import com.fintech.fintech_service.entity.Transaction;
import com.fintech.fintech_service.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCreatedByIdAndDeletedFalse(Long userId);


    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.deleted = false GROUP BY t.category")
    List<Object[]> sumByCategory();

    @Query("SELECT t FROM Transaction t WHERE t.createdBy.id = :userId AND t.date BETWEEN :fromDate AND :toDate AND t.deleted = false ORDER BY t.date DESC")
    List<Transaction> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // Add Pageable overloads
    Page<Transaction> findByTypeAndDeletedFalse(TransactionType type, Pageable pageable);
    Page<Transaction> findByCategoryAndDeletedFalse( String category, Pageable pageable);
    Page<Transaction> findByDateBetweenAndDeletedFalse( LocalDate from, LocalDate to, Pageable pageable);
    Page<Transaction> findByDeletedFalse(Pageable pageable);


    @Query("""
    SELECT YEAR(t.date), MONTH(t.date),
           SUM(CASE WHEN t.type = 'INCOME'  THEN t.amount ELSE 0 END),
           SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)
    FROM Transaction t
    WHERE t.deleted = false
    GROUP BY YEAR(t.date), MONTH(t.date)
    ORDER BY YEAR(t.date) DESC, MONTH(t.date) DESC
    """)
    List<Object[]> findMonthlyTrends();

    @Query("""
    SELECT COALESCE(SUM(
        CASE 
            WHEN t.type = 'INCOME' THEN t.amount 
            WHEN t.type = 'EXPENSE' THEN -t.amount 
        END
    ), 0)
    FROM Transaction t
    WHERE t.createdBy.id = :userId AND t.deleted = false
""")
    BigDecimal getUserBalance(Long userId);

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.deleted = false
    AND (:type IS NULL OR t.type = :type)
    AND (:category IS NULL OR t.category = :category)
    AND (:from IS NULL OR t.date >= :from)
    AND (:to IS NULL OR t.date <= :to)
    ORDER BY t.date DESC
    """)
    List<Transaction> findWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // ─── Per-user queries ───────────────────────────────────────────

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.deleted = false AND t.createdBy.id = :userId")
    BigDecimal sumByTypeAndUser(@Param("type") TransactionType type, @Param("userId") Long userId);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.deleted = false AND t.createdBy.id = :userId GROUP BY t.category")
    List<Object[]> sumByCategoryAndUser(@Param("userId") Long userId);

    @Query("""
    SELECT YEAR(t.date), MONTH(t.date),
           SUM(CASE WHEN t.type = 'INCOME'  THEN t.amount ELSE 0 END),
           SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)
    FROM Transaction t
    WHERE t.deleted = false AND t.createdBy.id = :userId
    GROUP BY YEAR(t.date), MONTH(t.date)
    ORDER BY YEAR(t.date) DESC, MONTH(t.date) DESC
    """)
    List<Object[]> findMonthlyTrendsByUser(@Param("userId") Long userId);

    List<Transaction> findTop10ByCreatedByIdAndDeletedFalseOrderByDateDesc(Long id);


}
