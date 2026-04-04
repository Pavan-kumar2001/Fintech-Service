package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.roleandstatus.UpdateRoleRequest;
import com.fintech.fintech_service.dto.roleandstatus.UpdateStatusRequest;
import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.dto.user.UserResponse;
import com.fintech.fintech_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    //CREATE USER
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse>  create(@RequestBody UserRequest userRequest) {
        return new ResponseEntity<>(service.create(userRequest), HttpStatus.CREATED);
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // UPDATE ROLE
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                     @RequestBody UpdateRoleRequest req) {
      service.updateRole(id, req.getRole());
        return  ResponseEntity.ok(Map.of("message","Role Updated Successfully"));
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                             @RequestBody UpdateStatusRequest req) {
        service.updateStatus(id, req.getStatus());
       return ResponseEntity.ok(Map.of("message","Status Updated Successfully"));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}