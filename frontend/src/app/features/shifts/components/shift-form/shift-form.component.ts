import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ShiftService } from '../../../../core/services/shift.service';
import { EmployeeService } from '../../../../core/services/employee.service';
import { Department, Skill } from '../../../../core/models/employee.model';

@Component({
  selector: 'app-shift-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSlideToggleModule, MatSnackBarModule, MatTooltipModule],
  templateUrl: './shift-form.component.html',
  styleUrls: ['./shift-form.component.scss']
})
export class ShiftFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private shiftService = inject(ShiftService);
  private employeeService = inject(EmployeeService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  form!: FormGroup;
  departments: Department[] = [];
  skills: Skill[] = [];
  isEdit = false;
  shiftId?: number;

  get requirements(): FormArray { return this.form.get('requirements') as FormArray; }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      departmentId: [null, Validators.required],
      date: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      overnight: [false],
      minStaff: [1, [Validators.required, Validators.min(1)]],
      maxStaff: [5, [Validators.required, Validators.min(1)]],
      nightShift: [false],
      requirements: this.fb.array([])
    });

    this.employeeService.getDepartments().subscribe(d => this.departments = d);
    this.employeeService.getSkills().subscribe(s => this.skills = s);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.shiftId = +id;
      this.shiftService.getShift(this.shiftId).subscribe(shift => {
        this.form.patchValue(shift);
      });
    }
  }

  addRequirement(): void {
    this.requirements.push(this.fb.group({
      skillId: [null, Validators.required],
      requiredCount: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  removeRequirement(index: number): void {
    this.requirements.removeAt(index);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const data = this.form.value;
    const op = this.isEdit
      ? this.shiftService.updateShift(this.shiftId!, data)
      : this.shiftService.createShift(data);

    op.subscribe({
      next: () => {
        this.snackBar.open(this.isEdit ? 'Shift updated' : 'Shift created', 'OK', { duration: 3000 });
        this.router.navigate(['/shifts']);
      },
      error: (err) => this.snackBar.open(err.error?.error || 'Error saving shift', 'OK', { duration: 5000 })
    });
  }
}
