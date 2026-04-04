package com.fintech.fintech_service.service;

import com.fintech.fintech_service.dto.transaction.TransactionRequest;
import com.fintech.fintech_service.dto.transaction.TransactionResponse;
import com.fintech.fintech_service.entity.Transaction;
import com.fintech.fintech_service.entity.User;
import com.fintech.fintech_service.enums.TransactionType;
import com.fintech.fintech_service.exception.ResourceNotFoundException;
import com.fintech.fintech_service.repository.TransactionRepository;
import com.fintech.fintech_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    @Transactional
    public TransactionResponse create(TransactionRequest req,String mobile) {

        // 1. Validate user exists
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with mobile number: " + mobile
                ));

        // 2. Get current balance
        BigDecimal currentBalance = transactionRepository.getUserBalance(user.getId());

        // 3. Convert type
        TransactionType type = TransactionType.valueOf(req.getType().toUpperCase());

        // 4. Validate EXPENSE
        if (type == TransactionType.EXPENSE) {
            if (currentBalance.compareTo(req.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient balance for this expense");
            }
        }

        // 5. Create transaction
        Transaction t = new Transaction();
        t.setAmount(req.getAmount());
        t.setType(type);
        t.setCategory(req.getCategory());
        t.setDate(LocalDate.now());
        t.setNotes(req.getNotes());
        t.setCreatedBy(user);

        // 6. Save
        return toResponse(transactionRepository.save(t));
    }

    public Page<TransactionResponse> getAll(String type, String category,
                                            LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> resultPage;

        if (type != null) {
            resultPage = transactionRepository.findByTypeAndDeletedFalse(TransactionType.valueOf(type.toUpperCase()), pageable);
        } else if (category != null) {
            resultPage = transactionRepository.findByCategoryAndDeletedFalse(category, pageable);
        } else if (from != null && to != null) {
            resultPage = transactionRepository.findByDateBetweenAndDeletedFalse(from, to, pageable);
        } else {
            resultPage = transactionRepository.findByDeletedFalse(pageable);
        }

        return resultPage.map(this::toResponse); // Maps Page<Transaction> -> Page<TransactionResponse>
    }


    public @Nullable TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id
                ));
        return  toResponse(transaction);
    }

    public List<TransactionResponse> getByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        return transactionRepository.findByCreatedByIdAndDeletedFalse(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TransactionResponse> getMyTransactionsByDate(String mobile, LocalDate fromDate, LocalDate toDate) {
        // Find user by mobile
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + mobile));

        // Fetch transactions for user between fromDate and toDate
        List<Transaction> transactions = transactionRepository.findByUserAndDateRange(user.getId(), fromDate, toDate);

        // Map to TransactionResponse DTO
        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }



    public void delete(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with Id "+id));
        t.setDeleted(true);
        transactionRepository.save(t);
    }



    public TransactionResponse update(Long transactionId, @Valid TransactionRequest req) {


        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        transaction.setAmount(req.getAmount());
        transaction.setType(TransactionType.valueOf(req.getType())); // assuming enum
        transaction.setCategory(req.getCategory());
        transaction.setDate(LocalDate.now());
        transaction.setNotes(req.getNotes());

        // save updated entity
        Transaction updated = transactionRepository.save(transaction);

        // map to response DTO
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(updated.getTransactionId());
        response.setAmount(updated.getAmount());
        response.setType(updated.getType().name());
        response.setCategory(updated.getCategory());
        response.setDate(updated.getDate());
        response.setNotes(updated.getNotes());
        response.setUserId(updated.getCreatedBy() != null ? updated.getCreatedBy().getId() : null);
        response.setCreatedAt(updated.getCreatedAt());

        return response;
    }
    private TransactionResponse toResponse(Transaction t) {

        TransactionResponse response=new TransactionResponse();
        response.setTransactionId(t.getTransactionId());
        response.setAmount(t.getAmount());
        response.setType(String.valueOf(t.getType()));
        response.setCategory(t.getCategory());
        response.setDate(t.getDate());
        response.setNotes(t.getNotes());
        response.setUserId(t.getCreatedBy().getId());
        response.setCreatedAt(t.getCreatedAt());
        return response;
    }

    public List<TransactionResponse> getMyTransactions(String mobile) {

        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByCreatedByIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }
}