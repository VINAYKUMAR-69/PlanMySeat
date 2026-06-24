import { useState } from 'react';
import { useApp } from '../context/AppContext';
import { FaEye, FaEyeSlash, FaLock, FaEnvelope, FaUser, FaBuilding } from 'react-icons/fa';

export default function Login() {
  const { loginUser, registerUser, forgotPassword } = useApp();
  
  // Modes: 'login' | 'register' | 'forgot'
  const [mode, setMode] = useState('login');
  
  // Form Fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState('principal');
  const [collegeOrg, setCollegeOrg] = useState('');
  
  // UI States
  const [showPassword, setShowPassword] = useState(false);
  const [err, setErr] = useState('');
  const [info, setInfo] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resetMessages = () => {
    setErr('');
    setInfo('');
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) return;
    
    resetMessages();
    setIsSubmitting(true);
    try {
      await loginUser(email, password);
    } catch (e) {
      setErr(e.message || 'Invalid Credentials');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!email || !password || !fullName || !collegeOrg) {
      setErr('Please fill in all fields');
      return;
    }

    resetMessages();
    setIsSubmitting(true);
    try {
      await registerUser(fullName, email, password, role, collegeOrg);
      setInfo('Account created successfully! Please sign in.');
      setMode('login');
      // Clear fields
      setPassword('');
    } catch (e) {
      setErr(e.message || 'Registration failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if (!email) return;

    resetMessages();
    setIsSubmitting(true);
    try {
      await forgotPassword(email);
      setInfo('Password reset email sent! Check your inbox and follow the link to reset your password.');
    } catch (e) {
      // Provide a friendly message for common Firebase Auth errors
      if (e.code === 'auth/user-not-found') {
        setErr('No account found with that email address.');
      } else if (e.code === 'auth/invalid-email') {
        setErr('Please enter a valid email address.');
      } else {
        setErr(e.message || 'Error sending reset email. Please try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-view animate-fade-up">
      <div className="auth-card glass-card">
        <div className="auth-header">
          <div className="logo-box">P</div>
          <h2>PlanMySeat</h2>
          <p>
            {mode === 'login' && 'Enter credentials to access the system'}
            {mode === 'register' && 'Create an account to start managing seat plans'}
            {mode === 'forgot' && 'Enter your email to receive a password reset link'}
          </p>
        </div>

        {err && <div className="alert-msg alert-danger">{err}</div>}
        {info && <div className="alert-msg alert-success">{info}</div>}

        {/* --- Login Mode --- */}
        {mode === 'login' && (
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-with-icon">
                <FaEnvelope className="input-icon" />
                <input 
                  type="email" 
                  className="form-input" 
                  placeholder="admin@example.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required 
                  id="login-email"
                />
              </div>
            </div>

            <div className="form-group">
              <div className="label-row">
                <label className="form-label">Password</label>
                <button 
                  type="button" 
                  className="forgot-link-btn"
                  onClick={() => { resetMessages(); setMode('forgot'); }}
                >
                  Forgot password?
                </button>
              </div>
              <div className="input-with-icon">
                <FaLock className="input-icon" />
                <input 
                  type={showPassword ? 'text' : 'password'} 
                  className="form-input" 
                  placeholder="••••••••"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  required 
                  id="login-password"
                />
                <button 
                  type="button" 
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <FaEyeSlash /> : <FaEye />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting} id="btn-submit-login">
              {isSubmitting ? 'Signing In...' : 'Sign In'}
            </button>
            
            <p className="auth-footer">
              Don't have an account?{' '}
              <button type="button" onClick={() => { resetMessages(); setMode('register'); }} className="text-btn">
                Sign Up
              </button>
            </p>
          </form>
        )}

        {/* --- Register Mode --- */}
        {mode === 'register' && (
          <form onSubmit={handleRegister}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <div className="input-with-icon">
                <FaUser className="input-icon" />
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="John Doe"
                  value={fullName}
                  onChange={e => setFullName(e.target.value)}
                  required 
                  id="register-fullname"
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-with-icon">
                <FaEnvelope className="input-icon" />
                <input 
                  type="email" 
                  className="form-input" 
                  placeholder="john@example.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required 
                  id="register-email"
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-with-icon">
                <FaLock className="input-icon" />
                <input 
                  type={showPassword ? 'text' : 'password'} 
                  className="form-input" 
                  placeholder="••••••••"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  required 
                  id="register-password"
                />
                <button 
                  type="button" 
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <FaEyeSlash /> : <FaEye />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Select System Role</label>
              <select 
                className="form-input" 
                value={role} 
                onChange={e => setRole(e.target.value)}
                id="register-role"
              >
                <option value="principal">Principal (Administrator)</option>
                <option value="faculty">Faculty Member</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">College / Organization</label>
              <div className="input-with-icon">
                <FaBuilding className="input-icon" />
                <input 
                  type="text" 
                  className="form-input" 
                  placeholder="IIT Delhi"
                  value={collegeOrg}
                  onChange={e => setCollegeOrg(e.target.value)}
                  required 
                  id="register-college"
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting} id="btn-submit-register">
              {isSubmitting ? 'Creating Account...' : 'Create Account'}
            </button>

            <p className="auth-footer">
              Already have an account?{' '}
              <button type="button" onClick={() => { resetMessages(); setMode('login'); }} className="text-btn">
                Sign In
              </button>
            </p>
          </form>
        )}

        {/* --- Forgot Password Mode --- */}
        {mode === 'forgot' && (
          <form onSubmit={handleForgotPassword}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-with-icon">
                <FaEnvelope className="input-icon" />
                <input 
                  type="email" 
                  className="form-input" 
                  placeholder="admin@example.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required 
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting}>
              {isSubmitting ? 'Sending code...' : 'Send Verification OTP'}
            </button>

            <p className="auth-footer">
              Back to{' '}
              <button type="button" onClick={() => { resetMessages(); setMode('login'); }} className="text-btn">
                Sign In
              </button>
            </p>
          </form>
        )}

        {/* OTP/Reset modes removed — Firebase sends a secure reset link directly to the user's email */}
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .auth-view {
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 1.5rem;
          background: radial-gradient(circle at center, var(--primary-glow) 0%, var(--bg-color) 80%);
        }

        .auth-card {
          width: 100%;
          max-width: 440px;
          padding: 2.5rem;
        }

        .auth-header {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          margin-bottom: 2rem;
          gap: 0.5rem;
        }

        .auth-header h2 {
          font-size: 1.5rem;
          font-weight: 800;
        }

        .auth-header p {
          font-size: 0.85rem;
          color: var(--text-muted);
        }

        .input-with-icon {
          position: relative;
          display: flex;
          align-items: center;
        }

        .input-icon {
          position: absolute;
          left: 1rem;
          color: var(--text-muted);
          font-size: 0.9rem;
          pointer-events: none;
        }

        .input-with-icon .form-input {
          padding-left: 2.5rem;
        }

        .password-toggle {
          position: absolute;
          right: 1rem;
          background: transparent;
          border: none;
          color: var(--text-muted);
          cursor: pointer;
          font-size: 1rem;
          display: flex;
          align-items: center;
        }

        .password-toggle:hover {
          color: var(--text-main);
        }

        .label-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .forgot-link-btn {
          background: transparent;
          border: none;
          color: var(--primary-color);
          font-size: 0.8rem;
          font-weight: 500;
          cursor: pointer;
          font-family: var(--font-sans);
        }

        .forgot-link-btn:hover {
          text-decoration: underline;
        }

        .btn-block {
          width: 100%;
          margin-top: 1rem;
        }

        .auth-footer {
          text-align: center;
          font-size: 0.85rem;
          color: var(--text-muted);
          margin-top: 1.5rem;
        }

        .text-btn {
          background: transparent;
          border: none;
          color: var(--primary-color);
          font-weight: 600;
          cursor: pointer;
          font-family: var(--font-sans);
          font-size: 0.85rem;
        }

        .text-btn:hover {
          text-decoration: underline;
        }

        .alert-msg {
          padding: 0.75rem 1rem;
          border-radius: var(--radius-sm);
          font-size: 0.85rem;
          font-weight: 500;
          margin-bottom: 1.5rem;
        }

        .alert-danger {
          background: var(--error-bg);
          color: var(--error-color);
          border: 1px solid rgba(239, 68, 68, 0.2);
        }

        .alert-success {
          background: var(--success-bg);
          color: var(--success-color);
          border: 1px solid rgba(16, 185, 129, 0.2);
        }

        .text-center {
          text-align: center;
        }
      `}} />
    </div>
  );
}
