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

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repo;
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
        BigDecimal currentBalance = repo.getUserBalance(user.getId());

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
        return toResponse(repo.save(t));
    }

    public Page<TransactionResponse> getAll(String type, String category,
                                            LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> resultPage;

        if (type != null) {
            resultPage = repo.findByTypeAndDeletedFalse(TransactionType.valueOf(type.toUpperCase()), pageable);
        } else if (category != null) {
            resultPage = repo.findByCategoryAndDeletedFalse(category, pageable);
        } else if (from != null && to != null) {
            resultPage = repo.findByDateBetweenAndDeletedFalse(from, to, pageable);
        } else {
            resultPage = repo.findByDeletedFalse(pageable);
        }

        return resultPage.map(this::toResponse); // Maps Page<Transaction> -> Page<TransactionResponse>
    }


    public @Nullable TransactionResponse getById(Long id) {
        Transaction transaction = repo.findById(id)
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

        return repo.findByCreatedByIdAndDeletedFalse(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }



    public void delete(Long id) {
        Transaction t = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with Id "+id));
        t.setDeleted(true);
        repo.save(t);
    }



    public TransactionResponse update(Long transaction_id, @Valid TransactionRequest req) {


        Transaction transaction = repo.findById(transaction_id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transaction_id));

        transaction.setAmount(req.getAmount());
        transaction.setType(TransactionType.valueOf(req.getType())); // assuming enum
        transaction.setCategory(req.getCategory());
        transaction.setDate(LocalDate.now());
        transaction.setNotes(req.getNotes());

        // save updated entity
        Transaction updated = repo.save(transaction);

        // map to response DTO
        TransactionResponse response = new TransactionResponse();
        response.setTransaction_id(updated.getTransaction_id());
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
        response.setTransaction_id(t.getTransaction_id());
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

        return repo.findByCreatedByIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }
}