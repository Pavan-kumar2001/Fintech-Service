package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.roleandstatus.UpdateRoleRequest;
import com.fintech.fintech_service.dto.roleandstatus.UpdateStatusRequest;
import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.dto.user.UserResponse;
import com.fintech.fintech_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    //CREATE USER
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse>  create(@RequestBody UserRequest userRequest) {
        log.info("Creating User with User-Name:"+userRequest.getUserName());
        UserResponse response = service.create(userRequest);
        log.info("User created with id: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        log.info("Fetching all users");
        List<UserResponse> users = service.getAll();
        log.info("Total users fetched: {}", users.size());
        return ResponseEntity.ok(users);
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    // UPDATE ROLE
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                     @RequestBody UpdateRoleRequest req) {
      service.updateRole(id, req.getRole());
        log.info("Updating role for user id: {} to {}", id, req.getRole());
        return  ResponseEntity.ok(Map.of("message","Role Updated Successfully"));
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                             @RequestBody UpdateStatusRequest req) {
        log.info("Updating status for user id: {} to active={}", id, req.getStatus());
        service.updateStatus(id, req.getStatus());

       return ResponseEntity.ok(Map.of("message","Status Updated Successfully"));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        log.warn("Deleting user with id: {}", id);
        service.delete(id);
        log.info("User deleted successfully: {}", id);
    }
}