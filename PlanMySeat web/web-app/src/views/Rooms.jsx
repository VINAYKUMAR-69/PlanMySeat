import { useState } from 'react';
import { useApp } from '../context/AppContext';
import Modal from '../components/Modal';
import { FaPlus, FaTrash, FaEdit, FaSync, FaDoorClosed } from 'react-icons/fa';

export default function Rooms() {
  const { rooms, addRoom, updateRoom, deleteRoom, fetchRooms, loading } = useApp();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [targetId, setTargetId] = useState(null);

  const [roomNumber, setRoomNumber] = useState('');
  const [capacity, setCapacity] = useState('');
  const [building, setBuilding] = useState('');

  const handleOpenAdd = () => {
    setModalMode('add');
    setRoomNumber('');
    setCapacity('');
    setBuilding('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (room) => {
    setModalMode('edit');
    setTargetId(room.id);
    setRoomNumber(room.roomNumber);
    setCapacity(String(room.capacity));
    setBuilding(room.building);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!roomNumber || !capacity || !building) return;

    try {
      if (modalMode === 'add') {
        await addRoom(roomNumber, Number(capacity), building);
      } else {
        await updateRoom(targetId, { roomNumber, capacity: Number(capacity), building });
      }
      setIsModalOpen(false);
    } catch (e) {
      alert(e.message || 'Operation failed');
    }
  };

  const handleDelete = async (roomId, roomNo) => {
    if (window.confirm(`Are you sure you want to delete classroom ${roomNo}?`)) {
      try {
        await deleteRoom(roomId, roomNo);
      } catch (e) {
        alert(e.message || 'Delete failed');
      }
    }
  };

  return (
    <div className="rooms-view animate-fade-up">
      {/* Top Header metrics */}
      <div className="action-bar glass-card padding-md">
        <div className="rooms-summary">
          <h3>Classroom Layouts Roster</h3>
          <p className="card-subtitle">Registered centers: {rooms.length} venues</p>
        </div>
        <div className="buttons-group">
          <button className="btn btn-primary btn-sm" onClick={handleOpenAdd} id="btn-add-room">
            <FaPlus /> Add Room
          </button>
          <button className="btn btn-secondary btn-sm refresh-btn" onClick={fetchRooms} disabled={loading.rooms}>
            <FaSync className={loading.rooms ? 'spinner' : ''} />
          </button>
        </div>
      </div>

      {/* Grid listing */}
      <div className="rooms-grid margin-top-lg">
        {rooms.map((room) => (
          <div className="room-card glass-card" key={room.id}>
            <div className="room-card-header">
              <FaDoorClosed className="room-icon" />
              <div className="room-title">
                <h4>Room {room.roomNumber}</h4>
                <span>{room.building} Block</span>
              </div>
            </div>
            <div className="room-card-body">
              <div className="metric">
                <span className="label">Total Seat capacity</span>
                <span className="value">{room.capacity} seats</span>
              </div>
            </div>
            <div className="room-card-actions">
              <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEdit(room)}>
                <FaEdit /> Edit
              </button>
              <button className="btn btn-danger btn-sm" onClick={() => handleDelete(room.id, room.roomNumber)}>
                <FaTrash /> Delete
              </button>
            </div>
          </div>
        ))}

        {rooms.length === 0 && (
          <div className="empty-state w-100">
            <p>No classrooms registered in the system. Add classrooms to generate seating plans.</p>
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={modalMode === 'add' ? 'Register New Classroom' : 'Edit Classroom Parameters'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Room Number</label>
            <input 
              type="text" 
              className="form-input" 
              value={roomNumber} 
              onChange={e => setRoomNumber(e.target.value)} 
              placeholder="LH 301"
              required 
              id="room-number"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Total Seating Capacity</label>
            <input 
              type="number" 
              className="form-input" 
              value={capacity} 
              onChange={e => setCapacity(e.target.value)} 
              placeholder="40"
              required 
              min="5"
              id="room-capacity"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Building / Block</label>
            <input 
              type="text" 
              className="form-input" 
              value={building} 
              onChange={e => setBuilding(e.target.value)} 
              placeholder="Main Science Block"
              required 
              id="room-building"
            />
          </div>

          <div className="modal-footer-btns">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" id="btn-submit-room">
              {modalMode === 'add' ? 'Register Room' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      <style dangerouslySetInnerHTML={{__html: `
        .rooms-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 1.5rem;
        }

        .room-card {
          padding: 1.5rem;
          display: flex;
          flex-direction: column;
          gap: 1.25rem;
        }

        .room-card-header {
          display: flex;
          align-items: center;
          gap: 1rem;
        }

        .room-icon {
          font-size: 1.75rem;
          color: var(--primary-color);
          background: var(--primary-glow);
          width: 48px;
          height: 48px;
          border-radius: var(--radius-sm);
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 0.75rem;
        }

        .room-title h4 {
          font-size: 1.1rem;
          font-weight: 700;
        }

        .room-title span {
          font-size: 0.8rem;
          color: var(--text-muted);
        }

        .room-card-body {
          border-top: 1px solid var(--card-border);
          border-bottom: 1px solid var(--card-border);
          padding: 0.75rem 0;
        }

        .metric {
          display: flex;
          justify-content: space-between;
          font-size: 0.9rem;
        }

        .metric .label {
          color: var(--text-muted);
        }

        .metric .value {
          font-weight: 700;
          color: var(--text-main);
        }

        .room-card-actions {
          display: flex;
          justify-content: flex-end;
          gap: 0.5rem;
        }

        .w-100 {
          grid-column: 1 / -1;
        }
      `}} />
    </div>
  );
}
