export type AlertType = 'LOW_STOCK' | 'PO_OVERDUE' | 'MANUAL';
export interface Alert {
  id: number; alertType: AlertType;
  referenceId: number; referenceType: string;
  message: string; isRead: boolean; createdAt: string;
}
