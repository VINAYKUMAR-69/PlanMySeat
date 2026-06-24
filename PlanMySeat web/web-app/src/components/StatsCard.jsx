

export default function StatsCard({ title, value, icon, description, onClick }) {
  return (
    <div 
      className={`stats-card glass-card ${onClick ? 'clickable' : ''}`} 
      onClick={onClick}
      style={{ cursor: onClick ? 'pointer' : 'default' }}
    >
      <div className="stats-card-main">
        <div className="stats-card-details">
          <span className="stats-card-title">{title}</span>
          <h2 className="stats-card-value">{value}</h2>
        </div>
        <div className="stats-card-icon">{icon}</div>
      </div>
      {description && <p className="stats-card-desc">{description}</p>}

      <style dangerouslySetInnerHTML={{__html: `
        .stats-card {
          padding: 1.5rem;
          display: flex;
          flex-direction: column;
          gap: 0.75rem;
          position: relative;
          overflow: hidden;
        }

        .stats-card::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 4px;
          height: 100%;
          background: linear-gradient(to bottom, var(--primary-color), var(--accent-color));
          opacity: 0;
          transition: opacity var(--transition-fast);
        }

        .stats-card:hover::before {
          opacity: 1;
        }

        .stats-card.clickable:hover {
          transform: translateY(-4px);
        }

        .stats-card-main {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .stats-card-details {
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
        }

        .stats-card-title {
          font-family: var(--font-display);
          font-weight: 600;
          font-size: 0.85rem;
          color: var(--text-muted);
          text-transform: uppercase;
          letter-spacing: 0.05em;
        }

        .stats-card-value {
          font-size: 2.25rem;
          font-weight: 800;
          line-height: 1;
          color: var(--text-main);
          letter-spacing: -1px;
        }

        .stats-card-icon {
          font-size: 1.75rem;
          color: var(--primary-color);
          background: var(--primary-glow);
          width: 50px;
          height: 50px;
          border-radius: var(--radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;
          transition: transform var(--transition-normal);
        }

        .stats-card:hover .stats-card-icon {
          transform: scale(1.1) rotate(5deg);
        }

        .stats-card-desc {
          font-size: 0.8rem;
          color: var(--text-muted);
          border-top: 1px solid var(--card-border);
          padding-top: 0.5rem;
          margin-top: 0.25rem;
        }
      `}} />
    </div>
  );
}
