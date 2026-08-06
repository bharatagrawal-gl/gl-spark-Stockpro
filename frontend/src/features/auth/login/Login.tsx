import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  IconButton,
  InputAdornment,
  TextField
} from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import { authService } from '../../../core/services/auth';
import './Login.css';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [hidePassword, setHidePassword] = useState(true);
  const [touched, setTouched] = useState({ email: false, password: false });

  const emailRequiredError = touched.email && !email;
  const emailFormatError = touched.email && !!email && !EMAIL_PATTERN.test(email);
  const passwordRequiredError = touched.password && !password;
  const passwordMinLengthError = touched.password && !!password && password.length < 6;

  const isFormInvalid =
    !email || !EMAIL_PATTERN.test(email) || !password || password.length < 6;

  function getErrorMessage(err: unknown, fallback: string): string {
    const message = (err as { response?: { data?: { message?: unknown } } })?.response?.data?.message;
    return typeof message === 'string' && message.trim() ? message : fallback;
  }

  function onSubmit(event: FormEvent): void {
    event.preventDefault();

    if (isFormInvalid) {
      setTouched({ email: true, password: true });
      setError('Please correct the highlighted fields');
      return;
    }

    setLoading(true);
    setError('');
    authService.login({ email: email.trim(), password })
      .then(() => navigate('/dashboard'))
      .catch((err) => {
        setError(getErrorMessage(err, 'Invalid email or password'));
        setLoading(false);
      });
  }

  return (
    <div className="login-wrapper">
      <Card className="login-card">
        <CardContent>
          <div className="login-header">
            <h1>StockPro</h1>
            <p>Inventory Management System</p>
          </div>

          {error && <div className="error-banner">{error}</div>}

          <form onSubmit={onSubmit} noValidate>
            <TextField
              className="full-width"
              variant="outlined"
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, email: true }))}
              placeholder="admin@stockpro.com"
              error={emailRequiredError || emailFormatError}
              helperText={
                emailRequiredError
                  ? 'Email is required'
                  : emailFormatError
                    ? 'Enter a valid email address'
                    : ' '
              }
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <EmailIcon />
                  </InputAdornment>
                )
              }}
            />

            <TextField
              className="full-width"
              variant="outlined"
              label="Password"
              type={hidePassword ? 'password' : 'text'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, password: true }))}
              error={passwordRequiredError || passwordMinLengthError}
              helperText={
                passwordRequiredError
                  ? 'Password is required'
                  : passwordMinLengthError
                    ? 'Password must be at least 6 characters'
                    : ' '
              }
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      type="button"
                      onClick={() => setHidePassword((value) => !value)}
                      edge="end"
                    >
                      {hidePassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                    </IconButton>
                  </InputAdornment>
                )
              }}
            />

            <Button
              className="full-width login-btn"
              variant="contained"
              color="primary"
              type="submit"
              disabled={loading || isFormInvalid}
            >
              {loading ? <CircularProgress size={20} color="inherit" /> : <span>Sign In</span>}
            </Button>
          </form>
        </CardContent>

        <CardActions>
          <p className="register-link">Don't have an account? <Link to="/register">Register</Link></p>
        </CardActions>
      </Card>
    </div>
  );
}
