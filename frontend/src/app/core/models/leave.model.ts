export interface LeaveRequest {
  employeeId: number;
  leaveType: string;
  startDate: string;
  endDate: string;
  reason: string;
}

export interface LeaveResponse {
  id: number;
  employeeId: number;
  leaveType: string;
  startDate: string;
  endDate: string;
  reason: string;
  status: string;
  reviewedBy: string;
  reviewNotes: string;
  createdAt: string;
  updatedAt: string;
}

export interface LeaveBalance {
  employeeId: number;
  year: number;
  annualTotal: number;
  annualUsed: number;
  annualRemaining: number;
  sickTotal: number;
  sickUsed: number;
  sickRemaining: number;
  personalTotal: number;
  personalUsed: number;
  personalRemaining: number;
}

export interface LeaveApproval {
  approved: boolean;
  reviewNotes: string;
}
