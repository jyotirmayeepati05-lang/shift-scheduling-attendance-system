import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { LeaveService } from '../../../../core/services/leave.service';
import { LeaveResponse } from '../../../../core/models/leave.model';

@Component({
  selector: 'app-leave-approval',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatTooltipModule, MatSnackBarModule],
  templateUrl: './leave-approval.component.html',
  styleUrls: ['./leave-approval.component.scss']
})
export class LeaveApprovalComponent implements OnInit {
  private leaveService = inject(LeaveService);
  private snackBar = inject(MatSnackBar);

  pendingLeaves: LeaveResponse[] = [];
  displayedColumns = ['employeeId', 'leaveType', 'startDate', 'endDate', 'reason', 'actions'];
  reviewNotes: { [id: number]: string } = {};

  ngOnInit(): void { this.loadPending(); }

  loadPending(): void {
    this.leaveService.getPendingLeaves().subscribe(d => this.pendingLeaves = d);
  }

  approve(leave: LeaveResponse): void {
    this.leaveService.reviewLeave(leave.id, { approved: true, reviewNotes: this.reviewNotes[leave.id] || '' }).subscribe({
      next: () => { this.snackBar.open('Leave approved', 'Close', { duration: 3000 }); this.loadPending(); },
      error: err => this.snackBar.open(err.error?.error || 'Failed', 'Close', { duration: 3000 })
    });
  }

  reject(leave: LeaveResponse): void {
    this.leaveService.reviewLeave(leave.id, { approved: false, reviewNotes: this.reviewNotes[leave.id] || '' }).subscribe({
      next: () => { this.snackBar.open('Leave rejected', 'Close', { duration: 3000 }); this.loadPending(); },
      error: err => this.snackBar.open(err.error?.error || 'Failed', 'Close', { duration: 3000 })
    });
  }
}
