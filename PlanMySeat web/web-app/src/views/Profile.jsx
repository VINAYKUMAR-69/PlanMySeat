import { useState } from 'react';
import { useApp } from '../context/AppContext';
import { auth } from '../firebase';
import {
  reauthenticateWithCredential,
  EmailAuthProvider,
  updatePassword,
} from 'firebase/auth';
import { FaUser, FaBuilding, FaLock, FaCheckCircle, FaExclamationTriangle } from 'react-icons/fa';

export default function Profile() {
  const { user, triggerSystemNotification, updateUserProfile } = useApp();

  // Profile form fields
  const [fullName, setFullName] = useState(user?.name || '');
  const [collegeOrg, setCollegeOrg] = useState(user?.college || '');

  // Password fields
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // UI state messages
  const [profileMsg, setProfileMsg] = useState('');
  const [profileErr, setProfileErr] = useState('');
  const [pwMsg, setPwMsg] = useState('');
  const [pwErr, setPwErr] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    if (!fullName || !collegeOrg) return;

    setProfileMsg('');
    setProfileErr('');
    setIsSubmitting(true);
    try {
      await updateUserProfile(fullName, collegeOrg);
      setProfileMsg('Profile updated successfully!');
      await triggerSystemNotification('Profile Updated', 'Name and college organization updated.');
    } catch (e) {
      setProfileErr(e.message || 'Failed to update profile.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (!oldPassword || !newPassword || !confirmPassword) return;

    setPwMsg('');
    setPwErr('');

    if (newPassword !== confirmPassword) {
      setPwErr('Confirm password does not match.');
      return;
    }
    if (newPassword.length < 6) {
      setPwErr('New password must be at least 6 characters.');
      return;
    }

    setIsSubmitting(true);
    try {
      const firebaseUser = auth.currentUser;
      if (!firebaseUser) throw new Error('Not signed in.');

      // Re-authenticate before changing password (Firebase security requirement)
      const credential = EmailAuthProvider.credential(firebaseUser.email, oldPassword);
      await reauthenticateWithCredential(firebaseUser, credential);

      // Now update password in Firebase Auth
      await updatePassword(firebaseUser, newPassword);

      setPwMsg('Password changed successfully!');
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
      await triggerSystemNotification('Security Updated', 'User password was changed.');
    } catch (e) {
      if (e.code === 'auth/wrong-password' || e.code === 'auth/invalid-credential') {
        setPwErr('Current password is incorrect.');
      } else if (e.code === 'auth/weak-password') {
        setPwErr('New password is too weak. Use at least 6 characters.');
      } else {
        setPwErr(e.message || 'Error updating password.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="profile-view animate-fade-up">
      <div className="profile-grid">
        
        {/* Left Side: General Profile Info */}
        <form onSubmit={handleUpdateProfile} className="glass-card padding-md">
          <h3>Profile Credentials</h3>
          <p className="card-subtitle margin-bottom-md">Manage organization coordinates</p>

          {profileMsg && (
            <div className="alert-msg alert-success flex items-center gap-xs">
              <FaCheckCircle /> <span>{profileMsg}</span>
            </div>
          )}
          {profileErr && (
            <div className="alert-msg alert-danger flex items-center gap-xs">
              <FaExclamationTriangle /> <span>{profileErr}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Full Name</label>
            <div className="input-with-icon">
              <FaUser className="input-icon" />
              <input 
                type="text" 
                className="form-input" 
                value={fullName}
                onChange={e => setFullName(e.target.value)}
                required
                id="profile-fullname"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Email Address (Read-only)</label>
            <input 
              type="email" 
              className="form-input" 
              value={user?.email || ''} 
              disabled 
            />
          </div>

          <div className="form-group">
            <label className="form-label">College / Organization</label>
            <div className="input-with-icon">
              <FaBuilding className="input-icon" />
              <input 
                type="text" 
                className="form-input" 
                value={collegeOrg}
                onChange={e => setCollegeOrg(e.target.value)}
                required
                id="profile-college"
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting} id="btn-save-profile">
            {isSubmitting ? 'Updating...' : 'Save Profile Changes'}
          </button>
        </form>

        {/* Right Side: Change Password */}
        <form onSubmit={handleChangePassword} className="glass-card padding-md">
          <h3>Security Settings</h3>
          <p className="card-subtitle margin-bottom-md">Reset user login password</p>

          {pwMsg && (
            <div className="alert-msg alert-success flex items-center gap-xs">
              <FaCheckCircle /> <span>{pwMsg}</span>
            </div>
          )}
          {pwErr && (
            <div className="alert-msg alert-danger flex items-center gap-xs">
              <FaExclamationTriangle /> <span>{pwErr}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Current Password</label>
            <div className="input-with-icon">
              <FaLock className="input-icon" />
              <input 
                type="password" 
                className="form-input" 
                placeholder="••••••••"
                value={oldPassword}
                onChange={e => setOldPassword(e.target.value)}
                required
                id="profile-old-password"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">New Password</label>
            <div className="input-with-icon">
              <FaLock className="input-icon" />
              <input 
                type="password" 
                className="form-input" 
                placeholder="••••••••"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
                id="profile-new-password"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Confirm New Password</label>
            <div className="input-with-icon">
              <FaLock className="input-icon" />
              <input 
                type="password" 
                className="form-input" 
                placeholder="••••••••"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
                id="profile-confirm-password"
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={isSubmitting} id="btn-save-password">
            {isSubmitting ? 'Updating...' : 'Update Password'}
          </button>
        </form>

      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .profile-grid {
          display: grid;
          grid-template-columns: 1.2fr 1fr;
          gap: 2rem;
        }

        .gap-xs {
          gap: 0.35rem;
        }

        .items-center {
          align-items: center;
        }

        @media (max-width: 900px) {
          .profile-grid {
            grid-template-columns: 1fr;
          }
        }
      `}} />
    </div>
  );
}
