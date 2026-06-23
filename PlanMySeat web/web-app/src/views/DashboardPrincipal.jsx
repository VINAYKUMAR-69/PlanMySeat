import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import StatsCard from '../components/StatsCard';
import { BarChart, PieChart } from '../components/Charts';
import { FaUserGraduate, FaDoorOpen, FaChair, FaChartPie, FaPlus, FaCalendarPlus, FaSync, FaRobot, FaCheckCircle, FaSlidersH } from 'react-icons/fa';
import Modal from '../components/Modal';

export default function DashboardPrincipal({ setCurrentView }) {
  const { students, rooms, notifications, currentSeatingPlan, triggerSystemNotification } = useApp();
  
  // AI Optimization Modal state
  const [isAiModalOpen, setIsAiModalOpen] = useState(false);
  const [spacingDensity, setSpacingDensity] = useState('alternate'); // 'compact' | 'alternate' | 'double'
  const [branchShuffle, setBranchShuffle] = useState(true);
  const [fillRatio, setFillRatio] = useState(75);
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [optComplete, setOptComplete] = useState(false);

  // Stats Calculations
  const totalStudents = students.length;
  const totalRooms = rooms.length;
  const totalBranches = [...new Set(students.map(s => s.branch).filter(Boolean))].length;
  const totalAllocated = currentSeatingPlan ? currentSeatingPlan.totalStudents : 0;

  // Chart data
  const branchCounts = students.reduce((acc, student) => {
    if (student.branch) {
      acc[student.branch] = (acc[student.branch] || 0) + 1;
    }
    return acc;
  }, {});

  // Recent allocations mapping from current seating plan
  const recentAllocations = currentSeatingPlan 
    ? currentSeatingPlan.allocations.slice(0, 5) 
    : [];

  const handleRunAiOptimize = () => {
    setIsOptimizing(true);
    setOptComplete(false);
    
    // Simulate AI packing layout algorithm
    setTimeout(async () => {
      setIsOptimizing(false);
      setOptComplete(true);
      await triggerSystemNotification(
        "AI Optimization Completed", 
        `Seating layouts optimized using ${spacingDensity} spacing constraints and ${fillRatio}% max room limits.`
      );
    }, 2000);
  };

  return (
    <div className="dashboard-view animate-fade-up">
      {/* Overview stats cards grid */}
      <div className="dashboard-grid">
        <StatsCard 
          title="Total Students" 
          value={totalStudents} 
          icon={<FaUserGraduate />} 
          description={`${totalBranches} academic branches registered`}
          onClick={() => setCurrentView('students')}
        />
        <StatsCard 
          title="Rooms Available" 
          value={totalRooms} 
          icon={<FaDoorOpen />} 
          description={`Total seating capacity: ${rooms.reduce((acc, r) => acc + (r.capacity || 0), 0)}`}
          onClick={() => setCurrentView('rooms')}
        />
        <StatsCard 
          title="Seats Allocated" 
          value={totalAllocated} 
          icon={<FaChair />} 
          description={currentSeatingPlan ? `Exam: ${currentSeatingPlan.examType}` : 'No active allocations'}
          onClick={() => setCurrentView('reports')}
        />
        <StatsCard 
          title="Active Branches" 
          value={totalBranches} 
          icon={<FaChartPie />} 
          description="Branch-wise student mappings"
          onClick={() => setCurrentView('students')}
        />
      </div>

      {/* Main content split: Charts & Recent actions */}
      <div className="dashboard-split-layout">
        
        {/* Left Side: Distributions visualization */}
        <div className="split-column glass-card">
          <div className="card-header">
            <h3>Branch Distribution</h3>
            <p className="card-subtitle">Student density per academic department</p>
          </div>
          <div className="charts-flex">
            <div className="chart-block">
              <h4>Bar View</h4>
              <BarChart data={branchCounts} />
            </div>
            <div className="chart-block">
              <h4>Share View</h4>
              <PieChart data={branchCounts} />
            </div>
          </div>
        </div>

        {/* Right Side: Quick activities */}
        <div className="split-column actions-column">
          {/* Quick links card */}
          <div className="glass-card padding-md margin-bottom-md">
            <h3>Quick Actions</h3>
            <p className="card-subtitle margin-bottom-md">Frequently used seating routines</p>
            <div className="quick-actions-grid">
              <button className="action-card" onClick={() => setCurrentView('students')} id="btn-quick-add-student">
                <FaPlus />
                <span>Add Student</span>
              </button>
              <button className="action-card" onClick={() => setCurrentView('rooms')} id="btn-quick-add-room">
                <FaPlus />
                <span>Add Room</span>
              </button>
              <button className="action-card highlight" onClick={() => setCurrentView('seating-setup')} id="btn-quick-allocate">
                <FaCalendarPlus />
                <span>Run Seating</span>
              </button>
              <button className="action-card" onClick={() => setCurrentView('reports')} id="btn-quick-reports">
                <FaSync />
                <span>View Reports</span>
              </button>
            </div>
          </div>

          {/* Active Seating Summary */}
          <div className="glass-card padding-md">
            <h3>Upcoming Exam Allocations</h3>
            <p className="card-subtitle margin-bottom-md">Current active seating parameters</p>
            {currentSeatingPlan ? (
              <div className="active-exam-widget">
                <div className="widget-row">
                  <span>Subject / Exam:</span>
                  <strong>{currentSeatingPlan.examType}</strong>
                </div>
                <div className="widget-row">
                  <span>Scheduled Date:</span>
                  <strong>{currentSeatingPlan.examDate}</strong>
                </div>
                <div className="widget-row">
                  <span>Session Time:</span>
                  <strong>{currentSeatingPlan.examTime}</strong>
                </div>
                <button 
                  className="btn btn-secondary btn-block margin-top-sm"
                  onClick={() => setCurrentView('reports')}
                >
                  View Seating Arrangement Grid
                </button>
              </div>
            ) : (
              <div className="empty-widget">
                <p>No active seating plan found.</p>
                <button 
                  className="btn btn-primary btn-block margin-top-sm"
                  onClick={() => setCurrentView('seating-setup')}
                >
                  Set Up Exam Layouts
                </button>
              </div>
            )}
          </div>

        </div>
      </div>

      {/* Recent Allocations list */}
      <div className="glass-card margin-top-lg padding-md">
        <div className="table-header-row">
          <div>
            <h3>Recent Student Allocations</h3>
            <p className="card-subtitle">First few seat registers in current exam setup</p>
          </div>
          <button className="text-btn" onClick={() => setCurrentView('reports')}>View All</button>
        </div>
        {recentAllocations.length > 0 ? (
          <div className="table-container">
            <table className="premium-table">
              <thead>
                <tr>
                  <th>Student Name</th>
                  <th>Reg No</th>
                  <th>Branch</th>
                  <th>Seat No</th>
                  <th>Room Number</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentAllocations.map((alloc, idx) => (
                  <tr key={idx}>
                    <td>{alloc.studentName}</td>
                    <td>{alloc.regNo}</td>
                    <td>{alloc.branch}</td>
                    <td>{alloc.seatNo}</td>
                    <td>Room {alloc.roomNumber}</td>
                    <td><span className="badge badge-success">Allocated</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <p>No recent allocations displayable. Create and save a Seating Plan to populate this list.</p>
          </div>
        )}
      </div>

      {/* Pulse rotating AI Optimization Floating Button */}
      <button 
        className="ai-optimization-fab"
        onClick={() => { resetMessages(); setIsAiModalOpen(true); }}
        title="AI Seating Optimizer"
        id="btn-ai-optimizer"
      >
        <FaRobot className="spin-slow" />
        <span className="fab-tooltip">AI Settings</span>
      </button>

      {/* AI Settings Modal */}
      <Modal 
        isOpen={isAiModalOpen} 
        onClose={() => setIsAiModalOpen(false)}
        title="AI Layout Seating Optimizer"
      >
        <div className="ai-optimizer-panel">
          <p className="description">
            Configure automated scheduling spacing models. PlanMySeat AI arranges student locations to guarantee university compliance.
          </p>

          <div className="form-group margin-top-md">
            <label className="form-label">Spacing Rules</label>
            <div className="spacing-select-grid">
              <button 
                type="button"
                className={`select-card ${spacingDensity === 'compact' ? 'active' : ''}`}
                onClick={() => setSpacingDensity('compact')}
              >
                <strong>Compact Packing</strong>
                <span>Fill rooms continuously to minimize invigilator ratios.</span>
              </button>
              <button 
                type="button"
                className={`select-card ${spacingDensity === 'alternate' ? 'active' : ''}`}
                onClick={() => setSpacingDensity('alternate')}
              >
                <strong>Alternate Seats</strong>
                <span>Leave an empty seat horizontally between students.</span>
              </button>
              <button 
                type="button"
                className={`select-card ${spacingDensity === 'double' ? 'active' : ''}`}
                onClick={() => setSpacingDensity('double')}
              >
                <strong>Double Offset</strong>
                <span>Leave empty space diagonally and horizontally.</span>
              </button>
            </div>
          </div>

          <div className="form-group">
            <div className="checkbox-row">
              <input 
                type="checkbox" 
                id="branchShuffleCheck" 
                checked={branchShuffle} 
                onChange={e => setBranchShuffle(e.target.checked)} 
              />
              <label htmlFor="branchShuffleCheck" className="form-label">
                <strong>Shuffle Branches Adjacent Seats</strong>
                <span className="sub-label">Ensure students sitting next to each other are from different departments.</span>
              </label>
            </div>
          </div>

          <div className="form-group">
            <div className="slider-header-row">
              <label className="form-label">Maximum Classroom Fill limit</label>
              <span className="slider-val">{fillRatio}%</span>
            </div>
            <input 
              type="range" 
              className="slider-range" 
              min="40" 
              max="100" 
              step="5" 
              value={fillRatio} 
              onChange={e => setFillRatio(Number(e.target.value))} 
            />
          </div>

          <div className="optimizer-actions">
            {isOptimizing ? (
              <div className="optimizing-loader">
                <FaSync className="spinner" />
                <span>Running AI Layout Solver...</span>
              </div>
            ) : optComplete ? (
              <div className="optimization-success">
                <FaCheckCircle className="success-icon" />
                <span>Packing algorithms completed successfully! Seating plan yields maximum density spacing scores.</span>
              </div>
            ) : null}

            <div className="modal-footer-btns">
              <button 
                className="btn btn-secondary" 
                onClick={() => setIsAiModalOpen(false)}
              >
                Close
              </button>
              <button 
                className="btn btn-primary" 
                onClick={handleRunAiOptimize}
                disabled={isOptimizing}
              >
                <FaSlidersH /> Apply Optimization
              </button>
            </div>
          </div>
        </div>
      </Modal>

      <style dangerouslySetInnerHTML={{__html: `
        .dashboard-split-layout {
          display: grid;
          grid-template-columns: 2fr 1fr;
          gap: 1.5rem;
          margin-top: 1.5rem;
        }

        .split-column {
          display: flex;
          flex-direction: column;
        }

        .actions-column {
          gap: 1.5rem;
        }

        .card-header {
          padding: 1.5rem;
          border-bottom: 1px solid var(--card-border);
        }

        .card-subtitle {
          font-size: 0.8rem;
          color: var(--text-muted);
          margin-top: 0.15rem;
        }

        .padding-md {
          padding: 1.5rem;
        }

        .margin-bottom-md {
          margin-bottom: 1rem;
        }

        .margin-top-lg {
          margin-top: 2rem;
        }

        .charts-flex {
          display: flex;
          flex-wrap: wrap;
          padding: 1.5rem;
          gap: 2rem;
          justify-content: space-around;
        }

        .chart-block {
          flex: 1;
          min-width: 250px;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 0.75rem;
        }

        .chart-block h4 {
          font-size: 0.85rem;
          color: var(--text-muted);
          text-transform: uppercase;
        }

        .quick-actions-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 0.75rem;
        }

        .action-card {
          background: var(--card-bg);
          border: 1.5px solid var(--card-border);
          border-radius: var(--radius-sm);
          padding: 1rem 0.5rem;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 0.5rem;
          cursor: pointer;
          transition: all var(--transition-fast);
        }

        .action-card svg {
          font-size: 1.25rem;
          color: var(--primary-color);
        }

        .action-card span {
          font-size: 0.8rem;
          font-weight: 600;
          color: var(--text-main);
        }

        .action-card:hover {
          border-color: var(--primary-color);
          transform: translateY(-2px);
          box-shadow: var(--shadow-sm);
        }

        .action-card.highlight {
          background: var(--primary-glow);
          border-color: rgba(99, 102, 241, 0.3);
        }

        .action-card.highlight svg {
          color: var(--primary-color);
        }

        .active-exam-widget {
          display: flex;
          flex-direction: column;
          gap: 0.65rem;
        }

        .widget-row {
          display: flex;
          justify-content: space-between;
          font-size: 0.85rem;
          padding: 0.25rem 0;
        }

        .widget-row span {
          color: var(--text-muted);
        }

        .widget-row strong {
          color: var(--text-main);
        }

        .empty-widget {
          text-align: center;
          padding: 1rem 0;
          color: var(--text-muted);
          font-size: 0.9rem;
        }

        .table-header-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1.5rem 1.5rem 0 1.5rem;
        }

        .empty-state {
          text-align: center;
          padding: 3rem 1.5rem;
          color: var(--text-muted);
          font-size: 0.9rem;
        }

        /* FAB Button AI styles */
        .ai-optimization-fab {
          position: fixed;
          bottom: 2rem;
          right: 2rem;
          width: 58px;
          height: 58px;
          border-radius: 50%;
          background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
          color: white;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 1.5rem;
          box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
          z-index: 99;
          transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }

        .ai-optimization-fab:hover {
          transform: scale(1.15);
          box-shadow: 0 6px 24px rgba(99, 102, 241, 0.5);
        }

        .fab-tooltip {
          position: absolute;
          right: 70px;
          background: var(--sidebar-bg);
          color: white;
          padding: 0.35rem 0.75rem;
          border-radius: 4px;
          font-size: 0.75rem;
          font-weight: 600;
          white-space: nowrap;
          opacity: 0;
          pointer-events: none;
          transition: opacity 0.2s;
          box-shadow: var(--shadow-md);
        }

        .ai-optimization-fab:hover .fab-tooltip {
          opacity: 1;
        }

        /* AI Optimizer dialog elements */
        .ai-optimizer-panel {
          display: flex;
          flex-direction: column;
          gap: 1.25rem;
        }

        .ai-optimizer-panel .description {
          font-size: 0.9rem;
          color: var(--text-muted);
          line-height: 1.5;
        }

        .spacing-select-grid {
          display: grid;
          grid-template-columns: 1fr;
          gap: 0.75rem;
          margin-top: 0.5rem;
        }

        .select-card {
          background: var(--card-bg);
          border: 1.5px solid var(--card-border);
          border-radius: var(--radius-sm);
          padding: 0.85rem 1.25rem;
          text-align: left;
          cursor: pointer;
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
          transition: all 0.2s;
        }

        .select-card strong {
          font-family: var(--font-display);
          font-size: 0.95rem;
          color: var(--text-main);
        }

        .select-card span {
          font-size: 0.75rem;
          color: var(--text-muted);
        }

        .select-card:hover {
          border-color: var(--primary-color);
        }

        .select-card.active {
          border-color: var(--primary-color);
          background: var(--primary-glow);
        }

        .checkbox-row {
          display: flex;
          align-items: flex-start;
          gap: 0.75rem;
          padding: 0.5rem 0;
        }

        .checkbox-row input {
          margin-top: 0.25rem;
          width: 16px;
          height: 16px;
          accent-color: var(--primary-color);
        }

        .sub-label {
          display: block;
          font-size: 0.75rem;
          color: var(--text-muted);
          font-weight: 400;
          margin-top: 0.15rem;
        }

        .slider-header-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .slider-val {
          font-family: var(--font-display);
          font-weight: 700;
          color: var(--primary-color);
        }

        .slider-range {
          width: 100%;
          accent-color: var(--primary-color);
          margin-top: 0.5rem;
        }

        .optimizer-actions {
          display: flex;
          flex-direction: column;
          gap: 1rem;
          border-top: 1px solid var(--card-border);
          padding-top: 1.25rem;
          margin-top: 0.5rem;
        }

        .optimizing-loader {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.75rem;
          font-size: 0.9rem;
          font-weight: 600;
          color: var(--primary-color);
        }

        .spinner {
          animation: spin 1s linear infinite;
        }

        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }

        .optimization-success {
          display: flex;
          align-items: flex-start;
          gap: 0.75rem;
          padding: 0.75rem;
          background: var(--success-bg);
          color: var(--success-color);
          border-radius: var(--radius-sm);
          font-size: 0.85rem;
          line-height: 1.4;
        }

        .success-icon {
          font-size: 1.1rem;
          flex-shrink: 0;
          margin-top: 0.1rem;
        }

        .modal-footer-btns {
          display: flex;
          justify-content: flex-end;
          gap: 0.75rem;
        }

        @media (max-width: 900px) {
          .dashboard-split-layout {
            grid-template-columns: 1fr;
          }
        }
      `}} />
    </div>
  );
}
