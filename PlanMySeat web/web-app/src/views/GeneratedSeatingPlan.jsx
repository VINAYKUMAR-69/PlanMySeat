import { useState } from 'react';
import { useApp } from '../context/AppContext';
import { FaPrint, FaDownload, FaUsers, FaArrowLeft, FaSearch } from 'react-icons/fa';

export default function GeneratedSeatingPlan({ setCurrentView }) {
  const { currentSeatingPlan, seatingPlans } = useApp();
  
  // Search states inside seating sheets
  const [search, setSearch] = useState('');
  const [selectedExamTab, setSelectedExamTab] = useState('');

  // Collect saved exams from final-reports if current plan is null
  const savedExams = [...new Set(seatingPlans.map(p => p.subject).filter(Boolean))];

  // Pick active plan
  let activePlan = null;

  if (currentSeatingPlan && currentSeatingPlan.allocations.length > 0) {
    activePlan = currentSeatingPlan;
  } else if (seatingPlans.length > 0) {
    // If user has saved plans but didn't just generate one, select the first or matching exam
    const targetExam = selectedExamTab || savedExams[0];
    const filteredAllocations = seatingPlans.filter(p => p.subject === targetExam);
    
    if (filteredAllocations.length > 0) {
      activePlan = {
        examType: targetExam,
        examDate: filteredAllocations[0].date || 'Not Set',
        examTime: filteredAllocations[0].time || 'Not Set',
        roomsUsed: [...new Set(filteredAllocations.map(a => a.roomNumber))].length,
        totalStudents: filteredAllocations.length,
        allocations: filteredAllocations.map(a => ({
          studentName: a.studentName,
          regNo: a.regNo,
          branch: a.branch,
          seatNo: a.seatNo,
          roomNumber: a.roomNumber,
          building: a.building || 'Unknown',
          invigilator: a.invigilator || 'Not Assigned',
          date: a.date,
          time: a.time
        }))
      };
    }
  }

  const handlePrint = () => {
    window.print();
  };

  const handleExportCsv = () => {
    if (!activePlan) return;
    const headers = 'Room Number,Seat Number,Student Name,Registration Number,Branch,Invigilator,Exam Date,Exam Time\n';
    const rows = activePlan.allocations.map(a => 
      `"Room ${a.roomNumber}",${a.seatNo},"${a.studentName}","${a.regNo}","${a.branch}","${a.invigilator}","${activePlan.examDate}","${activePlan.examTime}"`
    ).join('\n');

    const blob = new Blob([headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `SeatingPlan_${activePlan.examType.replace(/\s+/g, '_')}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (!activePlan) {
    return (
      <div className="seating-plan-empty animate-fade-up glass-card padding-md text-center">
        <FaUsers className="empty-icon" />
        <h3>No Seating Arrangements Generated</h3>
        <p className="card-subtitle margin-bottom-md">
          Please add students and classrooms to configure dynamic room seating layout plans.
        </p>
        <button 
          className="btn btn-primary"
          onClick={() => setCurrentView('seating-setup')}
        >
          Create Allocation Setup
        </button>
      </div>
    );
  }

  // Filter allocations based on inline search
  const displayedAllocations = activePlan.allocations.filter(a => {
    return (
      a.studentName.toLowerCase().includes(search.toLowerCase()) ||
      a.regNo.toLowerCase().includes(search.toLowerCase()) ||
      a.roomNumber.toLowerCase().includes(search.toLowerCase())
    );
  });

  // Group allocations by Room
  const allocationsByRoom = displayedAllocations.reduce((acc, alloc) => {
    acc[alloc.roomNumber] = acc[alloc.roomNumber] || [];
    acc[alloc.roomNumber].push(alloc);
    return acc;
  }, {});

  return (
    <div className="seating-plan-view animate-fade-up">
      {/* Print stylesheet override */}
      <style dangerouslySetInnerHTML={{__html: `
        @media print {
          body * {
            visibility: hidden;
            background: white !important;
            color: black !important;
          }
          .seating-plan-print-area, .seating-plan-print-area * {
            visibility: visible;
          }
          .seating-plan-print-area {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
          }
          .print-room-page {
            page-break-after: always;
            border: none !important;
            box-shadow: none !important;
            padding: 2rem !important;
          }
          .no-print {
            display: none !important;
          }
        }
      `}} />

      {/* Control bar */}
      <div className="action-bar glass-card padding-md no-print">
        <div className="search-group">
          <div className="input-with-icon">
            <FaSearch className="input-icon" />
            <input 
              type="text" 
              className="form-input" 
              placeholder="Search students, reg number, or room in active plan..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              id="plan-search-input"
            />
          </div>
        </div>

        {savedExams.length > 0 && !currentSeatingPlan && (
          <div className="filter-item">
            <select 
              className="form-input select-sm" 
              value={selectedExamTab} 
              onChange={e => setSelectedExamTab(e.target.value)}
              id="plan-saved-exams"
            >
              {savedExams.map(e => <option key={e} value={e}>{e}</option>)}
            </select>
          </div>
        )}

        <div className="buttons-group">
          <button className="btn btn-secondary btn-sm" onClick={handlePrint} id="btn-print-plan">
            <FaPrint /> Print Plan
          </button>
          <button className="btn btn-secondary btn-sm" onClick={handleExportCsv} id="btn-export-plan-csv">
            <FaDownload /> Export CSV
          </button>
          <button className="btn btn-primary btn-sm" onClick={() => setCurrentView('seating-setup')} id="btn-back-setup">
            <FaArrowLeft /> New Plan
          </button>
        </div>
      </div>

      {/* Seating Plan metadata */}
      <div className="plan-summary-banner glass-card margin-top-lg padding-md no-print">
        <div className="summary-left">
          <h2>Seating Plan: {activePlan.examType}</h2>
          <span className="exam-time-coordinates">{activePlan.examDate} | {activePlan.examTime}</span>
        </div>
        <div className="summary-right">
          <div className="badge badge-success">
            {activePlan.totalStudents} allocated students
          </div>
          <div className="badge badge-warning">
            {activePlan.roomsUsed} classrooms
          </div>
        </div>
      </div>

      {/* Grouped rooms grid */}
      <div className="seating-plan-print-area margin-top-lg">
        {Object.entries(allocationsByRoom).map(([roomNumber, roomAllocations]) => {
          const invigilatorName = roomAllocations[0]?.invigilator || 'Not Assigned';
          const buildingName = roomAllocations[0]?.building || 'Unknown';
          
          return (
            <div className="print-room-page room-plan-sheet glass-card margin-bottom-lg" key={roomNumber}>
              <div className="room-sheet-header">
                <div>
                  <h3>Room {roomNumber}</h3>
                  <p className="building-label">{buildingName} block</p>
                </div>
                <div className="invigilator-badge">
                  <span>Invigilator:</span>
                  <strong>{invigilatorName}</strong>
                </div>
              </div>
              
              <div className="table-container">
                <table className="premium-table">
                  <thead>
                    <tr>
                      <th width="80">Seat</th>
                      <th>Student Name</th>
                      <th>Reg Number</th>
                      <th>Branch</th>
                      <th>Exam Session</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roomAllocations.map((alloc) => (
                      <tr key={alloc.regNo}>
                        <td className="seat-cell">{alloc.seatNo}</td>
                        <td className="bold-name">{alloc.studentName}</td>
                        <td>{alloc.regNo}</td>
                        <td>{alloc.branch}</td>
                        <td>{activePlan.examDate} - {activePlan.examTime}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          );
        })}
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .seating-plan-empty {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 1rem;
          padding: 4rem 2rem;
        }

        .empty-icon {
          font-size: 3rem;
          color: var(--text-muted);
        }

        .plan-summary-banner {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .summary-left h2 {
          font-size: 1.5rem;
          font-weight: 800;
        }

        .exam-time-coordinates {
          font-size: 0.85rem;
          color: var(--text-muted);
          font-weight: 500;
        }

        .summary-right {
          display: flex;
          gap: 0.5rem;
        }

        .room-plan-sheet {
          padding: 1.5rem;
        }

        .room-sheet-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 1.25rem;
          padding-bottom: 0.75rem;
          border-bottom: 1.5px dashed var(--card-border);
        }

        .room-sheet-header h3 {
          font-size: 1.25rem;
          font-weight: 800;
        }

        .building-label {
          font-size: 0.8rem;
          color: var(--text-muted);
        }

        .invigilator-badge {
          text-align: right;
          font-size: 0.85rem;
          display: flex;
          flex-direction: column;
        }

        .invigilator-badge span {
          color: var(--text-muted);
          font-size: 0.75rem;
        }

        .invigilator-badge strong {
          color: var(--primary-color);
          font-weight: 600;
        }

        .seat-cell {
          font-family: var(--font-display);
          font-weight: 700;
          color: var(--primary-color);
          text-align: center;
        }
      `}} />
    </div>
  );
}
