package com.shiftscheduling.leave.controller;

import com.shiftscheduling.leave.dto.*;
import com.shiftscheduling.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(@Valid @RequestBody LeaveRequestDto dto) {
        return ResponseEntity.ok(leaveService.applyLeave(dto));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<LeaveResponse> reviewLeave(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @Valid @RequestBody LeaveApprovalRequest approval) {
        return ResponseEntity.ok(leaveService.approveOrReject(id, reviewerId, approval));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeaveResponse>> getLeavesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(
                employeeId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<LeaveResponse>> getApprovedLeaves(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(leaveService.getApprovedLeaves(startDate, endDate));
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId, targetYear));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> hasApprovedLeave(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean hasLeave = leaveService.hasApprovedLeave(employeeId, date);
        return ResponseEntity.ok(Map.of("hasApprovedLeave", hasLeave));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "leave-service"));
    }
}
