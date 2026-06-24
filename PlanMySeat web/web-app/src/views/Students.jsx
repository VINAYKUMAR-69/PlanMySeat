import { useState } from 'react';
import { useApp } from '../context/AppContext';
import Modal from '../components/Modal';
import { FaSearch, FaPlus, FaTrash, FaEdit, FaUpload, FaDownload, FaSync } from 'react-icons/fa';

export default function Students() {
  const { 
    students, 
    addStudent, 
    updateStudent, 
    deleteStudent, 
    bulkDeleteStudents, 
    fetchStudents,
    loading 
  } = useApp();

  // Search & Filter State
  const [search, setSearch] = useState('');
  const [filterBranch, setFilterBranch] = useState('');
  const [filterExam, setFilterExam] = useState('');
  const [selectedRegs, setSelectedRegs] = useState([]);

  // Modal forms states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' | 'edit'
  const [targetRegNo, setTargetRegNo] = useState(''); // for edits

  // Form Fields
  const [name, setName] = useState('');
  const [regNo, setRegNo] = useState('');
  const [branch, setBranch] = useState('');
  const [year, setYear] = useState('1');
  const [examType, setExamType] = useState('Model');

  // CSV Uploader states
  const [isCsvModalOpen, setIsCsvModalOpen] = useState(false);
  const [csvProgress, setCsvProgress] = useState(-1); // -1 = idle, 0-100 = uploading
  const [csvRows, setCsvRows] = useState([]); // parsed preview rows
  const [csvError, setCsvError] = useState('');
  const [csvResult, setCsvResult] = useState(null); // { success, failed, total }
  const [isDragOver, setIsDragOver] = useState(false);

  // Collect unique values for drop-downs
  const branches = [...new Set(students.map(s => s.branch).filter(Boolean))];
  const examTypes = [...new Set(students.map(s => s.examType).filter(Boolean))];

  // Filter logic
  const filteredStudents = students.filter(s => {
    const matchesSearch = 
      (s.name && s.name.toLowerCase().includes(search.toLowerCase())) ||
      (s.regNo && s.regNo.toLowerCase().includes(search.toLowerCase()));
    
    const matchesBranch = filterBranch ? s.branch === filterBranch : true;
    const matchesExam = filterExam ? s.examType === filterExam : true;

    return matchesSearch && matchesBranch && matchesExam;
  });

  const handleSelectStudent = (reg) => {
    setSelectedRegs(prev => 
      prev.includes(reg) ? prev.filter(r => r !== reg) : [...prev, reg]
    );
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedRegs(filteredStudents.map(s => s.regNo));
    } else {
      setSelectedRegs([]);
    }
  };

  const handleOpenAdd = () => {
    setModalMode('add');
    setName('');
    setRegNo('');
    setBranch('');
    setYear('1');
    setExamType('Model');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (student) => {
    setModalMode('edit');
    setTargetRegNo(student.regNo);
    setName(student.name || '');
    setRegNo(student.regNo || '');
    setBranch(student.branch || '');
    setYear(String(student.year || 1));
    setExamType(student.examType || 'Model');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name || !regNo || !branch) return;

    try {
      if (modalMode === 'add') {
        await addStudent(name, regNo, branch, year, examType);
      } else {
        await updateStudent(targetRegNo, { name, branch, year, examType });
      }
      setIsModalOpen(false);
    } catch (e) {
      alert(e.message || 'Operation failed');
    }
  };

  const handleDelete = async (studentId, reg) => {
    if (window.confirm(`Are you sure you want to delete student ${reg}?`)) {
      try {
        await deleteStudent(studentId, reg);
      } catch (e) {
        alert(e.message || 'Delete failed');
      }
    }
  };

  const handleBulkDelete = async () => {
    if (selectedRegs.length === 0) return;
    if (window.confirm(`Are you sure you want to delete ${selectedRegs.length} selected students?`)) {
      try {
        await bulkDeleteStudents(selectedRegs);
        setSelectedRegs([]);
      } catch (e) {
        alert(e.message || 'Bulk delete failed');
      }
    }
  };

  // ─── CSV helpers ─────────────────────────────────────────────────────────────

  /** Parse raw CSV text into an array of row objects (does not upload anything) */
  const parseCSV = (text) => {
    const lines = text
      .split('\n')
      .map(l => l.trim())
      .filter(l => l.length > 0);
    const startIdx = lines[0].toLowerCase().includes('name') ? 1 : 0;
    return lines.slice(startIdx).map(line => {
      const cols = line.split(',').map(c => c.trim().replace(/^"|"$/g, ''));
      return {
        name:     cols[0] || '',
        regNo:    cols[1] || '',
        branch:   cols[2] || '',
        year:     cols[3] || '1',
        examType: cols[4] || 'Model',
      };
    }).filter(r => r.name && r.regNo && r.branch);
  };

  /** Read a File and populate the preview table — does NOT upload */
  const loadCsvPreview = (file) => {
    setCsvError('');
    setCsvResult(null);
    setCsvProgress(-1);
    const reader = new FileReader();
    reader.onload = (evt) => {
      const rows = parseCSV(evt.target.result);
      if (rows.length === 0) {
        setCsvError('No valid rows found. Make sure the CSV has columns: Name, RegNo, Branch, Year, ExamType.');
        setCsvRows([]);
      } else {
        setCsvRows(rows);
      }
    };
    reader.onerror = () => setCsvError('Failed to read file.');
    reader.readAsText(file);
  };

  /** Called when the user clicks the file input */
  const handleCsvFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) loadCsvPreview(file);
  };

  /** Drag-and-drop handlers */
  const handleDragOver  = (e) => { e.preventDefault(); setIsDragOver(true); };
  const handleDragLeave = ()  => setIsDragOver(false);
  const handleDrop      = (e) => {
    e.preventDefault();
    setIsDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file && file.name.endsWith('.csv')) loadCsvPreview(file);
    else setCsvError('Please drop a valid .csv file.');
  };

  /**
   * Upload all parsed rows in parallel batches of BATCH_SIZE.
   * This is ~10x faster than the old sequential approach for large CSVs.
   */
  const BATCH_SIZE = 10;

  const handleStartImport = async () => {
    if (csvRows.length === 0) return;
    setCsvProgress(0);
    setCsvResult(null);
    setCsvError('');

    let completed = 0;
    let successCount = 0;
    let failedCount = 0;
    const total = csvRows.length;

    // Split rows into chunks
    const chunks = [];
    for (let i = 0; i < total; i += BATCH_SIZE) {
      chunks.push(csvRows.slice(i, i + BATCH_SIZE));
    }

    for (const chunk of chunks) {
      // Fire all requests in the current chunk simultaneously
      const results = await Promise.allSettled(
        chunk.map(r => addStudent(r.name, r.regNo, r.branch, r.year, r.examType))
      );
      results.forEach(r => r.status === 'fulfilled' ? successCount++ : failedCount++);
      completed += chunk.length;
      setCsvProgress(Math.round((completed / total) * 100));
    }

    setCsvProgress(-1);
    setCsvResult({ success: successCount, failed: failedCount, total });
    setCsvRows([]); // clear preview
  };

  const handleCloseCsvModal = () => {
    if (csvProgress !== -1) return; // block close while uploading
    setIsCsvModalOpen(false);
    setCsvRows([]);
    setCsvError('');
    setCsvResult(null);
    setIsDragOver(false);
  };

  const handleExportCsv = () => {
    // Generate CSV contents
    const headers = 'Name,Registration Number,Branch,Year,Exam Type\n';
    const rows = students.map(s => 
      `"${s.name}","${s.regNo}","${s.branch}",${s.year},"${s.examType}"`
    ).join('\n');
    
    const blob = new Blob([headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "PlanMySeat_Students.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="students-view animate-fade-up">
      {/* Search and Action bars */}
      <div className="action-bar glass-card padding-md">
        <div className="search-group">
          <div className="input-with-icon">
            <FaSearch className="input-icon" />
            <input 
              type="text" 
              className="form-input" 
              placeholder="Search by student name or reg number..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              id="student-search-input"
            />
          </div>
        </div>

        <div className="filters-group">
          <div className="filter-item">
            <select 
              className="form-input select-sm" 
              value={filterBranch} 
              onChange={e => setFilterBranch(e.target.value)}
              id="student-filter-branch"
            >
              <option value="">All Branches</option>
              {branches.map(b => <option key={b} value={b}>{b}</option>)}
            </select>
          </div>

          <div className="filter-item">
            <select 
              className="form-input select-sm" 
              value={filterExam} 
              onChange={e => setFilterExam(e.target.value)}
              id="student-filter-exam"
            >
              <option value="">All Exams</option>
              {examTypes.map(e => <option key={e} value={e}>{e}</option>)}
            </select>
          </div>
        </div>

        <div className="buttons-group">
          {selectedRegs.length > 0 && (
            <button className="btn btn-danger btn-sm" onClick={handleBulkDelete} id="btn-bulk-delete">
              <FaTrash /> Delete ({selectedRegs.length})
            </button>
          )}
          <button className="btn btn-secondary btn-sm" onClick={() => setIsCsvModalOpen(true)} id="btn-import-csv">
            <FaUpload /> Import CSV
          </button>
          <button className="btn btn-secondary btn-sm" onClick={handleExportCsv} id="btn-export-csv">
            <FaDownload /> Export CSV
          </button>
          <button className="btn btn-primary btn-sm" onClick={handleOpenAdd} id="btn-add-student">
            <FaPlus /> Add Student
          </button>
          <button className="btn btn-secondary btn-sm refresh-btn" onClick={fetchStudents} disabled={loading.students} title="Reload Data">
            <FaSync className={loading.students ? 'spinner' : ''} />
          </button>
        </div>
      </div>

      {/* Roster Listing */}
      <div className="glass-card padding-md margin-top-lg">
        <h3>Student Directory</h3>
        <p className="card-subtitle margin-bottom-md">Total registered: {filteredStudents.length} entries</p>
        
        {filteredStudents.length > 0 ? (
          <div className="table-container">
            <table className="premium-table">
              <thead>
                <tr>
                  <th width="40">
                    <input 
                      type="checkbox" 
                      onChange={handleSelectAll} 
                      checked={filteredStudents.length > 0 && selectedRegs.length === filteredStudents.length}
                    />
                  </th>
                  <th>Name</th>
                  <th>Registration No</th>
                  <th>Branch</th>
                  <th>Academic Year</th>
                  <th>Exam Assignment</th>
                  <th width="100">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredStudents.map((student) => (
                  <tr key={student.id}>
                    <td>
                      <input 
                        type="checkbox" 
                        checked={selectedRegs.includes(student.regNo)}
                        onChange={() => handleSelectStudent(student.regNo)}
                      />
                    </td>
                    <td className="bold-name">{student.name}</td>
                    <td>{student.regNo}</td>
                    <td>{student.branch}</td>
                    <td>Year {student.year}</td>
                    <td>
                      <span className="badge badge-warning">{student.examType || 'Model'}</span>
                    </td>
                    <td>
                      <div className="actions-flex">
                        <button className="icon-action-btn" onClick={() => handleOpenEdit(student)} title="Edit Student">
                          <FaEdit />
                        </button>
                        <button className="icon-action-btn delete" onClick={() => handleDelete(student.id, student.regNo)} title="Delete Student">
                          <FaTrash />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <p>No students found matching your criteria.</p>
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        title={modalMode === 'add' ? 'Add New Student' : 'Edit Student Credentials'}
      >
        <form onSubmit={handleSubmit} className="student-form">
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input 
              type="text" 
              className="form-input" 
              value={name} 
              onChange={e => setName(e.target.value)} 
              placeholder="John Doe"
              required 
              id="student-name"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Registration Number</label>
            <input 
              type="text" 
              className="form-input" 
              value={regNo} 
              onChange={e => setRegNo(e.target.value)} 
              placeholder="RA2111003010001"
              required
              disabled={modalMode === 'edit'}
              id="student-regno"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Branch Department</label>
            <input 
              type="text" 
              className="form-input" 
              value={branch} 
              onChange={e => setBranch(e.target.value)} 
              placeholder="CSE"
              required 
              id="student-branch"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Academic Year</label>
            <select 
              className="form-input" 
              value={year} 
              onChange={e => setYear(e.target.value)}
              id="student-year"
            >
              <option value="1">1st Year</option>
              <option value="2">2nd Year</option>
              <option value="3">3rd Year</option>
              <option value="4">4th Year</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Exam Assignment Type</label>
            <input 
              type="text" 
              className="form-input" 
              value={examType} 
              onChange={e => setExamType(e.target.value)} 
              placeholder="Semester / Model"
              id="student-examtype"
            />
          </div>

          <div className="modal-footer-btns">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" id="btn-submit-student">
              {modalMode === 'add' ? 'Add Student' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>

      {/* CSV Import Modal */}
      <Modal
        isOpen={isCsvModalOpen}
        onClose={handleCloseCsvModal}
        title="Import Students via CSV"
      >
        <div className="csv-upload-panel">

          {/* ── Result Summary (shown after upload) ─────────────────── */}
          {csvResult && (
            <div className={`csv-result-banner ${csvResult.failed === 0 ? 'success' : 'partial'}`}>
              <span className="result-icon">{csvResult.failed === 0 ? '✅' : '⚠️'}</span>
              <div>
                <strong>{csvResult.success} of {csvResult.total}</strong> students imported.
                {csvResult.failed > 0 && <span className="result-warn"> {csvResult.failed} failed (duplicates or invalid data).</span>}
              </div>
              <button className="btn btn-primary btn-sm" onClick={handleCloseCsvModal}>Close</button>
            </div>
          )}

          {/* ── Upload Progress Bar ─────────────────────────────────── */}
          {csvProgress !== -1 && (
            <div className="csv-progress-wrap">
              <div className="csv-progress-header">
                <FaSync className="spinner" />
                <span>Importing students… {csvProgress}% ({Math.round(csvProgress * csvRows.length / 100)} of {csvRows.length})</span>
              </div>
              <div className="csv-progress-bar-bg">
                <div className="csv-progress-bar-fill" style={{ width: `${csvProgress}%` }} />
              </div>
            </div>
          )}

          {/* ── Drag-and-Drop Zone ──────────────────────────────────── */}
          {csvProgress === -1 && !csvResult && (
            <>
              <p className="description">
                Drop your CSV file below or click to browse. Format: <strong>Name, RegNo, Branch, Year, ExamType</strong>.
              </p>

              <div
                className={`upload-dropzone ${isDragOver ? 'drag-over' : ''}`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
              >
                <FaUpload className="upload-icon" />
                <p className="dropzone-hint">Drag & drop a CSV file here</p>
                <label className="btn btn-secondary">
                  Browse File
                  <input type="file" accept=".csv" onChange={handleCsvFileSelect} style={{ display: 'none' }} />
                </label>
              </div>

              {csvError && (
                <div className="csv-error-msg">⚠️ {csvError}</div>
              )}

              {/* ── Preview Table ──────────────────────────────────── */}
              {csvRows.length > 0 && (
                <div className="csv-preview">
                  <div className="csv-preview-header">
                    <strong>{csvRows.length} students ready to import</strong>
                    <button className="btn btn-primary btn-sm" onClick={handleStartImport}>
                      🚀 Start Import
                    </button>
                  </div>
                  <div className="csv-preview-table-wrap">
                    <table className="premium-table csv-preview-table">
                      <thead>
                        <tr>
                          <th>#</th>
                          <th>Name</th>
                          <th>Reg No</th>
                          <th>Branch</th>
                          <th>Year</th>
                          <th>Exam</th>
                        </tr>
                      </thead>
                      <tbody>
                        {csvRows.slice(0, 8).map((r, i) => (
                          <tr key={i}>
                            <td className="row-num">{i + 1}</td>
                            <td>{r.name}</td>
                            <td>{r.regNo}</td>
                            <td>{r.branch}</td>
                            <td>Year {r.year}</td>
                            <td><span className="badge badge-warning">{r.examType}</span></td>
                          </tr>
                        ))}
                        {csvRows.length > 8 && (
                          <tr>
                            <td colSpan={6} className="more-rows">…and {csvRows.length - 8} more rows</td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </Modal>

      <style dangerouslySetInnerHTML={{__html: `
        .action-bar {
          display: flex;
          align-items: center;
          gap: 1rem;
          flex-wrap: wrap;
        }

        .search-group {
          flex: 1.5;
          min-width: 250px;
        }

        .filters-group {
          display: flex;
          gap: 0.5rem;
          flex: 1;
        }

        .buttons-group {
          display: flex;
          gap: 0.5rem;
          align-items: center;
          flex-wrap: wrap;
        }

        .select-sm {
          padding: 0.6rem 0.75rem;
          font-size: 0.85rem;
        }

        .btn-sm {
          padding: 0.6rem 1rem;
          font-size: 0.85rem;
        }

        .bold-name {
          font-weight: 600;
          color: var(--text-main);
        }

        .actions-flex {
          display: flex;
          gap: 0.5rem;
        }

        .icon-action-btn {
          background: transparent;
          border: none;
          cursor: pointer;
          color: var(--text-muted);
          font-size: 1rem;
          padding: 0.35rem;
          border-radius: 4px;
          display: flex;
          align-items: center;
          transition: background-color 0.2s, color 0.2s;
        }

        .icon-action-btn:hover {
          background: var(--bg-color);
          color: var(--primary-color);
        }

        .icon-action-btn.delete:hover {
          color: var(--error-color);
          background: var(--error-bg);
        }

        .refresh-btn {
          padding: 0.6rem;
        }

        .csv-upload-panel {
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }

        .upload-dropzone {
          border: 2.5px dashed var(--card-border);
          border-radius: var(--radius-md);
          padding: 2.5rem 1.5rem;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 1rem;
          background: var(--bg-color);
          transition: border-color 0.2s, background 0.2s;
          cursor: pointer;
        }

        .upload-dropzone:hover,
        .upload-dropzone.drag-over {
          border-color: var(--primary-color);
          background: var(--primary-glow);
        }

        .dropzone-hint {
          font-size: 0.9rem;
          color: var(--text-muted);
          margin: 0;
        }

        .upload-icon {
          font-size: 2.5rem;
          color: var(--text-muted);
        }

        /* Progress bar */
        .csv-progress-wrap {
          display: flex;
          flex-direction: column;
          gap: 0.6rem;
        }

        .csv-progress-header {
          display: flex;
          align-items: center;
          gap: 0.6rem;
          font-size: 0.9rem;
          font-weight: 600;
          color: var(--text-main);
        }

        .csv-progress-bar-bg {
          width: 100%;
          height: 10px;
          background: var(--card-border);
          border-radius: 99px;
          overflow: hidden;
        }

        .csv-progress-bar-fill {
          height: 100%;
          background: linear-gradient(90deg, var(--primary-color), var(--accent-color, var(--primary-color)));
          border-radius: 99px;
          transition: width 0.3s ease;
        }

        /* Result banner */
        .csv-result-banner {
          display: flex;
          align-items: center;
          gap: 1rem;
          padding: 1rem 1.2rem;
          border-radius: var(--radius-sm);
          font-size: 0.9rem;
          border: 1px solid;
        }

        .csv-result-banner.success {
          background: var(--success-bg);
          border-color: rgba(16,185,129,0.3);
          color: var(--success-color);
        }

        .csv-result-banner.partial {
          background: var(--warning-bg, #fff7ed);
          border-color: rgba(245,158,11,0.3);
          color: var(--warning-color, #92400e);
        }

        .result-icon { font-size: 1.4rem; }
        .result-warn { color: var(--error-color); margin-left: 0.25rem; }

        /* Preview table */
        .csv-preview {
          display: flex;
          flex-direction: column;
          gap: 0.75rem;
        }

        .csv-preview-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .csv-preview-table-wrap {
          max-height: 240px;
          overflow-y: auto;
          border-radius: var(--radius-sm);
          border: 1px solid var(--card-border);
        }

        .csv-preview-table {
          font-size: 0.8rem;
        }

        .row-num {
          color: var(--text-muted);
          font-size: 0.75rem;
        }

        .more-rows {
          text-align: center;
          color: var(--text-muted);
          font-style: italic;
          padding: 0.5rem;
        }

        .csv-error-msg {
          color: var(--error-color);
          background: var(--error-bg);
          padding: 0.75rem 1rem;
          border-radius: var(--radius-sm);
          font-size: 0.85rem;
          border: 1px solid rgba(239,68,68,0.2);
        }

        /* Old progress indicator (kept for compatibility) */
        .progress-indicator {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.95rem;
          font-weight: 600;
        }

        .progress-spinner {
          font-size: 1.5rem;
          color: var(--primary-color);
        }
      `}} />
    </div>
  );
}
