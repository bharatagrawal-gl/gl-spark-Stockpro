import { useEffect, useMemo, useState } from 'react';
import { Button, Card, CircularProgress } from '@mui/material';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { Alert } from '../../../core/models/alert';
import { alertService } from '../../../core/services/alert';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatMediumDateTime } from '../../../shared/utils/format';
import './AlertList.css';

export function AlertList() {
  const [loading, setLoading] = useState(true);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadAlerts(); }, []);

  const unreadCount = useMemo(() => alerts.filter((alert) => !alert.isRead).length, [alerts]);

  function loadAlerts(): void {
    setLoading(true);
    alertService.getAll()
      .then(setAlerts)
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load alerts')))
      .finally(() => setLoading(false));
  }

  function markAsRead(alert: Alert): void {
    alertService.markAsRead(alert.id)
      .then(() => {
        showMessage('Alert marked as read', 2500);
        loadAlerts();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to mark alert as read')));
  }

  function markAllAsRead(): void {
    alertService.markAllAsRead()
      .then(() => {
        showMessage('All alerts marked as read', 2500);
        loadAlerts();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update alerts')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Alerts</h1>
          <p>{unreadCount} unread alerts need attention.</p>
        </div>

        <Button variant="contained" color="primary" startIcon={<DoneAllIcon />} disabled={unreadCount === 0} onClick={markAllAsRead}>
          Mark All Read
        </Button>
      </div>

      {loading && (
        <div className="loading-state">
          <CircularProgress size={42} />
        </div>
      )}

      {!loading && (
        <div className="alert-list">
          {alerts.map((alert) => (
            <Card key={alert.id} className={`alert-card${!alert.isRead ? ' unread' : ''}`}>
              <div className="alert-content">
                <div className="alert-main">
                  <span className={`status-chip ${alert.alertType.toLowerCase()}`}>{alert.alertType}</span>
                  <p>{alert.message}</p>
                  <span>{formatMediumDateTime(alert.createdAt)} &bull; {alert.referenceType} #{alert.referenceId}</span>
                </div>

                {!alert.isRead && (
                  <Button variant="outlined" onClick={() => markAsRead(alert)}>Mark Read</Button>
                )}
              </div>
            </Card>
          ))}

          {alerts.length === 0 && (
            <div className="empty-state">There are no alerts right now.</div>
          )}
        </div>
      )}

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
