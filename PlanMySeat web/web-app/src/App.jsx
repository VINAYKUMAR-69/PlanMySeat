/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import { useApp } from './context/AppContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';

// Public Screens
import Welcome from './views/Welcome';
import Login from './views/Login';

// Authenticated Screens
import DashboardPrincipal from './views/DashboardPrincipal';
import DashboardFaculty from './views/DashboardFaculty';
import Students from './views/Students';
import Rooms from './views/Rooms';
import SeatingAllocationSetup from './views/SeatingAllocationSetup';
import GeneratedSeatingPlan from './views/GeneratedSeatingPlan';
import Faculties from './views/Faculties';
import Notifications from './views/Notifications';
import Profile from './views/Profile';
import Support from './views/Support';

export default function App() {
  const { user } = useApp();
  
  // Public routing state: 'welcome' | 'login'
  const [publicMode, setPublicMode] = useState('welcome');
  
  // Authenticated routing state: 'dashboard' | 'students' | 'rooms' | 'seating-setup' | 'reports' | 'faculties' | 'notifications' | 'support' | 'profile'
  const [currentView, setCurrentView] = useState('dashboard');

  // Trigger home redirect on user state change
  useEffect(() => {
    if (user) {
      setCurrentView('dashboard');
    } else {
      setPublicMode('welcome');
    }
  }, [user]);

  // Public View Dispatcher
  if (!user) {
    return publicMode === 'welcome' 
      ? <Welcome onGetStarted={() => setPublicMode('login')} /> 
      : <Login />;
  }

  // Determine current page title
  const getPageTitle = () => {
    switch (currentView) {
      case 'dashboard':
        return user.role === 'faculty' ? 'Faculty Duty Center' : 'Administrative Dashboard';
      case 'students':
        return 'Students Directory';
      case 'rooms':
        return 'Classrooms Inventory';
      case 'seating-setup':
        return 'Seat Allocation Setup';
      case 'reports':
        return 'Seating Plan Sheets';
      case 'faculties':
        return 'Invigilators Roster';
      case 'notifications':
        return 'Inbox Notifications';
      case 'support':
        return 'Support & Help Desk';
      case 'profile':
        return 'Profile Configurations';
      default:
        return 'PlanMySeat';
    }
  };

  return (
    <div className="app-container">
      {/* Navigation Sidebar Drawer */}
      <Sidebar currentView={currentView} setCurrentView={setCurrentView} />

      {/* Main Content Area */}
      <main className="content-wrapper">
        <Header title={getPageTitle()} />

        {/* Authenticated View Switcher */}
        {currentView === 'dashboard' && (
          user.role === 'faculty' 
            ? <DashboardFaculty setCurrentView={setCurrentView} /> 
            : <DashboardPrincipal setCurrentView={setCurrentView} />
        )}
        {currentView === 'students' && <Students />}
        {currentView === 'rooms' && <Rooms />}
        {currentView === 'seating-setup' && <SeatingAllocationSetup setCurrentView={setCurrentView} />}
        {currentView === 'reports' && <GeneratedSeatingPlan setCurrentView={setCurrentView} />}
        {currentView === 'faculties' && <Faculties />}
        {currentView === 'notifications' && <Notifications />}
        {currentView === 'support' && <Support />}
        {currentView === 'profile' && <Profile />}
      </main>
    </div>
  );
}
