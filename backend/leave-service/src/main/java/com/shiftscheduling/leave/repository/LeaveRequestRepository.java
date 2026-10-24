package com.shiftscheduling.leave.repository;

import com.shiftscheduling.leave.entity.LeaveRequest;
import com.shiftscheduling.leave.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LeaveStatus status, LocalDate endDate, LocalDate startDate);

    List<LeaveRequest> findByStartDateBetween(LocalDate start, LocalDate end);

    List<LeaveRequest> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LeaveStatus status, LocalDate rangeEnd, LocalDate rangeStart);
}
