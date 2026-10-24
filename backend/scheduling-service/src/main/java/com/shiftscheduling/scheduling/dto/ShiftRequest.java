package com.shiftscheduling.scheduling.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ShiftRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull Long departmentId,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull LocalDate date,
    boolean overnight,
    @Min(1) int maxStaff,
    @Min(1) int minStaff,
    boolean nightShift,
    List<ShiftRequirementRequest> requirements
) {}
