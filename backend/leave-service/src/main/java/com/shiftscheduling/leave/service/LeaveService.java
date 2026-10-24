package com.shiftscheduling.leave.service;

import com.shiftscheduling.leave.dto.*;
import com.shiftscheduling.leave.entity.*;
import com.shiftscheduling.leave.repository.LeaveBalanceRepository;
import com.shiftscheduling.leave.repository.LeaveRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Transactional
    public LeaveResponse applyLeave(LeaveRequestDto dto) {
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        int days = (int) ChronoUnit.DAYS.between(dto.startDate(), dto.endDate()) + 1;

        if (dto.leaveType() != LeaveType.UNPAID) {
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndYear(dto.employeeId(), dto.startDate().getYear())
                    .orElseGet(() -> initializeBalance(dto.employeeId(), dto.startDate().getYear()));

            int remaining = getRemainingForType(balance, dto.leaveType());
            if (days > remaining) {
                throw new RuntimeException("Insufficient " + dto.leaveType() + " leave balance. Remaining: " + remaining + ", Requested: " + days);
            }
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(dto.employeeId());
        request.setLeaveType(dto.leaveType());
        request.setStartDate(dto.startDate());
        request.setEndDate(dto.endDate());
        request.setReason(dto.reason());
        request.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(request);
        return toResponse(saved);
    }

    @Transactional
    public LeaveResponse approveOrReject(Long leaveId, Long reviewerId, LeaveApprovalRequest approval) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + leaveId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request has already been " + request.getStatus().name().toLowerCase());
        }

        if (request.getEmployeeId().equals(reviewerId)) {
            throw new RuntimeException("Cannot review your own leave request");
        }

        if (approval.status() != LeaveStatus.APPROVED && approval.status() != LeaveStatus.REJECTED) {
            throw new RuntimeException("Review status must be APPROVED or REJECTED");
        }

        request.setStatus(approval.status());
        request.setReviewedBy(reviewerId);
        request.setReviewNotes(approval.reviewNotes());

        if (approval.status() == LeaveStatus.APPROVED && request.getLeaveType() != LeaveType.UNPAID) {
            int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndYear(request.getEmployeeId(), request.getStartDate().getYear())
                    .orElseGet(() -> initializeBalance(request.getEmployeeId(), request.getStartDate().getYear()));

            deductBalance(balance, request.getLeaveType(), days);
            leaveBalanceRepository.save(balance);
        }

        LeaveRequest saved = leaveRequestRepository.save(request);
        return toResponse(saved);
    }

    public Page<LeaveResponse> getLeavesByEmployee(Long employeeId, Pageable pageable) {
        return leaveRequestRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse);
    }

    public List<LeaveResponse> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<LeaveResponse> getApprovedLeaves(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LeaveStatus.APPROVED, endDate, startDate)
                .stream().map(this::toResponse).toList();
    }

    public LeaveBalanceResponse getLeaveBalance(Long employeeId, int year) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .orElseGet(() -> initializeBalance(employeeId, year));
        return toBalanceResponse(balance);
    }

    @Transactional
    public LeaveBalance initializeBalance(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .orElseGet(() -> {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setEmployeeId(employeeId);
                    balance.setYear(year);
                    balance.setAnnualLeave(20);
                    balance.setSickLeave(10);
                    balance.setPersonalLeave(5);
                    balance.setAnnualUsed(0);
                    balance.setSickUsed(0);
                    balance.setPersonalUsed(0);
                    return leaveBalanceRepository.save(balance);
                });
    }

    public boolean hasApprovedLeave(Long employeeId, LocalDate date) {
        List<LeaveRequest> leaves = leaveRequestRepository
                .findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, LeaveStatus.APPROVED, date, date);
        return !leaves.isEmpty();
    }

    private int getRemainingForType(LeaveBalance balance, LeaveType type) {
        return switch (type) {
            case ANNUAL -> balance.getAnnualLeave() - balance.getAnnualUsed();
            case SICK -> balance.getSickLeave() - balance.getSickUsed();
            case PERSONAL -> balance.getPersonalLeave() - balance.getPersonalUsed();
            case UNPAID -> Integer.MAX_VALUE;
        };
    }

    private void deductBalance(LeaveBalance balance, LeaveType type, int days) {
        switch (type) {
            case ANNUAL -> balance.setAnnualUsed(balance.getAnnualUsed() + days);
            case SICK -> balance.setSickUsed(balance.getSickUsed() + days);
            case PERSONAL -> balance.setPersonalUsed(balance.getPersonalUsed() + days);
            case UNPAID -> { /* no deduction */ }
        }
    }

    private LeaveResponse toResponse(LeaveRequest r) {
        return new LeaveResponse(
                r.getId(), r.getEmployeeId(), r.getLeaveType(),
                r.getStartDate(), r.getEndDate(), r.getReason(),
                r.getStatus(), r.getReviewedBy(), r.getReviewNotes(),
                r.getCreatedAt(), r.getUpdatedAt()
        );
    }

    private LeaveBalanceResponse toBalanceResponse(LeaveBalance b) {
        return new LeaveBalanceResponse(
                b.getId(), b.getEmployeeId(),
                b.getAnnualLeave(), b.getSickLeave(), b.getPersonalLeave(),
                b.getAnnualUsed(), b.getSickUsed(), b.getPersonalUsed(),
                b.getAnnualLeave() - b.getAnnualUsed(),
                b.getSickLeave() - b.getSickUsed(),
                b.getPersonalLeave() - b.getPersonalUsed(),
                b.getYear()
        );
    }
}
