import { useApp } from '../context/AppContext';
import StatsCard from '../components/StatsCard';
import { DonutChart, BarChart, LineChart } from '../components/Charts';
import { FaUsers, FaCalendarCheck, FaClock, FaStar } from 'react-icons/fa';

export default function DashboardFaculty({ setCurrentView }) {
  const { faculties, seatingPlans, user } = useApp();

  const totalFaculty = faculties.length;
  const activeFaculty = faculties.filter(f => f.status === 'Active' || f.status === 'Confirmed').length;
  
  // Calculate specific duties from all seating plans matching this user name
  const facultyDuties = seatingPlans.filter(p => {
    // If the plan has allocations matching this faculty name
    if (user && p.invigilator) {
      return p.invigilator.toLowerCase().trim() === user.name.toLowerCase().trim();
    }
    return false;
  });

  const assignedDutiesCount = facultyDuties.length;

  // Chart data calculations
  const deptCounts = faculties.reduce((acc, f) => {
    if (f.department) acc[f.department] = (acc[f.department] || 0) + 1;
    return acc;
  }, {});

  const statusCounts = faculties.reduce((acc, f) => {
    if (f.status) acc[f.status] = (acc[f.status] || 0) + 1;
    return acc;
  }, {});

  // Top Performers based on rating
  const topPerformers = [...faculties]
    .filter(f => f.rating && Number(f.rating) > 0)
    .sort((a, b) => Number(b.rating) - Number(a.rating))
    .slice(0, 5);

  return (
    <div className="faculty-dashboardanimate animate-fade-up">
      {/* Overview Cards */}
      <div className="dashboard-grid">
        <StatsCard 
          title="Total Faculty" 
          value={totalFaculty} 
          icon={<FaUsers />} 
          description="Registered invigilators"
          onClick={() => setCurrentView('faculties')}
        />
        <StatsCard 
          title="Available Now" 
          value={activeFaculty} 
          icon={<FaCalendarCheck />} 
          description={`${Math.round(totalFaculty ? (activeFaculty / totalFaculty) * 100 : 0)}% of roster available`}
          onClick={() => setCurrentView('faculties')}
        />
        <StatsCard 
          title="My Assigned Duties" 
          value={assignedDutiesCount} 
          icon={<FaClock />} 
          description="Assigned classroom duties this term"
          onClick={() => setCurrentView('reports')}
        />
        <StatsCard 
          title="Avg Rating Score" 
          value="4.8" 
          icon={<FaStar />} 
          description="Duties feedback rating"
        />
      </div>

      {/* Charts Panel */}
      <div className="dashboard-split-layout">
        
        {/* Left Side: Department Distribution and Stats */}
        <div className="split-column glass-card">
          <div className="card-header">
            <h3>Faculty Allocation Analytics</h3>
            <p className="card-subtitle">Breakdown of departments and availability</p>
          </div>
          <div className="charts-flex">
            <div className="chart-block">
              <h4>Department Roster</h4>
              <DonutChart data={deptCounts} />
            </div>
            <div className="chart-block">
              <h4>Status Availability</h4>
              <BarChart data={statusCounts} />
            </div>
          </div>
        </div>

        {/* Right Side: Duties Timeline Activity */}
        <div className="split-column glass-card padding-md">
          <h3>Roster Activity Timeline</h3>
          <p className="card-subtitle margin-bottom-md">Number of assigned invigilations over time</p>
          <LineChart data={[10, 30, 25, 45, 60, 50, 75]} />
        </div>

      </div>

      {/* Two lists: Duties roster list & Top Performers rating list */}
      <div className="lists-grid margin-top-lg">
        
        {/* Duties lists */}
        <div className="glass-card padding-md">
          <h3>My Duties Roster</h3>
          <p className="card-subtitle margin-bottom-md">Schedules of upcoming invigilator shifts</p>
          {facultyDuties.length > 0 ? (
            <div className="table-container">
              <table className="premium-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Classroom</th>
                    <th>Subject</th>
                  </tr>
                </thead>
                <tbody>
                  {facultyDuties.map((duty, idx) => (
                    <tr key={idx}>
                      <td>{duty.date}</td>
                      <td>{duty.time}</td>
                      <td>Room {duty.roomNumber}</td>
                      <td>{duty.subject}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <p>You have no pending invigilator duties assigned under your user name.</p>
            </div>
          )}
        </div>

        {/* Top Performers */}
        <div className="glass-card padding-md">
          <h3>Top Roster Performers</h3>
          <p className="card-subtitle margin-bottom-md">Invigilators with highest ratings</p>
          {topPerformers.length > 0 ? (
            <div className="performers-list">
              {topPerformers.map((perf) => (
                <div className="performer-row" key={perf.id}>
                  <div className="perf-avatar">
                    {perf.name.slice(0, 2).toUpperCase()}
                  </div>
                  <div className="perf-info">
                    <h4>{perf.name}</h4>
                    <span>{perf.designation} | {perf.department}</span>
                  </div>
                  <div className="perf-rating">
                    <FaStar className="star-icon" />
                    <strong>{perf.rating}</strong>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>No faculty performance evaluations available.</p>
            </div>
          )}
        </div>

      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .lists-grid {
          display: grid;
          grid-template-columns: 1.5fr 1fr;
          gap: 1.5rem;
        }

        .performers-list {
          display: flex;
          flex-direction: column;
          gap: 1rem;
          margin-top: 1rem;
        }

        .performer-row {
          display: flex;
          align-items: center;
          gap: 1rem;
          padding: 0.75rem;
          background: rgba(255, 255, 255, 0.02);
          border: 1px solid var(--card-border);
          border-radius: var(--radius-sm);
        }

        .perf-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: var(--primary-glow);
          color: var(--primary-color);
          font-weight: 700;
          font-size: 0.8rem;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .perf-info {
          display: flex;
          flex-direction: column;
          gap: 0.15rem;
          flex: 1;
        }

        .perf-info h4 {
          font-size: 0.9rem;
          font-weight: 600;
        }

        .perf-info span {
          font-size: 0.75rem;
          color: var(--text-muted);
        }

        .perf-rating {
          display: flex;
          align-items: center;
          gap: 0.25rem;
          color: var(--warning-color);
          font-size: 0.9rem;
        }

        .star-icon {
          font-size: 0.95rem;
        }

        @media (max-width: 900px) {
          .lists-grid {
            grid-template-columns: 1fr;
          }
        }
      `}} />
    </div>
  );
}
