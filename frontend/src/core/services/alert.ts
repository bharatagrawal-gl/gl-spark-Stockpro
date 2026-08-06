import { http } from '../http';
import { Alert } from '../models/alert';

type UnreadCountListener = (count: number) => void;

let unreadCount = 0;
const listeners = new Set<UnreadCountListener>();

function setUnreadCount(count: number): void {
  unreadCount = count;
  listeners.forEach((listener) => listener(unreadCount));
}

export const alertService = {
  async getAll(): Promise<Alert[]> {
    const { data } = await http.get<Alert[]>('/api/v1/alerts');
    return data;
  },

  async getUnread(): Promise<Alert[]> {
    const { data } = await http.get<Alert[]>('/api/v1/alerts/unread');
    setUnreadCount(data.length);
    return data;
  },

  async markAsRead(id: number): Promise<Alert> {
    const { data } = await http.put<Alert>(`/api/v1/alerts/${id}/read`, {});
    await alertService.refreshUnreadCount();
    return data;
  },

  async markAllAsRead(): Promise<void> {
    await http.put<void>('/api/v1/alerts/read-all', {});
    setUnreadCount(0);
  },

  async refreshUnreadCount(): Promise<Alert[]> {
    return alertService.getUnread();
  },

  getUnreadCount(): number {
    return unreadCount;
  },

  subscribeUnreadCount(listener: UnreadCountListener): () => void {
    listeners.add(listener);
    listener(unreadCount);
    return () => listeners.delete(listener);
  }
};
