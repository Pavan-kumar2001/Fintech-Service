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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    @Transactional
    public TransactionResponse create(TransactionRequest req,String mobile) {
        log.debug("Create transaction request — mobile: {}, type: {}, amount: {}, category: {}",
                mobile, req.getType(), req.getAmount(), req.getCategory());
        // 1. Validate user exists
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() ->{
                    log.warn("Transaction creation failed — user not found for mobile: {}", mobile);
                 return new ResourceNotFoundException("User not found with mobile number: " + mobile);
                });

        // 2. Get current balance
        BigDecimal currentBalance = transactionRepository.getUserBalance(user.getId());
        log.debug("Current balance for user id: {} is {}", user.getId(), currentBalance);
        // 3. Convert type
        TransactionType type = TransactionType.valueOf(req.getType().toUpperCase());

        // 4. Validate EXPENSE
        if (type == TransactionType.EXPENSE) {
            if (currentBalance.compareTo(req.getAmount()) < 0) {
                log.warn("Insufficient balance");
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
        Transaction saved = transactionRepository.save(t);
        log.info("Transaction created — id: {}, type: {}, amount: {}, user id: {}",
                saved.getTransactionId(), saved.getType(), saved.getAmount(), user.getId());
        return toResponse(saved);
    }

    public Page<TransactionResponse> getAll(String type, String category,
                                            LocalDate from, LocalDate to, int page, int size) {
        log.info("Fetching all transactions — type={}, category={}, from={}, to={}, page={}, size={}",
                type, category, from, to, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> resultPage;

        if (type != null) {
            log.debug("Filtering by type: {}", type);
            resultPage = transactionRepository.findByTypeAndDeletedFalse(TransactionType.valueOf(type.toUpperCase()), pageable);
        } else if (category != null) {
            log.debug("Filtering by category: {}", category);
            resultPage = transactionRepository.findByCategoryAndDeletedFalse(category, pageable);
        } else if (from != null && to != null) {
            log.debug("Filtering by date range: {} to {}", from, to);
            resultPage = transactionRepository.findByDateBetweenAndDeletedFalse(from, to, pageable);
        } else {
            log.debug("No filters applied — fetching all non-deleted transactions");
            resultPage = transactionRepository.findByDeletedFalse(pageable);
        }
        log.info("Transactions fetched — total elements: {}, total pages: {}",
                resultPage.getTotalElements(), resultPage.getTotalPages());
        return resultPage.map(this::toResponse); // Maps Page<Transaction> -> Page<TransactionResponse>
    }


    public @Nullable TransactionResponse getById(Long id) {
        log.debug("Fetching transaction by id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found with id: {}", id);
                      return   new ResourceNotFoundException(
                        "Transaction not found with id: " + id);
                });
        log.info("Transaction found — id: {}",id);
        return  toResponse(transaction);
    }

    public List<TransactionResponse> getByUserId(Long userId) {
        log.debug("Fetching transactions for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                   return new ResourceNotFoundException("User not found with id: " + userId);
                });

        List<TransactionResponse> list = transactionRepository.findByCreatedByIdAndDeletedFalse(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        log.info("Transactions fetched for user id: {} — count: {}", userId, list.size());
        return list;
    }

    public List<TransactionResponse> getMyTransactionsByDate(String mobile, LocalDate fromDate, LocalDate toDate) {
        log.debug("Fetching transactions by date range — mobile: {}, from: {}, to: {}",
                mobile, fromDate, toDate);
        // Find user by mobile
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.warn("User not found for date-range query — mobile: {}", mobile);
                      return   new ResourceNotFoundException("User not found with mobile: " + mobile);
                        });

        List<TransactionResponse> result = transactionRepository
                .findByCreatedByIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        log.info("Date-range transactions fetched — user id: {}, from: {}, to: {}, count: {}",
                user.getId(), fromDate, toDate, result.size());

        return result;
    }



    public void delete(Long id) {
        log.debug("Soft delete requested for transaction id: {}", id);
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for soft delete — id: {}", id);
                    return new ResourceNotFoundException("Transaction not found with Id " + id);
                });
        t.setDeleted(true);
        transactionRepository.save(t);
    }



    public TransactionResponse update(Long transactionId, @Valid TransactionRequest req) {

        log.debug("Update request for transaction id: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for update — id: {}", transactionId);
                       return new ResourceNotFoundException("Transaction not found with id: " + transactionId);
                });
        log.debug("Before update — type: {}, amount: {}, category: {}",
                transaction.getType(), transaction.getAmount(), transaction.getCategory());

        transaction.setAmount(req.getAmount());
        transaction.setType(TransactionType.valueOf(req.getType())); // assuming enum
        transaction.setCategory(req.getCategory());
        transaction.setDate(LocalDate.now());
        transaction.setNotes(req.getNotes());

        // save updated entity
        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated — id: {}, type: {}, amount: {}, category: {}",
                updated.getTransactionId(), updated.getType(),
                updated.getAmount(), updated.getCategory());

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
        log.debug("Fetching own transactions for mobile: {}", mobile);
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() ->{
                    log.warn("User not found for /my transactions — mobile: {}", mobile);
                      return   new ResourceNotFoundException("User not found");});

        List<TransactionResponse> result = transactionRepository.findByCreatedByIdAndDeletedFalse(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        log.info("Own transactions fetched — user id: {}, count: {}",
                user.getId(), result.size());
        return result;
    }
}