import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { api } from '../api/client';
import { FaCheckCircle, FaExclamationTriangle, FaStar, FaBug, FaLightbulb, FaCommentAlt } from 'react-icons/fa';

export default function Support() {
  const { user, triggerSystemNotification } = useApp();

  // Mode: 'feedback' | 'bug' | 'feature'
  const [tab, setTab] = useState('feedback');

  // UI Message States
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Feedback State
  const [fbType, setFbType] = useState('General');
  const [fbRating, setFbRating] = useState(5);
  const [fbMessage, setFbMessage] = useState('');

  // Bug Report State
  const [bugTitle, setBugTitle] = useState('');
  const [bugSeverity, setBugSeverity] = useState('Medium'); // 'Low' | 'Medium' | 'High' | 'Critical'
  const [bugFrequency, setBugFrequency] = useState('Rarely');
  const [bugDesc, setBugDesc] = useState('');
  const [bugSteps, setBugSteps] = useState('');
  const [bugExpect, setBugExpect] = useState('');
  const [bugActual, setBugActual] = useState('');

  // Feature Request State
  const [featTitle, setFeatTitle] = useState('');
  const [featDesc, setFeatDesc] = useState('');
  const [featCategory, setFeatCategory] = useState('UIUX'); // 'Core' | 'UIUX' | 'Performance' | 'Integration'
  const [featPriority, setFeatPriority] = useState('Medium');
  const [featUseCase, setFeatUseCase] = useState('');
  const [featBenefit, setFeatBenefit] = useState('');

  const resetForms = () => {
    setMsg('');
    setErr('');
    // Clear feedback
    setFbMessage('');
    setFbRating(5);
    // Clear bug
    setBugTitle('');
    setBugDesc('');
    setBugSteps('');
    setBugExpect('');
    setBugActual('');
    // Clear feature
    setFeatTitle('');
    setFeatDesc('');
    setFeatUseCase('');
    setFeatBenefit('');
  };

  const handleFeedback = async (e) => {
    e.preventDefault();
    if (!fbMessage) return;

    setMsg('');
    setErr('');
    setIsSubmitting(true);
    try {
      await api.submitFeedback({
        name: user?.name || 'Anonymous',
        email: user?.email || 'anon@example.com',
        feedbackType: fbType,
        rating: fbRating,
        message: fbMessage
      });
      setMsg('Feedback submitted! Thank you.');
      await triggerSystemNotification("Feedback Submitted", "Thank you for rating our seating services.");
      setFbMessage('');
    } catch (e) {
      setErr(e.message || 'Submission failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleBug = async (e) => {
    e.preventDefault();
    if (!bugTitle || !bugDesc) return;

    setMsg('');
    setErr('');
    setIsSubmitting(true);
    try {
      await api.submitBugReport({
        title: bugTitle,
        severity: bugSeverity,
        frequency: bugFrequency,
        description: bugDesc,
        stepsToReproduce: bugSteps,
        expectedBehavior: bugExpect,
        actualBehavior: bugActual
      });
      setMsg('Bug report submitted. Our engineers are investigating.');
      await triggerSystemNotification("Bug Logged", `Logged critical issue: ${bugTitle}`);
      resetForms();
    } catch (e) {
      setErr(e.message || 'Submission failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleFeature = async (e) => {
    e.preventDefault();
    if (!featTitle || !featDesc) return;

    setMsg('');
    setErr('');
    setIsSubmitting(true);
    try {
      await api.submitFeatureRequest({
        title: featTitle,
        description: featDesc,
        category: featCategory,
        priority: featPriority,
        useCase: featUseCase,
        expectedBenefit: featBenefit
      });
      setMsg('Feature proposal logged in repository backlog.');
      await triggerSystemNotification("Feature Requested", `Proposed feature enhancement: ${featTitle}`);
      resetForms();
    } catch (e) {
      setErr(e.message || 'Submission failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="support-view animate-fade-up">
      {/* Selector Tabs */}
      <div className="support-tabs glass-card padding-md no-padding-bottom">
        <div className="tabs-header-row">
          <h3>Support & Backlogs Center</h3>
          <p className="card-subtitle">Connect with engineers and submit feedback loops</p>
        </div>
        <div className="tab-buttons-row">
          <button 
            className={`tab-btn ${tab === 'feedback' ? 'active' : ''}`}
            onClick={() => { resetForms(); setTab('feedback'); }}
            id="tab-feedback"
          >
            <FaCommentAlt /> Feedback
          </button>
          <button 
            className={`tab-btn ${tab === 'bug' ? 'active' : ''}`}
            onClick={() => { resetForms(); setTab('bug'); }}
            id="tab-bug"
          >
            <FaBug /> Report Bug
          </button>
          <button 
            className={`tab-btn ${tab === 'feature' ? 'active' : ''}`}
            onClick={() => { resetForms(); setTab('feature'); }}
            id="tab-feature"
          >
            <FaLightbulb /> Request Feature
          </button>
        </div>
      </div>

      {/* Forms contents */}
      <div className="glass-card padding-md margin-top-lg">
        {msg && <div className="alert-msg alert-success flex items-center gap-xs"><FaCheckCircle /> <span>{msg}</span></div>}
        {err && <div className="alert-msg alert-danger flex items-center gap-xs"><FaExclamationTriangle /> <span>{err}</span></div>}

        {/* --- Feedback Form --- */}
        {tab === 'feedback' && (
          <form onSubmit={handleFeedback}>
            <h4>Submit System Feedback</h4>
            <p className="card-subtitle margin-bottom-md">Help us optimize classroom allocations and invigilator rosters</p>

            <div className="form-group">
              <label className="form-label">Feedback Category</label>
              <select className="form-input" value={fbType} onChange={e => setFbType(e.target.value)}>
                <option value="General">General Seating Routine</option>
                <option value="AI Solver">AI Allocation Packing</option>
                <option value="UI Design">User Interface Aesthetics</option>
                <option value="Performance">Response Speed & timeouts</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">System Rating</label>
              <div className="star-rating-row">
                {[1, 2, 3, 4, 5].map((val) => (
                  <button
                    key={val}
                    type="button"
                    className={`star-btn ${fbRating >= val ? 'filled' : ''}`}
                    onClick={() => setFbRating(val)}
                  >
                    <FaStar />
                  </button>
                ))}
                <span className="rating-label">({fbRating} / 5 stars)</span>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Message Details</label>
              <textarea 
                className="form-input textarea-tall"
                placeholder="What was your experience using the seating arrangement solver?"
                value={fbMessage}
                onChange={e => setFbMessage(e.target.value)}
                required
                id="feedback-message"
              ></textarea>
            </div>

            <button type="submit" className="btn btn-primary" disabled={isSubmitting} id="btn-submit-feedback">
              {isSubmitting ? 'Sending...' : 'Submit Feedback'}
            </button>
          </form>
        )}

        {/* --- Bug Report Form --- */}
        {tab === 'bug' && (
          <form onSubmit={handleBug}>
            <h4>File Bug Report</h4>
            <p className="card-subtitle margin-bottom-md">Help us debug error triggers and system latency loops</p>

            <div className="form-group">
              <label className="form-label">Defect Title</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="e.g. Allocation capacity deficit calculation error"
                value={bugTitle}
                onChange={e => setBugTitle(e.target.value)}
                required 
                id="bug-title"
              />
            </div>

            <div className="form-group-split">
              <div className="form-group">
                <label className="form-label">Severity Level</label>
                <select className="form-input" value={bugSeverity} onChange={e => setBugSeverity(e.target.value)}>
                  <option value="Low">Low (Visual layout alignment)</option>
                  <option value="Medium">Medium (Incorrect calculations)</option>
                  <option value="High">High (API endpoint timeout errors)</option>
                  <option value="Critical">Critical (Application crash/data loss)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Occurrence Frequency</label>
                <select className="form-input" value={bugFrequency} onChange={e => setBugFrequency(e.target.value)}>
                  <option value="Rarely">Once (Hard to reproduce)</option>
                  <option value="Sometimes">Occasionally (Depends on dataset size)</option>
                  <option value="Always">Consistently (Fires every run)</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Steps to Reproduce</label>
              <textarea 
                className="form-input textarea-short"
                placeholder="1. Navigate to Seat Allocation Setup...&#10;2. Decrease rooms count...&#10;3. Click submit"
                value={bugSteps}
                onChange={e => setBugSteps(e.target.value)}
                id="bug-steps"
              ></textarea>
            </div>

            <div className="form-group">
              <label className="form-label">Description & details</label>
              <textarea 
                className="form-input textarea-short"
                placeholder="Provide a detailed description of the error..."
                value={bugDesc}
                onChange={e => setBugDesc(e.target.value)}
                required
                id="bug-description"
              ></textarea>
            </div>

            <div className="form-group-split">
              <div className="form-group">
                <label className="form-label">Expected Behavior</label>
                <textarea 
                  className="form-input textarea-short"
                  placeholder="What should the system have displayed?"
                  value={bugExpect}
                  onChange={e => setBugExpect(e.target.value)}
                  id="bug-expected"
                ></textarea>
              </div>

              <div className="form-group">
                <label className="form-label">Actual Behavior</label>
                <textarea 
                  className="form-input textarea-short"
                  placeholder="What did the system actually do?"
                  value={bugActual}
                  onChange={e => setBugActual(e.target.value)}
                  id="bug-actual"
                ></textarea>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={isSubmitting} id="btn-submit-bug">
              {isSubmitting ? 'Filing Report...' : 'File Bug Report'}
            </button>
          </form>
        )}

        {/* --- Feature Request Form --- */}
        {tab === 'feature' && (
          <form onSubmit={handleFeature}>
            <h4>Propose Backlog Feature</h4>
            <p className="card-subtitle margin-bottom-md">Suggest enhancements for the PlanMySeat platform roadmap</p>

            <div className="form-group">
              <label className="form-label">Feature Name</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="e.g. Add Excel bulk classroom importer"
                value={featTitle}
                onChange={e => setFeatTitle(e.target.value)}
                required 
                id="feature-title"
              />
            </div>

            <div className="form-group-split">
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="form-input" value={featCategory} onChange={e => setFeatCategory(e.target.value)}>
                  <option value="Core">Core Allocation Rules</option>
                  <option value="UIUX">User Experience Improvement</option>
                  <option value="Performance">Optimizations & Speeds</option>
                  <option value="Integration">Third Party Tools Integrations</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Development Priority</label>
                <select className="form-input" value={featPriority} onChange={e => setFeatPriority(e.target.value)}>
                  <option value="Low">Low (Backlog backlog)</option>
                  <option value="Medium">Medium (Recommended addition)</option>
                  <option value="High">High (Immediate necessity)</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Description & Design concept</label>
              <textarea 
                className="form-input textarea-short"
                placeholder="Describe the new feature and how it should look/work..."
                value={featDesc}
                onChange={e => setFeatDesc(e.target.value)}
                required
                id="feature-desc"
              ></textarea>
            </div>

            <div className="form-group">
              <label className="form-label">Target Use Case</label>
              <textarea 
                className="form-input textarea-short"
                placeholder="Describe a scenario where a user needs this..."
                value={featUseCase}
                onChange={e => setFeatUseCase(e.target.value)}
                id="feature-usecase"
              ></textarea>
            </div>

            <div className="form-group">
              <label className="form-label">Business Value & Benefit</label>
              <textarea 
                className="form-input textarea-short"
                placeholder="How will this improve scheduling efficiencies?"
                value={featBenefit}
                onChange={e => setFeatBenefit(e.target.value)}
                id="feature-benefit"
              ></textarea>
            </div>

            <button type="submit" className="btn btn-primary" disabled={isSubmitting} id="btn-submit-feature">
              {isSubmitting ? 'Logging Backlog...' : 'Log Backlog Proposal'}
            </button>
          </form>
        )}
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        .support-tabs {
          padding-bottom: 0;
        }

        .tabs-header-row {
          margin-bottom: 1.25rem;
        }

        .tab-buttons-row {
          display: flex;
          gap: 0.5rem;
        }

        .tab-btn {
          background: transparent;
          border: none;
          color: var(--text-muted);
          font-family: var(--font-display);
          font-weight: 600;
          font-size: 0.95rem;
          padding: 0.75rem 1.25rem;
          cursor: pointer;
          border-bottom: 3px solid transparent;
          display: flex;
          align-items: center;
          gap: 0.5rem;
          transition: all 0.2s;
        }

        .tab-btn:hover {
          color: var(--text-main);
          background: rgba(255, 255, 255, 0.02);
        }

        .tab-btn.active {
          color: var(--primary-color);
          border-bottom-color: var(--primary-color);
        }

        .star-rating-row {
          display: flex;
          align-items: center;
          gap: 0.35rem;
        }

        .star-btn {
          background: transparent;
          border: none;
          color: var(--card-border);
          font-size: 1.5rem;
          cursor: pointer;
          transition: transform 0.15s, color 0.15s;
          display: flex;
          align-items: center;
        }

        .star-btn:hover {
          transform: scale(1.2);
        }

        .star-btn.filled {
          color: var(--warning-color);
        }

        .rating-label {
          font-size: 0.85rem;
          color: var(--text-muted);
          font-weight: 500;
          margin-left: 0.5rem;
        }

        .textarea-tall {
          min-height: 150px;
          resize: vertical;
        }

        .textarea-short {
          min-height: 80px;
          resize: vertical;
        }

        .form-group-split {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 1rem;
        }

        @media (max-width: 768px) {
          .form-group-split {
            grid-template-columns: 1fr;
            gap: 0;
          }
        }
      `}} />
    </div>
  );
}
