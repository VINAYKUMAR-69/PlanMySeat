import { useApp } from '../context/AppContext';

export default function Header({ title }) {
  const { user } = useApp();
  
  const formatDate = () => {
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    return new Date().toLocaleDateString(undefined, options);
  };

  return (
    <header className="header-container">
      <div className="header-title-section">
        <h1>{title}</h1>
        <p className="header-date">{formatDate()}</p>
      </div>
      
      {user && (
        <div className="header-meta">
          <div className="header-welcome">
            <span>Welcome back,</span>
            <strong>{user.name}</strong>
          </div>
          <div className="role-tag">
            {user.role}
          </div>
        </div>
      )}

      <style dangerouslySetInnerHTML={{__html: `
        .header-container {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 2rem;
          padding-bottom: 1.25rem;
          border-bottom: 1px solid var(--card-border);
        }

        .header-title-section h1 {
          font-size: 1.75rem;
          font-weight: 800;
          color: var(--text-main);
        }

        .header-date {
          font-size: 0.85rem;
          color: var(--text-muted);
          margin-top: 0.25rem;
        }

        .header-meta {
          display: flex;
          align-items: center;
          gap: 1rem;
        }

        .header-welcome {
          text-align: right;
          display: flex;
          flex-direction: column;
          font-size: 0.9rem;
        }

        .header-welcome span {
          color: var(--text-muted);
          font-size: 0.8rem;
        }

        .header-welcome strong {
          color: var(--text-main);
          font-weight: 600;
        }

        .role-tag {
          padding: 0.35rem 0.85rem;
          background: var(--primary-glow);
          color: var(--primary-color);
          border: 1px solid rgba(99, 102, 241, 0.2);
          border-radius: var(--radius-sm);
          font-size: 0.75rem;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.05em;
        }

        @media (max-width: 768px) {
          .header-container {
            flex-direction: column;
            align-items: flex-start;
            gap: 1rem;
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
          }

          .header-welcome {
            text-align: left;
          }
        }
      `}} />
    </header>
  );
}
