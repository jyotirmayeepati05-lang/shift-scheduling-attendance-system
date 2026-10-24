package com.shiftscheduling.scheduling.controller;

import com.shiftscheduling.scheduling.dto.ShiftRequest;
import com.shiftscheduling.scheduling.dto.ShiftResponse;
import com.shiftscheduling.scheduling.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final SchedulingService service;

    public ShiftController(SchedulingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> create(@Valid @RequestBody ShiftRequest req) {
        return ResponseEntity.ok(service.createShift(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponse> update(@PathVariable Long id, @Valid @RequestBody ShiftRequest req) {
        return ResponseEntity.ok(service.updateShift(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getShift(id));
    }

    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (departmentId != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(service.getShiftsByDepartmentAndDateRange(departmentId, startDate, endDate));
        }
        if (departmentId != null) {
            return ResponseEntity.ok(service.getShiftsByDepartment(departmentId));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(service.getShiftsByDateRange(startDate, endDate));
        }
        return ResponseEntity.ok(service.getAllShifts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
