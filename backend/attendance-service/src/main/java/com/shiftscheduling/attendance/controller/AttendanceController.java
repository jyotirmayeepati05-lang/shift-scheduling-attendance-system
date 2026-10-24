package com.shiftscheduling.attendance.controller;

import com.shiftscheduling.attendance.dto.*;
import com.shiftscheduling.attendance.service.AttendanceService;
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
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(@Valid @RequestBody CheckInRequest req) {
        return ResponseEntity.ok(service.checkIn(req));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(@Valid @RequestBody CheckOutRequest req) {
        return ResponseEntity.ok(service.checkOut(req));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AttendanceResponse>> getHistory(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(service.getAttendanceHistory(employeeId, startDate, endDate, pr));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getAttendanceByDate(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAttendanceById(id));
    }

    @PostMapping("/{id}/correct")
    public ResponseEntity<CorrectionResponse> correct(
            @PathVariable Long id, @Valid @RequestBody CorrectionRequest req) {
        return ResponseEntity.ok(service.correctAttendance(id, req));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<AttendanceReportResponse> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.exportCsv(startDate, endDate));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "attendance-service"));
    }
}
