import React from 'react';
import { useApp } from '../context/AppContext';
import { FaBell, FaTrash, FaCheckCircle } from 'react-icons/fa';

export default function Notifications() {
  const { notifications, deleteNotification } = useApp();

  const handleDelete = async (id) => {
    try {
      await deleteNotification(id);
    } catch (e) {
      alert(e.message || 'Delete failed');
    }
  };

  return (
    <div className="notifications-view animate-fade-up">
      <div className="glass-card padding-md">
        <h3>System Notifications</h3>
        <p className="card-subtitle margin-bottom-md">Event logs triggered inside your organization</p>

        <div className="notifications-list margin-top-md">
          {notifications.map((notif) => (
            <div className="notif-item glass-card" key={notif.id}>
              <div className="notif-icon-box">
                <FaBell />
              </div>
              
              <div className="notif-content">
                <div className="notif-header-row">
                  <h4>{notif.title}</h4>
                  <span className="notif-time">{notif.date} | {notif.time || notif.sender}</span>
                </div>
                <p className="notif-text">{notif.message}</p>
                <span className="notif-sender">Logged by: <strong>{notif.sender || 'System'}</strong></span>
              </div>

              <div className="notif-actions">
                <button 
                  className="icon-action-btn delete" 
                  onClick={() => handleDelete(notif.id)}
                  title="Remove notification"
                >
                  <FaTrash />
                </button>
              </div>
            </div>
          ))}

          {notifications.length === 0 && (
            <div className="empty-state text-center">
              <FaCheckCircle className="empty-icon text-success" />
              <h3>Inbox Clear!</h3>
              <p>You have no new system alerts or optimization warnings.</p>
            </div>
          )}
        </div>
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .notifications-list {
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }

        .notif-item {
          padding: 1.25rem;
          display: flex;
          gap: 1.25rem;
          align-items: flex-start;
          transition: transform 0.2s;
        }

        .notif-item:hover {
          transform: translateX(4px);
        }

        .notif-icon-box {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: var(--primary-glow);
          color: var(--primary-color);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 1.1rem;
          flex-shrink: 0;
        }

        .notif-content {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
        }

        .notif-header-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          flex-wrap: wrap;
          gap: 0.5rem;
        }

        .notif-header-row h4 {
          font-size: 1rem;
          font-weight: 700;
        }

        .notif-time {
          font-size: 0.75rem;
          color: var(--text-muted);
        }

        .notif-text {
          font-size: 0.9rem;
          color: var(--text-main);
          line-height: 1.5;
        }

        .notif-sender {
          font-size: 0.75rem;
          color: var(--text-muted);
          margin-top: 0.25rem;
        }

        .notif-actions {
          align-self: center;
        }
      `}} />
    </div>
  );
}
