import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow
} from '@mui/material';
import { User } from '../../../core/models/user';
import { authService } from '../../../core/services/auth';
import { useSnackbar, getErrorMessage } from '../../../shared/hooks/useSnackbar';
import { SnackbarHost } from '../../../shared/components/SnackbarHost';
import { formatMediumDateTime } from '../../../shared/utils/format';
import './UserList.css';

export function UserList() {
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<User[]>([]);
  const { snackbar, showMessage, close } = useSnackbar();

  useEffect(() => { loadUsers(); }, []);

  function loadUsers(): void {
    setLoading(true);
    authService.getAllUsers()
      .then(setUsers)
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to load users')))
      .finally(() => setLoading(false));
  }

  function toggleStatus(user: User): void {
    const request = user.isActive
      ? authService.deactivateUser(user.id)
      : authService.reactivateUser(user.id);

    request
      .then(() => {
        showMessage(`User ${user.isActive ? 'deactivated' : 'reactivated'}`);
        loadUsers();
      })
      .catch((error) => showMessage(getErrorMessage(error, 'Unable to update user')));
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Users</h1>
          <p>Manage account access and operational roles.</p>
        </div>
      </div>

      <Card>
        {loading && (
          <div className="loading-state">
            <CircularProgress size={42} />
          </div>
        )}

        {!loading && (
          <div className="table-shell">
            <Table className="mat-card-table">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Phone</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Last Login</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.fullName}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.phone || '-'}</TableCell>
                    <TableCell>
                      <span className={`status-chip ${user.role.toLowerCase()}`}>{user.role}</span>
                    </TableCell>
                    <TableCell>
                      <span className={`status-chip ${(user.isActive ? 'ACTIVE' : 'INACTIVE').toLowerCase()}`}>
                        {user.isActive ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </TableCell>
                    <TableCell>{user.lastLoginAt ? formatMediumDateTime(user.lastLoginAt) : '-'}</TableCell>
                    <TableCell>
                      <Button variant="outlined" onClick={() => toggleStatus(user)}>
                        {user.isActive ? 'Deactivate' : 'Reactivate'}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {users.length === 0 && (
              <div className="empty-state">No users found.</div>
            )}
          </div>
        )}
      </Card>

      <SnackbarHost snackbar={snackbar} onClose={close} />
    </div>
  );
}
