import { useState } from 'react';

// --- Bar Chart Component ---
export function BarChart({ data = {} }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);
  const entries = Object.entries(data);

  if (entries.length === 0) {
    return <div className="chart-empty">No data to display</div>;
  }

  const values = entries.map(([, val]) => val);
  const maxValue = Math.max(...values, 1);

  // Layout parameters
  const height = 200;
  const width = 400;
  const paddingLeft = 40;
  const paddingBottom = 30;
  const paddingTop = 20;
  const paddingRight = 20;

  const chartWidth = width - paddingLeft - paddingRight;
  const chartHeight = height - paddingTop - paddingBottom;
  const barWidth = entries.length > 0 ? (chartWidth / entries.length) * 0.6 : 0;
  const barGap = entries.length > 0 ? (chartWidth / entries.length) * 0.4 : 0;

  const colors = [
    'hsl(250, 84%, 54%)',
    'hsl(262, 83%, 58%)',
    'hsl(142, 72%, 29%)',
    'hsl(38, 92%, 50%)',
    'hsl(0, 72%, 51%)',
    'hsl(200, 80%, 50%)'
  ];

  return (
    <div className="chart-container">
      <svg viewBox={`0 0 ${width} ${height}`} className="chart-svg">
        {/* Y Axis Grid Lines */}
        {[0, 0.25, 0.5, 0.75, 1].map((ratio, idx) => {
          const y = paddingTop + chartHeight * (1 - ratio);
          const valLabel = Math.round(maxValue * ratio);
          return (
            <g key={idx}>
              <line 
                x1={paddingLeft} 
                y1={y} 
                x2={width - paddingRight} 
                y2={y} 
                stroke="var(--card-border)" 
                strokeDasharray="4 4" 
              />
              <text 
                x={paddingLeft - 8} 
                y={y + 4} 
                textAnchor="end" 
                fontSize="10" 
                fill="var(--text-muted)"
              >
                {valLabel}
              </text>
            </g>
          );
        })}

        {/* Bars */}
        {entries.map(([label, val], idx) => {
          const barHeight = (val / maxValue) * chartHeight;
          const x = paddingLeft + idx * (barWidth + barGap) + barGap / 2;
          const y = paddingTop + chartHeight - barHeight;
          const color = colors[idx % colors.length];

          return (
            <g 
              key={label}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
              style={{ cursor: 'pointer' }}
            >
              <rect
                x={x}
                y={y}
                width={barWidth}
                height={barHeight}
                fill={color}
                rx="4"
                opacity={hoveredIdx === null || hoveredIdx === idx ? 1 : 0.6}
                style={{ transition: 'all 0.2s' }}
              />
              {/* Tooltip text when hovered */}
              {hoveredIdx === idx && (
                <g>
                  <rect 
                    x={x - 20} 
                    y={y - 25} 
                    width={barWidth + 40} 
                    height={20} 
                    rx="3" 
                    fill="var(--sidebar-bg)" 
                  />
                  <text 
                    x={x + barWidth / 2} 
                    y={y - 11} 
                    textAnchor="middle" 
                    fontSize="9" 
                    fontWeight="bold" 
                    fill="white"
                  >
                    {val}
                  </text>
                </g>
              )}
              {/* X Axis labels */}
              <text
                x={x + barWidth / 2}
                y={height - paddingBottom + 16}
                textAnchor="middle"
                fontSize="9"
                fontWeight="500"
                fill="var(--text-muted)"
              >
                {label.length > 8 ? `${label.slice(0, 6)}..` : label}
              </text>
            </g>
          );
        })}

        {/* X Axis Line */}
        <line 
          x1={paddingLeft} 
          y1={height - paddingBottom} 
          x2={width - paddingRight} 
          y2={height - paddingBottom} 
          stroke="var(--card-border)" 
          strokeWidth="1.5"
        />
      </svg>
    </div>
  );
}

// --- Pie / Donut Chart Component ---
export function PieChart({ data = {}, donut = false }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);
  const entries = Object.entries(data);

  if (entries.length === 0) {
    return <div className="chart-empty">No data to display</div>;
  }

  const total = entries.reduce((acc, [, v]) => acc + v, 0);

  const colors = [
    'hsl(250, 84%, 54%)',
    'hsl(262, 83%, 58%)',
    'hsl(142, 72%, 29%)',
    'hsl(38, 92%, 50%)',
    'hsl(0, 72%, 51%)',
    'hsl(200, 80%, 50%)'
  ];

  let accumulatedAngle = 0;
  const radius = 70;
  const innerRadius = donut ? 42 : 0;
  const cx = 80;
  const cy = 80;

  const getCoordinatesForPercent = (percent) => {
    const x = Math.cos(2 * Math.PI * percent);
    const y = Math.sin(2 * Math.PI * percent);
    return [x, y];
  };

  const slices = entries.map(([label, val], idx) => {
    const percent = val / total;
    const startAngle = accumulatedAngle;
    accumulatedAngle += percent;
    const endAngle = accumulatedAngle;

    // SVG path drawing logic for donut/pie
    const start = getCoordinatesForPercent(startAngle);
    const end = getCoordinatesForPercent(endAngle);
    const largeArcFlag = percent > 0.5 ? 1 : 0;

    // Arc path formula
    const pathData = donut
      ? [
          `M ${cx + start[0] * radius} ${cy + start[1] * radius}`,
          `A ${radius} ${radius} 0 ${largeArcFlag} 1 ${cx + end[0] * radius} ${cy + end[1] * radius}`,
          `L ${cx + end[0] * innerRadius} ${cy + end[1] * innerRadius}`,
          `A ${innerRadius} ${innerRadius} 0 ${largeArcFlag} 0 ${cx + start[0] * innerRadius} ${cy + start[1] * innerRadius}`,
          'Z'
        ].join(' ')
      : [
          `M ${cx} ${cy}`,
          `L ${cx + start[0] * radius} ${cy + start[1] * radius}`,
          `A ${radius} ${radius} 0 ${largeArcFlag} 1 ${cx + end[0] * radius} ${cy + end[1] * radius}`,
          'Z'
        ].join(' ');

    return {
      label,
      val,
      pathData,
      color: colors[idx % colors.length],
      percent: Math.round(percent * 100)
    };
  });

  return (
    <div className="pie-chart-wrapper">
      <svg viewBox="0 0 180 160" width="180" height="160">
        <g transform="rotate(-90 80 80)">
          {slices.map((slice, idx) => (
            <path
              key={slice.label}
              d={slice.pathData}
              fill={slice.color}
              opacity={hoveredIdx === null || hoveredIdx === idx ? 1 : 0.6}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
              style={{ transition: 'all 0.2s', cursor: 'pointer' }}
            />
          ))}
        </g>
      </svg>

      <div className="pie-legend">
        {slices.map((slice, idx) => (
          <div 
            className={`legend-item ${hoveredIdx === idx ? 'highlighted' : ''}`}
            key={slice.label}
            onMouseEnter={() => setHoveredIdx(idx)}
            onMouseLeave={() => setHoveredIdx(null)}
          >
            <span className="legend-dot" style={{ backgroundColor: slice.color }}></span>
            <span className="legend-label">{slice.label}</span>
            <span className="legend-value">{slice.val} ({slice.percent}%)</span>
          </div>
        ))}
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .pie-chart-wrapper {
          display: flex;
          align-items: center;
          gap: 1rem;
          justify-content: center;
        }

        .pie-legend {
          display: flex;
          flex-direction: column;
          gap: 0.35rem;
          max-height: 150px;
          overflow-y: auto;
          flex: 1;
        }

        .legend-item {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.8rem;
          padding: 0.2rem 0.4rem;
          border-radius: 4px;
          transition: background-color 0.2s;
        }

        .legend-item.highlighted {
          background-color: var(--card-border);
        }

        .legend-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          flex-shrink: 0;
        }

        .legend-label {
          color: var(--text-main);
          font-weight: 500;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          max-width: 80px;
        }

        .legend-value {
          color: var(--text-muted);
          margin-left: auto;
        }
      `}} />
    </div>
  );
}

// Donut alias
export function DonutChart({ data }) {
  return <PieChart data={data} donut={true} />;
}

// --- Line Chart Component ---
export function LineChart({ data = [] }) {
  if (data.length === 0) {
    // Generate some mock activity timeline if empty
    data = [40, 20, 80, 50, 90, 60, 100];
  }

  const height = 150;
  const width = 350;
  const padding = 20;

  const chartWidth = width - padding * 2;
  const chartHeight = height - padding * 2;
  const maxVal = Math.max(...data, 1);

  const points = data.map((val, idx) => {
    const x = padding + (idx / (data.length - 1)) * chartWidth;
    const y = padding + chartHeight - (val / maxVal) * chartHeight;
    return `${x},${y}`;
  }).join(' ');

  return (
    <div className="chart-container">
      <svg viewBox={`0 0 ${width} ${height}`} className="chart-svg">
        {/* Draw Path */}
        <polyline
          fill="none"
          stroke="var(--primary-color)"
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
          points={points}
        />
        {/* Draw Area */}
        <polygon
          fill="var(--primary-glow)"
          points={`${padding},${height - padding} ${points} ${width - padding},${height - padding}`}
        />
        
        {/* Grid and Dots */}
        {data.map((val, idx) => {
          const x = padding + (idx / (data.length - 1)) * chartWidth;
          const y = padding + chartHeight - (val / maxVal) * chartHeight;
          return (
            <g key={idx} className="line-dot-group">
              <circle
                cx={x}
                cy={y}
                r="4"
                fill="white"
                stroke="var(--primary-color)"
                strokeWidth="2"
              />
              <text
                x={x}
                y={y - 8}
                textAnchor="middle"
                fontSize="9"
                fontWeight="bold"
                fill="var(--text-main)"
                className="line-dot-text"
              >
                {val}
              </text>
            </g>
          );
        })}
      </svg>
      <style dangerouslySetInnerHTML={{__html: `
        .line-dot-text {
          opacity: 0;
          transition: opacity 0.2s;
        }
        .line-dot-group:hover .line-dot-text {
          opacity: 1;
        }
      `}} />
    </div>
  );
}
