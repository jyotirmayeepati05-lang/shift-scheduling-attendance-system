package com.shiftscheduling.leave.dto;

import com.shiftscheduling.leave.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record LeaveApprovalRequest(
    @NotNull LeaveStatus status,
    String reviewNotes
) {}
