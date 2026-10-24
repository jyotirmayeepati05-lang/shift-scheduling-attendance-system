import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { NotificationService } from '../../../../core/services/notification.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Notification } from '../../../../core/models/notification.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatPaginatorModule],
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss']
})
export class NotificationListComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  notifications: Notification[] = [];
  userId: number = 0;
  totalElements = 0;
  pageSize = 20;

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) { this.userId = user.userId; this.load(); }
  }

  load(page = 0): void {
    this.notificationService.getNotifications(this.userId, page, this.pageSize).subscribe(data => {
      this.notifications = data.content || data;
      this.totalElements = data.totalElements || this.notifications.length;
    });
  }

  onPage(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.load(event.pageIndex);
  }

  markRead(n: Notification): void {
    if (n.read) return;
    this.notificationService.markAsRead(n.id).subscribe(() => { n.read = true; });
  }

  markAllRead(): void {
    this.notificationService.markAllAsRead(this.userId).subscribe(() => {
      this.notifications.forEach(n => n.read = true);
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'SCHEDULE_PUBLISHED': return 'event_available';
      case 'LEAVE_APPROVED': return 'check_circle';
      case 'LEAVE_REJECTED': return 'cancel';
      case 'ATTENDANCE_REMINDER': return 'alarm';
      case 'SHIFT_CHANGE': return 'swap_horiz';
      default: return 'notifications';
    }
  }
}
