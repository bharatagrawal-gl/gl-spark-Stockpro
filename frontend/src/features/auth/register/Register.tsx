import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Button,
  Card,
  CardActions,
  CardContent,
  MenuItem,
  TextField
} from '@mui/material';
import { authService } from '../../../core/services/auth';
import './Register.css';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_PATTERN = /^[0-9]{10}$/;

type Role = 'ADMIN' | 'MANAGER' | 'STAFF' | 'AUDITOR';

export function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [adminSecret, setAdminSecret] = useState('');
  const [role, setRole] = useState<Role>('STAFF');
  const [assignedWarehouseId, setAssignedWarehouseId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [touched, setTouched] = useState({
    fullName: false, email: false, phone: false, password: false, role: false, adminSecret: false
  });

  const requiresAdminSecret = role === 'ADMIN';

  function onRoleChange(nextRole: Role): void {
    setRole(nextRole);
    if (nextRole !== 'ADMIN') {
      setAdminSecret('');
    }
  }

  const fullNameRequiredError = touched.fullName && !fullName;
  const fullNameMinLengthError = touched.fullName && !!fullName && fullName.length < 2;
  const fullNameMaxLengthError = touched.fullName && fullName.length > 100;

  const emailRequiredError = touched.email && !email;
  const emailFormatError = touched.email && !!email && !EMAIL_PATTERN.test(email);

  const phonePatternError = touched.phone && !!phone && !PHONE_PATTERN.test(phone);

  const passwordRequiredError = touched.password && !password;
  const passwordMinLengthError = touched.password && !!password && password.length < 6;

  const adminSecretRequiredError = touched.adminSecret && requiresAdminSecret && !adminSecret;

  const isFormInvalid =
    !fullName || fullName.length < 2 || fullName.length > 100 ||
    !email || !EMAIL_PATTERN.test(email) ||
    (!!phone && !PHONE_PATTERN.test(phone)) ||
    !password || password.length < 6 ||
    (requiresAdminSecret && !adminSecret.trim());

  function getErrorMessage(err: unknown, fallback: string): string {
    const message = (err as { response?: { data?: { message?: unknown } } })?.response?.data?.message;
    return typeof message === 'string' && message.trim() ? message : fallback;
  }

  function onSubmit(event: FormEvent): void {
    event.preventDefault();

    if (isFormInvalid) {
      setTouched({ fullName: true, email: true, phone: true, password: true, role: true, adminSecret: true });
      setError('Please correct the highlighted fields');
      return;
    }

    if (requiresAdminSecret && !adminSecret.trim()) {
      setError('Admin secret is required for admin registration');
      return;
    }

    setLoading(true);
    setError('');
    authService.register({
      fullName: fullName.trim(),
      email: email.trim(),
      password,
      phone: phone.trim(),
      role,
      assignedWarehouseId: assignedWarehouseId ? Number(assignedWarehouseId) : undefined,
      adminSecret: role === 'ADMIN' ? adminSecret : undefined
    })
      .then(() => { setSuccess(true); setLoading(false); })
      .catch((err) => { setError(getErrorMessage(err, 'Registration failed')); setLoading(false); });
  }

  return (
    <div className="login-wrapper">
      <Card className="login-card">
        <CardContent>
          <div className="login-header">
            <h1>StockPro</h1>
            <p>Create your account</p>
          </div>

          {error && <div className="error-banner">{error}</div>}
          {success && (
            <div className="success-banner">
              Registration successful! <Link to="/login">Login now</Link>
            </div>
          )}

          <form onSubmit={onSubmit} noValidate>
            <TextField
              className="full-width"
              variant="outlined"
              label="Full Name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, fullName: true }))}
              inputProps={{ maxLength: 100 }}
              error={fullNameRequiredError || fullNameMinLengthError || fullNameMaxLengthError}
              helperText={
                fullNameRequiredError
                  ? 'Full name is required'
                  : fullNameMinLengthError
                    ? 'Full name must be at least 2 characters'
                    : fullNameMaxLengthError
                      ? 'Full name must be at most 100 characters'
                      : ' '
              }
            />

            <TextField
              className="full-width"
              variant="outlined"
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, email: true }))}
              error={emailRequiredError || emailFormatError}
              helperText={
                emailRequiredError
                  ? 'Email is required'
                  : emailFormatError
                    ? 'Enter a valid email address'
                    : ' '
              }
            />

            <TextField
              className="full-width"
              variant="outlined"
              label="Phone"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, phone: true }))}
              inputMode="numeric"
              inputProps={{ maxLength: 10 }}
              placeholder="10-digit phone number"
              error={phonePatternError}
              helperText={
                phonePatternError
                  ? 'Phone must be exactly 10 digits'
                  : 'Optional, but must be exactly 10 digits if provided'
              }
            />

            <TextField
              className="full-width"
              variant="outlined"
              label="Password"
              type="password"
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
            />

            <TextField
              className="full-width"
              variant="outlined"
              select
              label="Role"
              value={role}
              onChange={(e) => onRoleChange(e.target.value as Role)}
              onBlur={() => setTouched((t) => ({ ...t, role: true }))}
            >
              <MenuItem value="STAFF">Staff</MenuItem>
              <MenuItem value="MANAGER">Manager</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
              <MenuItem value="AUDITOR">Auditor</MenuItem>
            </TextField>

            {requiresAdminSecret && (
              <TextField
                className="full-width"
                variant="outlined"
                label="Admin Secret"
                type="password"
                value={adminSecret}
                onChange={(e) => setAdminSecret(e.target.value)}
                onBlur={() => setTouched((t) => ({ ...t, adminSecret: true }))}
                required
                error={adminSecretRequiredError}
                helperText={adminSecretRequiredError ? 'Admin secret is required' : 'Required for admin registration'}
              />
            )}

            {role !== 'ADMIN' && role !== 'AUDITOR' && (
              <TextField
                className="full-width"
                variant="outlined"
                label="Assigned Warehouse ID"
                type="number"
                value={assignedWarehouseId}
                onChange={(e) => setAssignedWarehouseId(e.target.value)}
                helperText="Optional. Restricts user to a specific warehouse."
              />
            )}

            <Button
              className="full-width login-btn"
              variant="contained"
              color="primary"
              type="submit"
              disabled={loading || isFormInvalid}
            >
              {loading ? 'Registering...' : 'Register'}
            </Button>
          </form>
        </CardContent>

        <CardActions>
          <p className="register-link">Already have an account? <Link to="/login">Sign in</Link></p>
        </CardActions>
      </Card>
    </div>
  );
}
