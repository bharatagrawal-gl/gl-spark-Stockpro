import { Button, Snackbar } from '@mui/material';
import { SnackbarState } from '../hooks/useSnackbar';

interface SnackbarHostProps {
  snackbar: SnackbarState;
  onClose: () => void;
}

export function SnackbarHost({ snackbar, onClose }: SnackbarHostProps) {
  return (
    <Snackbar
      open={snackbar.open}
      autoHideDuration={snackbar.duration}
      onClose={onClose}
      message={snackbar.message}
      action={<Button color="inherit" size="small" onClick={onClose}>Close</Button>}
    />
  );
}
