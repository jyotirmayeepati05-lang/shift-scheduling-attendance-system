export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  referenceId: string;
  read: boolean;
  createdAt: string;
}

export interface NotificationCount {
  unreadCount: number;
}
