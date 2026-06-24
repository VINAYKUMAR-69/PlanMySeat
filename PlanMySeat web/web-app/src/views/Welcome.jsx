import { useState } from 'react';
import { 
  FaArrowRight, 
  FaCheckCircle, 
  FaChartBar, 
  FaUsers, 
  FaMobileAlt,
  FaChevronLeft,
  FaChevronRight
} from 'react-icons/fa';

export default function Welcome({ onGetStarted }) {
  const [activeSlide, setActiveSlide] = useState(0);

  const onboardingSlides = [
    {
      title: 'Easy Layout Configurations',
      description: 'Quickly set up your student rosters, register classrooms, and structure exam seating capacities in minutes.',
      icon: <FaUsers />,
      tag: 'Step 1'
    },
    {
      title: 'Dynamic Automated Allocation',
      description: 'One-click seat distributions shuffling student branches to prevent collusion and respect spacing constraints.',
      icon: <FaCheckCircle />,
      tag: 'Step 2'
    },
    {
      title: 'Smart Invigilator Duty Assignments',
      description: 'Allocate active faculty members sequentially to classrooms matching duty slots without schedule conflicts.',
      icon: <FaChartBar />,
      tag: 'Step 3'
    },
    {
      title: 'Instant Exports & Reports',
      description: 'Download detailed seating grids and lists in Excel or PDF formats, ready to print or sync to the cloud.',
      icon: <FaMobileAlt />,
      tag: 'Step 4'
    }
  ];

  const nextSlide = () => {
    setActiveSlide((prev) => (prev + 1) % onboardingSlides.length);
  };

  const prevSlide = () => {
    setActiveSlide((prev) => (prev - 1 + onboardingSlides.length) % onboardingSlides.length);
  };

  return (
    <div className="welcome-container animate-fade-up">
      {/* Brand Header */}
      <nav className="welcome-nav">
        <div className="welcome-logo">
          <div className="logo-box">P</div>
          <span>PlanMySeat</span>
        </div>
        <button className="btn btn-secondary btn-nav" onClick={onGetStarted}>Sign In</button>
      </nav>

      {/* Hero Content */}
      <header className="welcome-hero">
        <span className="hero-badge">VERSION 2.0 AVAILABLE NOW</span>
        <h1>Smart Seating. Simplified.</h1>
        <p className="hero-subtitle">
          Intelligent Exam Seating System for Modern Universities.
        </p>

        {/* Feature Pills */}
        <div className="welcome-pills">
          <div className="pill"><span className="icon">🚀</span> Automated Allocation</div>
          <div className="pill"><span className="icon">📊</span> Real-time Analytics</div>
          <div className="pill"><span className="icon">👥</span> Faculty Duties</div>
          <div className="pill"><span className="icon">📱</span> Mobile Friendly</div>
        </div>

        {/* CTA Button */}
        <div className="hero-cta">
          <button className="btn btn-primary btn-lg" onClick={onGetStarted} id="btn-get-started">
            Get Started <FaArrowRight />
          </button>
        </div>
      </header>

      {/* Onboarding Carousel Slider */}
      <section className="onboarding-slider glass-card">
        <div className="slider-header">
          <span className="slider-tag">{onboardingSlides[activeSlide].tag}</span>
          <h3>{onboardingSlides[activeSlide].title}</h3>
        </div>
        <div className="slider-body">
          <div className="slider-icon-box">{onboardingSlides[activeSlide].icon}</div>
          <p>{onboardingSlides[activeSlide].description}</p>
        </div>
        <div className="slider-controls">
          <button className="slider-btn" onClick={prevSlide} aria-label="Previous slide"><FaChevronLeft /></button>
          <div className="slider-dots">
            {onboardingSlides.map((_, i) => (
              <span 
                key={i} 
                className={`dot ${activeSlide === i ? 'active' : ''}`}
                onClick={() => setActiveSlide(i)}
              ></span>
            ))}
          </div>
          <button className="slider-btn" onClick={nextSlide} aria-label="Next slide"><FaChevronRight /></button>
        </div>
      </section>

      {/* Stats Counter Section */}
      <section className="welcome-stats">
        <div className="stat-col">
          <h3>50K+</h3>
          <p>Students Managed</p>
        </div>
        <div className="stat-col">
          <h3>1,000+</h3>
          <p>Exams Scheduled</p>
        </div>
        <div className="stat-col">
          <h3>99.9%</h3>
          <p>Allocation Success</p>
        </div>
      </section>

      <style dangerouslySetInnerHTML={{__html: `
        .welcome-container {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 2rem 1.5rem;
          background: radial-gradient(circle at top, var(--primary-glow) 0%, var(--bg-color) 70%);
        }

        .welcome-nav {
          width: 100%;
          max-width: 1200px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4rem;
        }

        .welcome-logo {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-family: var(--font-display);
          font-size: 1.35rem;
          font-weight: 800;
        }

        .logo-box {
          width: 32px;
          height: 32px;
          background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
          color: white;
          font-weight: 800;
          font-size: 1.1rem;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: var(--radius-sm);
        }

        .btn-nav {
          padding: 0.5rem 1.25rem;
          font-size: 0.9rem;
        }

        .welcome-hero {
          text-align: center;
          max-width: 800px;
          margin-bottom: 3.5rem;
          display: flex;
          flex-direction: column;
          align-items: center;
        }

        .hero-badge {
          background: var(--primary-glow);
          color: var(--primary-color);
          border: 1px solid rgba(99, 102, 241, 0.2);
          padding: 0.35rem 0.85rem;
          font-size: 0.75rem;
          font-weight: 700;
          letter-spacing: 0.05em;
          border-radius: var(--radius-full);
          margin-bottom: 1.25rem;
        }

        .welcome-hero h1 {
          font-size: 3.25rem;
          font-weight: 800;
          line-height: 1.15;
          letter-spacing: -2px;
          margin-bottom: 1rem;
          background: linear-gradient(135deg, var(--text-main) 30%, var(--primary-color));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .hero-subtitle {
          font-size: 1.2rem;
          color: var(--text-muted);
          max-width: 600px;
          margin-bottom: 2rem;
        }

        .welcome-pills {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 0.75rem;
          margin-bottom: 2.25rem;
        }

        .pill {
          background: var(--card-bg);
          border: 1px solid var(--card-border);
          padding: 0.5rem 1.25rem;
          border-radius: var(--radius-full);
          font-size: 0.85rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 0.5rem;
          box-shadow: var(--shadow-sm);
        }

        .btn-lg {
          padding: 0.9rem 2.25rem;
          font-size: 1.05rem;
          border-radius: var(--radius-sm);
        }

        .onboarding-slider {
          width: 100%;
          max-width: 500px;
          padding: 2rem;
          text-align: center;
          margin-bottom: 4rem;
        }

        .slider-header {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 0.5rem;
          margin-bottom: 1rem;
        }

        .slider-tag {
          font-size: 0.75rem;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          color: var(--accent-color);
        }

        .slider-header h3 {
          font-size: 1.25rem;
          font-weight: 700;
        }

        .slider-body {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 1rem;
          margin-bottom: 1.5rem;
          min-height: 140px;
        }

        .slider-icon-box {
          width: 60px;
          height: 60px;
          background: var(--primary-glow);
          color: var(--primary-color);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 1.5rem;
        }

        .slider-body p {
          font-size: 0.95rem;
          color: var(--text-muted);
          line-height: 1.6;
        }

        .slider-controls {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .slider-btn {
          background: transparent;
          border: none;
          color: var(--text-muted);
          cursor: pointer;
          font-size: 1.1rem;
          padding: 0.5rem;
          display: flex;
          align-items: center;
          transition: color 0.2s;
        }

        .slider-btn:hover {
          color: var(--primary-color);
        }

        .slider-dots {
          display: flex;
          gap: 0.5rem;
        }

        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: var(--card-border);
          cursor: pointer;
          transition: background-color 0.2s, transform 0.2s;
        }

        .dot.active {
          background: var(--primary-color);
          transform: scale(1.2);
        }

        .welcome-stats {
          width: 100%;
          max-width: 900px;
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 2rem;
          text-align: center;
          border-top: 1px solid var(--card-border);
          padding-top: 3rem;
        }

        .stat-col h3 {
          font-size: 2.5rem;
          font-weight: 800;
          color: var(--primary-color);
          letter-spacing: -1px;
        }

        .stat-col p {
          font-size: 0.9rem;
          color: var(--text-muted);
          font-weight: 500;
        }

        @media (max-width: 768px) {
          .welcome-hero h1 {
            font-size: 2.25rem;
          }
          .welcome-stats {
            grid-template-columns: 1fr;
            gap: 1.5rem;
            padding-top: 2rem;
          }
          .welcome-nav {
            margin-bottom: 2rem;
          }
        }
      `}} />
    </div>
  );
}
