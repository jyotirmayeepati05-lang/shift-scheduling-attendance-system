package com.shiftscheduling.notification.dto;

import jakarta.validation.constraints.NotNull;

public record LeaveDecisionRequest(
    @NotNull Long userId,
    @NotNull Long leaveId,
    @NotNull Boolean approved,
    String reason
) {}
