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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    // CREATE
    public UserResponse create(UserRequest req) {

        if (repo.existsByMobile(req.getMobile())) {
            System.out.println(repo.findByMobile(req.getMobile()));
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

        return toResponse(repo.save(user));
    }

    // GET ALL
    public List<UserResponse> getAll() {
        return repo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET BY ID
    public UserResponse getById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));

        return toResponse(user);
    }

    public boolean existsByMobile(String mobile) {
   return repo.existsByMobile(mobile);
    }

    // UPDATE ROLE
    public void updateRole(Long id, String role) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRole(Role.valueOf(role));
         repo.save(user);
    }

    // UPDATE STATUS
    public void updateStatus(Long id, String status) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        System.out.println(status);
        user.setStatus(Status.valueOf(status));
        repo.save(user);
    }

    // DELETE
    public void delete(Long id) {
        repo.deleteById(id);
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