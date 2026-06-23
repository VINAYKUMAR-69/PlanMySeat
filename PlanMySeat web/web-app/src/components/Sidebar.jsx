import React from 'react';
import { useApp } from '../context/AppContext';
import { 
  FaHome, 
  FaUserGraduate, 
  FaDoorOpen, 
  FaChair, 
  FaClipboardList, 
  FaUsers, 
  FaBell, 
  FaUser, 
  FaLifeRing, 
  FaCog, 
  FaSignOutAlt, 
  FaMoon, 
  FaSun 
} from 'react-icons/fa';

export default function Sidebar({ currentView, setCurrentView }) {
  const { user, logoutUser, theme, toggleTheme, notifications } = useApp();

  if (!user) return null;

  const isFaculty = user.role === 'faculty';
  const unreadNotificationsCount = notifications.length;

  const menuItems = isFaculty 
    ? [
        { id: 'dashboard', label: 'Faculty Panel', icon: <FaHome /> },
        { id: 'reports', label: 'Duty Schedule', icon: <FaClipboardList /> },
        { id: 'faculties', label: 'All Faculty', icon: <FaUsers /> },
        { id: 'support', label: 'Support & Feedback', icon: <FaLifeRing /> },
        { id: 'profile', label: 'My Profile', icon: <FaUser /> },
      ]
    : [
        { id: 'dashboard', label: 'Dashboard', icon: <FaHome /> },
        { id: 'students', label: 'Students Directory', icon: <FaUserGraduate /> },
        { id: 'rooms', label: 'Rooms Inventory', icon: <FaDoorOpen /> },
        { id: 'seating-setup', label: 'Seat Allocation', icon: <FaChair /> },
        { id: 'reports', label: 'Seating Plans', icon: <FaClipboardList /> },
        { id: 'faculties', label: 'Invigilators', icon: <FaUsers /> },
        { id: 'notifications', label: 'Notifications', icon: <FaBell />, badge: unreadNotificationsCount },
        { id: 'support', label: 'Support Center', icon: <FaLifeRing /> },
        { id: 'profile', label: 'Profile Settings', icon: <FaUser /> },
      ];

  return (
    <aside className="sidebar-container">
      <div className="sidebar-brand">
        <div className="logo-icon">P</div>
        <h2>PlanMySeat</h2>
      </div>

      <div className="sidebar-user">
        <div className="user-avatar">
          {user.name ? user.name.slice(0, 2).toUpperCase() : 'US'}
        </div>
        <div className="user-info">
          <h4>{user.name || 'User'}</h4>
          <span className="user-role">{user.role || 'Admin'}</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <ul>
          {menuItems.map((item) => (
            <li key={item.id}>
              <button 
                className={`nav-link ${currentView === item.id ? 'active' : ''}`}
                onClick={() => setCurrentView(item.id)}
                id={`sidebar-link-${item.id}`}
              >
                <span className="nav-icon">{item.icon}</span>
                <span className="nav-label">{item.label}</span>
                {item.badge > 0 && (
                  <span className="nav-badge">{item.badge}</span>
                )}
              </button>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <button className="theme-toggle-btn" onClick={toggleTheme} title="Toggle Theme">
          {theme === 'light' ? <FaMoon /> : <FaSun />}
          <span>{theme === 'light' ? 'Dark Mode' : 'Light Mode'}</span>
        </button>
        <button className="logout-btn" onClick={logoutUser} id="btn-logout">
          <FaSignOutAlt />
          <span>Sign Out</span>
        </button>
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .sidebar-container {
          width: 260px;
          background: var(--sidebar-bg);
          color: var(--sidebar-text);
          display: flex;
          flex-direction: column;
          padding: 1.5rem;
          border-right: 1px solid var(--card-border);
          transition: width var(--transition-normal);
          flex-shrink: 0;
        }

        .sidebar-brand {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          margin-bottom: 2rem;
        }

        .logo-icon {
          width: 38px;
          height: 38px;
          background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
          border-radius: var(--radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;
          font-family: var(--font-display);
          font-weight: 800;
          font-size: 1.25rem;
          color: white;
          box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
        }

        .sidebar-brand h2 {
          font-family: var(--font-display);
          font-size: 1.25rem;
          font-weight: 700;
          color: white;
          letter-spacing: -0.5px;
        }

        .sidebar-user {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          padding: 0.75rem;
          background: rgba(255, 255, 255, 0.05);
          border-radius: var(--radius-sm);
          margin-bottom: 2rem;
        }

        .user-avatar {
          width: 42px;
          height: 42px;
          border-radius: var(--radius-full);
          background: linear-gradient(135deg, var(--accent-color), var(--primary-color));
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          font-size: 0.9rem;
          border: 2px solid rgba(255, 255, 255, 0.1);
        }

        .user-info h4 {
          font-size: 0.95rem;
          font-weight: 600;
          color: white;
        }

        .user-role {
          font-size: 0.75rem;
          color: var(--text-muted);
          text-transform: capitalize;
          display: block;
        }

        .sidebar-nav {
          flex: 1;
        }

        .sidebar-nav ul {
          list-style: none;
          display: flex;
          flex-direction: column;
          gap: 0.35rem;
        }

        .nav-link {
          width: 100%;
          display: flex;
          align-items: center;
          gap: 0.75rem;
          padding: 0.75rem 1rem;
          background: transparent;
          border: none;
          color: rgba(255, 255, 255, 0.7);
          font-family: var(--font-sans);
          font-size: 0.95rem;
          font-weight: 500;
          text-align: left;
          cursor: pointer;
          border-radius: var(--radius-sm);
          transition: all var(--transition-fast);
          position: relative;
        }

        .nav-link:hover {
          color: white;
          background: rgba(255, 255, 255, 0.04);
        }

        .nav-link.active {
          color: white;
          background: var(--primary-color);
          box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
        }

        .nav-icon {
          font-size: 1.1rem;
          display: flex;
          align-items: center;
        }

        .nav-badge {
          background: var(--error-color);
          color: white;
          font-size: 0.7rem;
          padding: 0.15rem 0.45rem;
          border-radius: var(--radius-full);
          margin-left: auto;
          font-weight: 700;
        }

        .sidebar-footer {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          margin-top: auto;
          border-top: 1px solid rgba(255, 255, 255, 0.08);
          padding-top: 1rem;
        }

        .theme-toggle-btn, .logout-btn {
          width: 100%;
          display: flex;
          align-items: center;
          gap: 0.75rem;
          padding: 0.65rem 1rem;
          background: transparent;
          border: none;
          color: rgba(255, 255, 255, 0.6);
          font-family: var(--font-sans);
          font-size: 0.9rem;
          cursor: pointer;
          border-radius: var(--radius-sm);
          text-align: left;
          transition: all var(--transition-fast);
        }

        .theme-toggle-btn:hover {
          color: white;
          background: rgba(255, 255, 255, 0.05);
        }

        .logout-btn:hover {
          color: white;
          background: rgba(239, 68, 68, 0.15);
        }

        @media (max-width: 768px) {
          .sidebar-container {
            width: 100%;
            height: auto;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 100;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            padding: 0.75rem 1rem;
            background: var(--sidebar-bg);
            border-bottom: 1px solid var(--card-border);
            border-right: none;
          }
          
          .sidebar-brand {
            margin-bottom: 0;
          }

          .sidebar-user, .sidebar-nav, .sidebar-footer span {
            display: none;
          }

          .sidebar-footer {
            flex-direction: row;
            margin-top: 0;
            border-top: none;
            padding-top: 0;
            gap: 0.5rem;
          }
          
          .theme-toggle-btn, .logout-btn {
            padding: 0.5rem;
            width: auto;
          }
        }
      `}} />
    </aside>
  );
}
