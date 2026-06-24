/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { FaMinus, FaPlus, FaCalendarAlt, FaClock, FaExclamationTriangle, FaCheckCircle, FaChevronRight } from 'react-icons/fa';

export default function SeatingAllocationSetup({ setCurrentView }) {
  const { students, rooms, generateSeatingPlan, saveSeatingPlanToBackend, triggerSystemNotification } = useApp();

  // Form Fields
  const [examType, setExamType] = useState('');
  const [examDate, setExamDate] = useState('');
  const [examTime, setExamTime] = useState('');
  
  // Selection
  const [roomSelectionCount, setRoomSelectionCount] = useState(1);
  const [useAllOverride, setUseAllOverride] = useState(false);

  // Maximum constraints
  const maxRooms = rooms.length;
  const totalOverallStudents = students.length;

  // Auto calculations
  const filteredStudentsCount = students.filter(
    s => s.examType && s.examType.toLowerCase() === examType.toLowerCase()
  ).length;

  const targetStudentsCount = useAllOverride ? totalOverallStudents : (filteredStudentsCount || totalOverallStudents);
  const selectedRooms = rooms.slice(0, roomSelectionCount);
  const totalCapacity = selectedRooms.reduce((acc, r) => acc + (r.capacity || 0), 0);

  // Auto adjust room selection based on target student count
  useEffect(() => {
    if (targetStudentsCount > 0 && maxRooms > 0) {
      let needed = 0;
      let capacityAccum = 0;
      for (let i = 0; i < maxRooms; i++) {
        if (capacityAccum >= targetStudentsCount) break;
        capacityAccum += rooms[i].capacity;
        needed++;
      }
      setRoomSelectionCount(Math.max(1, Math.min(needed, maxRooms)));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [examType, useAllOverride, students, rooms]);

  const handleIncrement = () => {
    if (roomSelectionCount < maxRooms) {
      setRoomSelectionCount(prev => prev + 1);
    }
  };

  const handleDecrement = () => {
    if (roomSelectionCount > 1) {
      setRoomSelectionCount(prev => prev - 1);
    }
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!examType || !examDate || !examTime) {
      alert("Please fill in all exam parameters.");
      return;
    }

    // Check if there are no students assigned to this exam type
    if (filteredStudentsCount === 0 && !useAllOverride) {
      const confirmAll = window.confirm(
        `There are no students assigned to exam type '${examType}'. Would you like to use all ${totalOverallStudents} available students for this plan?`
      );
      if (confirmAll) {
        setUseAllOverride(true);
        // Continue generation in next render cycle or immediately using override
        triggerGeneration(true);
      }
      return;
    }

    triggerGeneration(useAllOverride);
  };

  const triggerGeneration = async (useAll) => {
    if (totalCapacity < targetStudentsCount) {
      alert(
        `Insufficient Seating Capacity!\nSelected rooms capacity: ${totalCapacity}\nStudents to allocate: ${targetStudentsCount}\n\nPlease select more rooms.`
      );
      return;
    }

    try {
      const plan = await generateSeatingPlan(examType, examDate, examTime, roomSelectionCount, useAll);
      await saveSeatingPlanToBackend(plan);
      await triggerSystemNotification("Plan Generated", `Seating allocations generated for ${examType}.`);
      setCurrentView('reports');
    } catch (e) {
      alert(e.message || 'Seating Plan Generation Failed');
    }
  };

  return (
    <div className="seating-setup animate-fade-up">
      <div className="setup-grid">
        
        {/* Left column: Parameters Setup */}
        <form onSubmit={handleGenerate} className="glass-card padding-md">
          <h3>Exam Seating Setup</h3>
          <p className="card-subtitle margin-bottom-md">Configure schedule coordinates and spaces</p>

          <div className="form-group">
            <label className="form-label">Exam Title / Subject Code</label>
            <input 
              type="text" 
              className="form-input" 
              placeholder="e.g. Model, Semester, CS101"
              value={examType}
              onChange={e => { setExamType(e.target.value); setUseAllOverride(false); }}
              required 
              id="setup-exam-type"
            />
            {examType && (
              <span className="input-info-hint">
                {filteredStudentsCount} students currently assigned to this exam type.
              </span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Exam Date</label>
            <div className="input-with-icon">
              <FaCalendarAlt className="input-icon" />
              <input 
                type="date" 
                className="form-input" 
                value={examDate}
                onChange={e => setExamDate(e.target.value)}
                required 
                id="setup-exam-date"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Start Time Session</label>
            <div className="input-with-icon">
              <FaClock className="input-icon" />
              <input 
                type="time" 
                className="form-input" 
                value={examTime}
                onChange={e => setExamTime(e.target.value)}
                required 
                id="setup-exam-time"
              />
            </div>
          </div>

          {/* Room Selection Counter */}
          <div className="form-group margin-top-md">
            <label className="form-label">Rooms Selected Count</label>
            <div className="counter-row">
              <button 
                type="button" 
                className="btn btn-secondary btn-counter"
                onClick={handleDecrement}
                disabled={roomSelectionCount <= 1}
              >
                <FaMinus />
              </button>
              <span className="counter-val">{roomSelectionCount}</span>
              <button 
                type="button" 
                className="btn btn-secondary btn-counter"
                onClick={handleIncrement}
                disabled={roomSelectionCount >= maxRooms}
              >
                <FaPlus />
              </button>
              <span className="counter-limit-text">of {maxRooms} available</span>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block margin-top-md" id="btn-submit-allocation">
            Generate Seating Plan <FaChevronRight />
          </button>
        </form>

        {/* Right column: Space Analytics */}
        <div className="glass-card padding-md flex-column justify-between">
          <div>
            <h3>Allocation Solver Analytics</h3>
            <p className="card-subtitle margin-bottom-md">Real-time packing layout checks</p>

            <div className="analytic-row">
              <div className="analytic-item">
                <span>Target Student Count</span>
                <strong>{targetStudentsCount}</strong>
              </div>
              <div className="analytic-item">
                <span>Capacity Provided</span>
                <strong className={totalCapacity >= targetStudentsCount ? 'text-success' : 'text-danger'}>
                  {totalCapacity}
                </strong>
              </div>
            </div>

            {totalCapacity < targetStudentsCount ? (
              <div className="alert-box alert-danger flex items-center gap-sm">
                <FaExclamationTriangle className="alert-icon" />
                <div>
                  <h4>Capacity Deficit</h4>
                  <p>Selected rooms hold {totalCapacity} seats, but {targetStudentsCount} students require spacing. Increment room selections.</p>
                </div>
              </div>
            ) : targetStudentsCount > 0 ? (
              <div className="alert-box alert-success flex items-center gap-sm">
                <FaCheckCircle className="alert-icon" />
                <div>
                  <h4>Valid Configuration</h4>
                  <p>All students fit comfortably inside the designated classrooms.</p>
                </div>
              </div>
            ) : null}

            {/* Selected Rooms List */}
            <div className="selected-rooms-scroll-list margin-top-md">
              <h4>Classrooms in active plan:</h4>
              <ul>
                {selectedRooms.map((room) => (
                  <li key={room.id}>
                    <span>• Room {room.roomNumber} ({room.building})</span>
                    <strong>{room.capacity} seats</strong>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .setup-grid {
          display: grid;
          grid-template-columns: 1.2fr 1fr;
          gap: 2rem;
        }

        .input-info-hint {
          font-size: 0.75rem;
          color: var(--primary-color);
          margin-top: 0.15rem;
        }

        .counter-row {
          display: flex;
          align-items: center;
          gap: 1rem;
        }

        .btn-counter {
          width: 40px;
          height: 40px;
          padding: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
        }

        .counter-val {
          font-family: var(--font-display);
          font-size: 1.5rem;
          font-weight: 800;
          min-width: 30px;
          text-align: center;
        }

        .counter-limit-text {
          font-size: 0.85rem;
          color: var(--text-muted);
        }

        .analytic-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 1rem;
          margin-bottom: 1.5rem;
        }

        .analytic-item {
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
          padding: 1rem;
          background: var(--bg-color);
          border-radius: var(--radius-sm);
          border: 1px solid var(--card-border);
        }

        .analytic-item span {
          font-size: 0.8rem;
          color: var(--text-muted);
        }

        .analytic-item strong {
          font-size: 1.5rem;
          font-weight: 800;
        }

        .text-success {
          color: var(--success-color);
        }

        .text-danger {
          color: var(--error-color);
        }

        .alert-box {
          display: flex;
          gap: 1rem;
          padding: 1rem;
          border-radius: var(--radius-sm);
          margin-bottom: 1.5rem;
        }

        .alert-icon {
          font-size: 1.5rem;
          flex-shrink: 0;
          margin-top: 0.15rem;
        }

        .alert-box h4 {
          font-size: 0.95rem;
          font-weight: 700;
          margin-bottom: 0.15rem;
        }

        .alert-box p {
          font-size: 0.8rem;
          line-height: 1.4;
        }

        .selected-rooms-scroll-list h4 {
          font-size: 0.85rem;
          color: var(--text-muted);
          text-transform: uppercase;
          margin-bottom: 0.5rem;
        }

        .selected-rooms-scroll-list ul {
          list-style: none;
          max-height: 160px;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }

        .selected-rooms-scroll-list li {
          display: flex;
          justify-content: space-between;
          font-size: 0.85rem;
          color: var(--text-main);
        }

        .selected-rooms-scroll-list strong {
          color: var(--text-muted);
        }

        .flex-column {
          display: flex;
          flex-direction: column;
        }

        .justify-between {
          justify-content: space-between;
        }

        @media (max-width: 900px) {
          .setup-grid {
            grid-template-columns: 1fr;
          }
        }
      `}} />
    </div>
  );
}
