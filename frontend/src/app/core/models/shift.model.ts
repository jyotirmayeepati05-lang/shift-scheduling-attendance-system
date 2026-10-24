export interface Shift {
  id: number;
  name: string;
  departmentId: number;
  startTime: string;
  endTime: string;
  date: string;
  overnight: boolean;
  maxStaff: number;
  minStaff: number;
  nightShift: boolean;
}

export interface ShiftRequest {
  name: string;
  departmentId: number;
  startTime: string;
  endTime: string;
  date: string;
  overnight: boolean;
  maxStaff: number;
  minStaff: number;
  nightShift: boolean;
  requirements: ShiftRequirementRequest[];
}

export interface ShiftRequirement {
  id: number;
  shiftId: number;
  skillId: number;
  requiredCount: number;
}

export interface ShiftRequirementRequest {
  skillId: number;
  requiredCount: number;
}
