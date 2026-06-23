import React, { useState, useRef } from 'react';
import { useApp } from '../context/AppContext';
import Modal from '../components/Modal';
import { FaPlus, FaTrash, FaStar, FaEnvelope, FaPhone, FaBuilding, FaBriefcase, FaSync, FaUpload, FaDownload } from 'react-icons/fa';

export default function Faculties() {
  const { faculties, addFaculty, deleteFaculty, fetchFaculties, loading } = useApp();

  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Form fields
  const [facultyId, setFacultyId] = useState('');
  const [name, setName] = useState('');
  const [designation, setDesignation] = useState('Assistant Professor');
  const [department, setDepartment] = useState('');
  const [phone, setPhone] = useState('');
  const [experience, setExperience] = useState('');
  const [rating, setRating] = useState('4.5');
  const [status, setStatus] = useState('Active');

  const fileInputRef = useRef(null);

  const handleDownloadTemplate = () => {
    const csvContent = "Name,Designation,Department,Experience,Phone\nJohn Doe,Professor,Computer Science,10,+1234567890";
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", "faculty_template.csv");
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (evt) => {
      const text = evt.target.result;
      const lines = text.split('\n').filter(l => l.trim() !== '');
      if (lines.length <= 1) {
        alert("The CSV file is empty or has only a header.");
        return;
      }
      
      let addedCount = 0;
      for (let i = 1; i < lines.length; i++) {
        // Simple CSV parse by comma
        const cols = lines[i].split(',').map(c => c.trim());
        if (cols.length >= 4) {
          const [csvName, csvDesignation, csvDept, csvExp, csvPhone] = cols;
          const newId = `FAC${Math.floor(1000 + Math.random() * 9000)}`;
          try {
            await addFaculty({
              facultyId: newId,
              name: csvName,
              designation: csvDesignation || 'Assistant Professor',
              department: csvDept,
              experience: Number(csvExp) || 0,
              phone: csvPhone || '',
              rating: '4.5',
              status: 'Active'
            });
            addedCount++;
          } catch (err) {
            console.error('Failed to add row:', lines[i], err);
          }
        }
      }
      alert(`Successfully uploaded ${addedCount} faculty members.`);
      e.target.value = ''; // Reset file input
    };
    reader.readAsText(file);
  };

  const handleOpenAdd = () => {
    setFacultyId(`FAC${Math.floor(1000 + Math.random() * 9000)}`);
    setName('');
    setDepartment('');
    setPhone('');
    setExperience('');
    setRating('4.5');
    setStatus('Active');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name || !department || !experience) return;

    try {
      await addFaculty({
        facultyId,
        name,
        designation,
        department,
        phone,
        experience: Number(experience),
        rating,
        status
      });
      setIsModalOpen(false);
    } catch (e) {
      alert(e.message || 'Failed to add faculty');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Are you sure you want to remove invigilator ${name}?`)) {
      try {
        await deleteFaculty(id, name);
      } catch (e) {
        alert(e.message || 'Operation failed');
      }
    }
  };

  return (
    <div className="faculties-view animate-fade-up">
      {/* Search and Action Bar */}
      <div className="action-bar glass-card padding-md">
        <div className="rooms-summary">
          <h3>Faculty Invigilator Roster</h3>
          <p className="card-subtitle">Registered teachers: {faculties.length} entries</p>
        </div>
        <div className="buttons-group">
          <input 
            type="file" 
            accept=".csv" 
            style={{ display: 'none' }} 
            ref={fileInputRef} 
            onChange={handleFileUpload} 
          />
          <button className="btn btn-secondary btn-sm" onClick={handleDownloadTemplate} title="Download CSV Template">
            <FaDownload /> Template
          </button>
          <button className="btn btn-secondary btn-sm" onClick={() => fileInputRef.current?.click()} title="Upload CSV">
            <FaUpload /> Upload CSV
          </button>
          <button className="btn btn-primary btn-sm" onClick={handleOpenAdd} id="btn-add-faculty">
            <FaPlus /> Add Invigilator
          </button>
          <button className="btn btn-secondary btn-sm refresh-btn" onClick={fetchFaculties} disabled={loading.faculties}>
            <FaSync className={loading.faculties ? 'spinner' : ''} />
          </button>
        </div>
      </div>

      {/* Grid listing */}
      <div className="faculties-grid margin-top-lg">
        {faculties.map((fac) => (
          <div className="faculty-card glass-card" key={fac.id}>
            <div className="faculty-card-header">
              <div className="avatar-circle">
                {fac.name.slice(0, 2).toUpperCase()}
              </div>
              <div className="faculty-meta">
                <h4>{fac.name}</h4>
                <span>{fac.designation}</span>
              </div>
              <span className={`status-badge-inline ${fac.status.toLowerCase() === 'active' || fac.status.toLowerCase() === 'confirmed' ? 'active' : ''}`}>
                {fac.status}
              </span>
            </div>

            <div className="faculty-card-body">
              <div className="info-row">
                <FaBuilding className="row-icon" />
                <span>Dept: {fac.department}</span>
              </div>
              <div className="info-row">
                <FaBriefcase className="row-icon" />
                <span>Exp: {fac.experience} years</span>
              </div>
              {fac.phone && (
                <div className="info-row">
                  <FaPhone className="row-icon" />
                  <span>{fac.phone}</span>
                </div>
              )}
              <div className="info-row rating-row">
                <FaStar className="row-icon star" />
                <span>Rating score: <strong>{fac.rating || 'N/A'}</strong></span>
              </div>
            </div>

            <div className="faculty-card-actions">
              <button 
                className="btn btn-danger btn-sm w-100" 
                onClick={() => handleDelete(fac.id, fac.name)}
              >
                <FaTrash /> Remove Faculty
              </button>
            </div>
          </div>
        ))}

        {faculties.length === 0 && (
          <div className="empty-state w-100">
            <p>No faculty members found. Add invigilators to organize schedules.</p>
          </div>
        )}
      </div>

      {/* Add Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Add New Faculty Member"
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Faculty ID</label>
            <input 
              type="text" 
              className="form-input" 
              value={facultyId} 
              disabled 
            />
          </div>

          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input 
              type="text" 
              className="form-input" 
              value={name} 
              onChange={e => setName(e.target.value)} 
              placeholder="Dr. Sarah Connor"
              required 
              id="faculty-name"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Designation Role</label>
            <select 
              className="form-input" 
              value={designation} 
              onChange={e => setDesignation(e.target.value)}
              id="faculty-designation"
            >
              <option value="Assistant Professor">Assistant Professor</option>
              <option value="Associate Professor">Associate Professor</option>
              <option value="Professor">Professor</option>
              <option value="Lecturer">Lecturer</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Department</label>
            <input 
              type="text" 
              className="form-input" 
              value={department} 
              onChange={e => setDepartment(e.target.value)} 
              placeholder="Physics / ECE"
              required 
              id="faculty-dept"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Mobile Number</label>
            <input 
              type="tel" 
              className="form-input" 
              value={phone} 
              onChange={e => setPhone(e.target.value)} 
              placeholder="+91 9876543210"
              id="faculty-phone"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Experience (Years)</label>
            <input 
              type="number" 
              className="form-input" 
              value={experience} 
              onChange={e => setExperience(e.target.value)} 
              placeholder="5"
              required 
              id="faculty-experience"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Status</label>
            <select 
              className="form-input" 
              value={status} 
              onChange={e => setStatus(e.target.value)}
              id="faculty-status"
            >
              <option value="Active">Active</option>
              <option value="Confirmed">Confirmed</option>
              <option value="Inactive">Inactive</option>
            </select>
          </div>

          <div className="modal-footer-btns">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" id="btn-submit-faculty">
              Register Faculty
            </button>
          </div>
        </form>
      </Modal>

      <style dangerouslySetInnerHTML={{__html: `
        .faculties-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 1.5rem;
        }

        .faculty-card {
          padding: 1.5rem;
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }

        .faculty-card-header {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          position: relative;
        }

        .avatar-circle {
          width: 44px;
          height: 44px;
          border-radius: 50%;
          background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
          color: white;
          font-weight: 700;
          font-size: 0.9rem;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--card-border);
        }

        .faculty-meta h4 {
          font-size: 1rem;
          font-weight: 700;
        }

        .faculty-meta span {
          font-size: 0.75rem;
          color: var(--text-muted);
        }

        .status-badge-inline {
          position: absolute;
          top: 0;
          right: 0;
          font-size: 0.65rem;
          font-weight: 700;
          text-transform: uppercase;
          padding: 0.15rem 0.5rem;
          border-radius: var(--radius-full);
          background: var(--card-border);
          color: var(--text-muted);
        }

        .status-badge-inline.active {
          background: var(--success-bg);
          color: var(--success-color);
        }

        .faculty-card-body {
          border-top: 1px solid var(--card-border);
          border-bottom: 1px solid var(--card-border);
          padding: 0.75rem 0;
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }

        .info-row {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.85rem;
          color: var(--text-main);
        }

        .row-icon {
          color: var(--text-muted);
        }

        .row-icon.star {
          color: var(--warning-color);
        }

        .rating-row strong {
          color: var(--text-main);
        }

        .faculty-card-actions {
          margin-top: 0.25rem;
        }
      `}} />
    </div>
  );
}
