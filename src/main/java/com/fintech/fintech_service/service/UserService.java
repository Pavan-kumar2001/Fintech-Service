package com.fintech.fintech_service.service;

import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.dto.user.UserResponse;
import com.fintech.fintech_service.entity.User;
import com.fintech.fintech_service.enums.Role;
import com.fintech.fintech_service.enums.Status;
import com.fintech.fintech_service.exception.ResourceAlreadyExistsException;
import com.fintech.fintech_service.exception.ResourceNotFoundException;
import com.fintech.fintech_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    // CREATE
    public UserResponse create(UserRequest req) {
        log.debug("Checking if Mobile Number already exists: {}", req.getMobile());
        if (repo.existsByMobile(req.getMobile())) {
            log.warn("Registration blocked — mobile already registered: {}", req.getMobile());
            throw new ResourceAlreadyExistsException("User with provided number already exists");
        }

        User user = User.builder()
                .userName(req.getUserName())
                .mobile(req.getMobile())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        log.info("New user created - id: {}, role: {}", user.getId(), user.getRole());
        return toResponse(repo.save(user));
    }

    // GET ALL
    public List<UserResponse> getAll() {
        log.info("Fetching all users");
        List<UserResponse> users = repo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        log.info("Total users fetched: {}", users.size());
        return users;
    }

    // GET BY ID
    public UserResponse getById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));
        log.info("User found — id: {}, mobile: {}", user.getId(), user.getMobile());
        return toResponse(user);
    }

    public boolean existsByMobile(String mobile) {
        boolean exists = repo.existsByMobile(mobile);
        log.debug("Mobile existence check: {} → {}", mobile, exists);
        return exists;
    }

    // UPDATE ROLE
    public void updateRole(Long id, String role) {
        log.debug("Updating role for user id: {} to {}", id, role);
        User user = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for role update — id: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
        Role previousRole = user.getRole();
        user.setRole(Role.valueOf(role));
         repo.save(user);
        log.info("Role updated — user id: {}, {} → {}", id, previousRole, role);
    }

    // UPDATE STATUS
    public void updateStatus(Long id, String status) {
        log.debug("Updating status for user id: {} to {}", id, status);
        User user = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for status update — id: {}", id);
                         return new ResourceNotFoundException("User not found");
                        }
                );
        System.out.println(status);
        user.setStatus(Status.valueOf(status));
        repo.save(user);
    }

    // DELETE
    public void delete(Long id) {
        log.warn("Deleting user with id: {}", id);
        if (!repo.existsById(id)) {
            log.warn("Delete failed — user not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        repo.deleteById(id);
        log.info("User deleted successfully — id: {}", id);
    }

    // MAPPER
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .status(String.valueOf(user.getStatus()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}