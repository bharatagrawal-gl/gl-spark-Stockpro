import { useCallback, useState } from 'react';

export interface SnackbarState {
  open: boolean;
  message: string;
  duration: number;
}

export function useSnackbar() {
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, message: '', duration: 3000 });

  const showMessage = useCallback((message: string, duration = 3000) => {
    setSnackbar({ open: true, message, duration });
  }, []);

  const close = useCallback(() => {
    setSnackbar((state) => ({ ...state, open: false }));
  }, []);

  return { snackbar, showMessage, close };
}

export function getErrorMessage(err: unknown, fallback: string): string {
  const message = (err as { response?: { data?: { message?: unknown } } })?.response?.data?.message;
  return typeof message === 'string' && message.trim() ? message : fallback;
}
