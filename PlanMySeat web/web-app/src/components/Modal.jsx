import { FaTimes } from 'react-icons/fa';

export default function Modal({ isOpen, onClose, title, children }) {
  if (!isOpen) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container glass-card" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{title}</h3>
          <button className="modal-close" onClick={onClose} aria-label="Close modal">
            <FaTimes />
          </button>
        </div>
        <div className="modal-content">
          {children}
        </div>
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .modal-backdrop {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.4);
          backdrop-filter: blur(4px);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
          padding: 1rem;
          animation: fadeIn 0.2s ease forwards;
        }

        .modal-container {
          width: 100%;
          max-width: 500px;
          background: var(--card-bg);
          border-radius: var(--radius-md);
          overflow: hidden;
          box-shadow: var(--shadow-lg);
          display: flex;
          flex-direction: column;
          max-height: 90vh;
          animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }

        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1.25rem 1.5rem;
          border-bottom: 1px solid var(--card-border);
        }

        .modal-header h3 {
          font-size: 1.15rem;
          font-weight: 700;
        }

        .modal-close {
          background: transparent;
          border: none;
          color: var(--text-muted);
          cursor: pointer;
          font-size: 1.1rem;
          display: flex;
          align-items: center;
          padding: 0.25rem;
          border-radius: 50%;
          transition: background-color 0.2s, color 0.2s;
        }

        .modal-close:hover {
          background-color: var(--bg-color);
          color: var(--text-main);
        }

        .modal-content {
          padding: 1.5rem;
          overflow-y: auto;
          flex: 1;
        }

        @keyframes fadeIn {
          from { opacity: 0; }
          to { opacity: 1; }
        }

        @keyframes slideUp {
          from { transform: translateY(30px); opacity: 0; }
          to { transform: translateY(0); opacity: 1; }
        }
      `}} />
    </div>
  );
}
