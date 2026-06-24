import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { auth, db } from '../firebase';
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  sendPasswordResetEmail,
} from 'firebase/auth';
import { doc, setDoc, getDoc, updateDoc } from 'firebase/firestore';

const AppContext = createContext();

export const useApp = () => useContext(AppContext);

export const AppProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [authReady, setAuthReady] = useState(false); // wait for Firebase auth to initialise
  const [students, setStudents] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [seatingPlans, setSeatingPlans] = useState([]);
  const [currentSeatingPlan, setCurrentSeatingPlan] = useState(null);

  const [loading, setLoading] = useState({
    auth: false,
    students: false,
    rooms: false,
    faculties: false,
    notifications: false,
    reports: false,
  });

  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'light';
  });

  // Toggle Theme
  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    localStorage.setItem('theme', nextTheme);
    document.documentElement.setAttribute('data-theme', nextTheme);
  };

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  // ─────────────────────────────────────────────────────────
  // Firebase Auth State Observer
  // Automatically restores session on page reload and
  // keeps the app in sync with Firebase Authentication.
  // ─────────────────────────────────────────────────────────
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        // User is signed in — pull extra profile fields from Firestore
        let fullName = firebaseUser.displayName || firebaseUser.email;
        let role = 'principal';
        let college = '';

        try {
          const profileSnap = await getDoc(doc(db, 'users', firebaseUser.uid));
          if (profileSnap.exists()) {
            const data = profileSnap.data();
            fullName = data.full_name || fullName;
            role = data.role || role;
            college = data.college_organization || '';
          }
        } catch (err) {
          console.error('Error loading Firebase profile (offline fallback):', err);
          // Graceful fallback to localStorage cached details if Firestore is unreachable
          fullName = localStorage.getItem('user_name') || fullName;
          role = localStorage.getItem('user_role') || role;
          college = localStorage.getItem('user_college') || college;
        }

        const emailLower = firebaseUser.email.toLowerCase().trim();
        const userData = {
          uid: firebaseUser.uid,
          email: emailLower,
          name: fullName,
          role,
          college,
        };

        // Keep localStorage in sync for offline/legacy operations
        localStorage.setItem('user_email', emailLower);
        localStorage.setItem('user_name', fullName);
        localStorage.setItem('user_role', role);
        localStorage.setItem('user_college', college);

        setUser(userData);
      } else {
        // User is signed out
        localStorage.removeItem('user_email');
        localStorage.removeItem('user_name');
        localStorage.removeItem('user_role');
        localStorage.removeItem('user_college');
        setUser(null);
      }
      setAuthReady(true); // Firebase has resolved initial auth state
    });

    return () => unsubscribe();
  }, []);

  // Fetch all user-specific data when the user is set
  useEffect(() => {
    if (user && user.email) {
      refreshAllData();
    } else {
      setStudents([]);
      setRooms([]);
      setFaculties([]);
      setNotifications([]);
      setSeatingPlans([]);
      setCurrentSeatingPlan(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const refreshAllData = async () => {
    if (!user || !user.email) return;
    await Promise.all([
      fetchStudents(),
      fetchRooms(),
      fetchFaculties(),
      fetchNotifications(),
      fetchReports(),
    ]);
  };

  // ─────────────────────────────────────────────────────────
  // Auth Operations — now powered by Firebase Authentication
  // ─────────────────────────────────────────────────────────

  /**
   * Sign in with email + password via Firebase Auth.
   * Extra profile fields (name, role, college) are read from Firestore.
   */
  const loginUser = async (email, password) => {
    setLoading(prev => ({ ...prev, auth: true }));
    try {
      const emailLower = email.toLowerCase().trim();
      const credential = await signInWithEmailAndPassword(auth, emailLower, password);
      const uid = credential.user.uid;

      // Fetch extended profile from Firestore with retry/fallback
      let fullName = credential.user.displayName || emailLower;
      let role = 'principal';
      let college = '';

      try {
        const profileSnap = await getDoc(doc(db, 'users', uid));
        if (profileSnap.exists()) {
          const data = profileSnap.data();
          fullName = data.full_name || fullName;
          role = data.role || role;
          college = data.college_organization || '';
        }
      } catch (err) {
        console.error('Error loading profile during login (offline fallback):', err);
        fullName = localStorage.getItem('user_name') || fullName;
        role = localStorage.getItem('user_role') || role;
        college = localStorage.getItem('user_college') || college;
      }

      const userData = { uid, email: emailLower, name: fullName, role, college };

      localStorage.setItem('user_email', emailLower);
      localStorage.setItem('user_name', fullName);
      localStorage.setItem('user_role', role);
      localStorage.setItem('user_college', college);

      setUser(userData);
      return userData;
    } finally {
      setLoading(prev => ({ ...prev, auth: false }));
    }
  };

  /**
   * Create a new account in Firebase Auth and save the
   * extended profile (name, role, college) to Firestore.
   */
  const registerUser = async (fullName, email, password, role, collegeOrganization) => {
    setLoading(prev => ({ ...prev, auth: true }));
    try {
      const emailLower = email.toLowerCase().trim();

      // 1. Create Firebase Auth user
      const credential = await createUserWithEmailAndPassword(auth, emailLower, password);
      const uid = credential.user.uid;

      // 2. Persist extra profile fields in Firestore /users/{uid}
      try {
        await setDoc(doc(db, 'users', uid), {
          full_name: fullName,
          email: emailLower,
          role,
          college_organization: collegeOrganization,
          created_at: new Date().toISOString(),
        });
      } catch (err) {
        console.error('Error writing profile to Firestore during registration:', err);
      }

      // Synchronize to localStorage immediately so offline fallbacks work
      localStorage.setItem('user_email', emailLower);
      localStorage.setItem('user_name', fullName);
      localStorage.setItem('user_role', role);
      localStorage.setItem('user_college', collegeOrganization);

      return { email: emailLower, full_name: fullName, role };
    } finally {
      setLoading(prev => ({ ...prev, auth: false }));
    }
  };

  /**
   * Sign the current user out of Firebase Auth.
   */
  const logoutUser = async () => {
    await signOut(auth);
    // onAuthStateChanged will clean up localStorage and setUser(null)
  };

  /**
   * Send a Firebase password-reset email.
   * Returns true on success; throws on failure.
   */
  const forgotPassword = async (email) => {
    await sendPasswordResetEmail(auth, email.toLowerCase().trim());
    return true;
  };

  /**
   * Update the user profile (full name, college organization)
   * in both local context state and Firestore.
   */
  const updateUserProfile = async (fullName, collegeOrganization) => {
    if (!user) throw new Error('Not signed in.');
    const uid = user.uid;

    try {
      await updateDoc(doc(db, 'users', uid), {
        full_name: fullName,
        college_organization: collegeOrganization,
      });
    } catch (err) {
      console.error('Error updating profile in Firestore (offline sync will handle it):', err);
    }

    // Always update local React state and localStorage immediately for a seamless UI
    const updatedUser = {
      ...user,
      name: fullName,
      college: collegeOrganization,
    };

    localStorage.setItem('user_name', fullName);
    localStorage.setItem('user_college', collegeOrganization);
    setUser(updatedUser);

    return updatedUser;
  };

  // ─────────────────────────────────────────────────────────
  // Student Operations (remain on REST API)
  // ─────────────────────────────────────────────────────────
  const fetchStudents = async () => {
    if (!user) return;
    setLoading(prev => ({ ...prev, students: true }));
    try {
      const data = await api.getStudents(user.email);
      setStudents(data);
    } catch (e) {
      console.error('Error fetching students:', e);
    } finally {
      setLoading(prev => ({ ...prev, students: false }));
    }
  };

  const addStudent = async (name, regNo, branch, year, examType) => {
    if (!user) return;
    const newStudent = await api.addStudent(user.email, name, regNo, branch, year, examType);
    setStudents(prev => {
      const exists = prev.some(s => s.regNo === regNo);
      if (exists) {
        return prev.map(s => s.regNo === regNo ? newStudent : s);
      }
      return [...prev, newStudent];
    });
    await triggerSystemNotification('Student Added', `Student ${name} (${regNo}) has been registered.`);
    return newStudent;
  };

  const updateStudent = async (regNo, studentData) => {
    if (!user) return;
    const updated = await api.updateStudent(regNo, { ...studentData, userEmail: user.email });
    setStudents(prev => prev.map(s => s.regNo === regNo ? updated : s));
    return updated;
  };

  const deleteStudent = async (studentId, regNo) => {
    await api.deleteStudent(studentId);
    setStudents(prev => prev.filter(s => s.id !== studentId));
    await triggerSystemNotification('Student Deleted', `Student with Reg No ${regNo} was removed.`);
  };

  const bulkDeleteStudents = async (regNos) => {
    await api.deleteBulkStudents(regNos);
    setStudents(prev => prev.filter(s => !regNos.includes(s.regNo)));
    await triggerSystemNotification('Bulk Delete Successful', `Removed ${regNos.length} students.`);
  };

  // ─────────────────────────────────────────────────────────
  // Room Operations
  // ─────────────────────────────────────────────────────────
  const fetchRooms = async () => {
    if (!user) return;
    setLoading(prev => ({ ...prev, rooms: true }));
    try {
      const data = await api.getRooms(user.email);
      setRooms(data);
    } catch (e) {
      console.error('Error fetching rooms:', e);
    } finally {
      setLoading(prev => ({ ...prev, rooms: false }));
    }
  };

  const addRoom = async (roomNumber, capacity, building) => {
    if (!user) return;
    const newRoom = await api.addRoom(user.email, roomNumber, capacity, building);
    setRooms(prev => [...prev, newRoom]);
    await triggerSystemNotification('Room Added', `Room ${roomNumber} (Capacity: ${capacity}) created in ${building}.`);
    return newRoom;
  };

  const updateRoom = async (roomId, roomData) => {
    if (!user) return;
    const updated = await api.updateRoom(roomId, { ...roomData, userEmail: user.email });
    setRooms(prev => prev.map(r => r.id === roomId ? updated : r));
    return updated;
  };

  const deleteRoom = async (roomId, roomNumber) => {
    await api.deleteRoom(roomId);
    setRooms(prev => prev.filter(r => r.id !== roomId));
    await triggerSystemNotification('Room Removed', `Room ${roomNumber} was deleted.`);
  };

  // ─────────────────────────────────────────────────────────
  // Faculty Operations
  // ─────────────────────────────────────────────────────────
  const fetchFaculties = async () => {
    if (!user) return;
    setLoading(prev => ({ ...prev, faculties: true }));
    try {
      const data = await api.getFaculties(user.email);
      setFaculties(data);
    } catch (e) {
      console.error('Error fetching faculties:', e);
    } finally {
      setLoading(prev => ({ ...prev, faculties: false }));
    }
  };

  const addFaculty = async (facultyData) => {
    if (!user) return;
    await api.addFaculty({ ...facultyData, userEmail: user.email });
    await fetchFaculties();
    await triggerSystemNotification('Faculty Added', `${facultyData.name} has been added to faculties.`);
  };

  const deleteFaculty = async (facultyId, name) => {
    await api.deleteFaculty(facultyId);
    setFaculties(prev => prev.filter(f => f.id !== facultyId));
    await triggerSystemNotification('Faculty Removed', `${name} was removed from the system.`);
  };

  // ─────────────────────────────────────────────────────────
  // Notification Operations
  // ─────────────────────────────────────────────────────────
  const fetchNotifications = async () => {
    if (!user) return;
    setLoading(prev => ({ ...prev, notifications: true }));
    try {
      const data = await api.getNotifications(user.email);
      setNotifications(data);
    } catch (e) {
      console.error('Error fetching notifications:', e);
    } finally {
      setLoading(prev => ({ ...prev, notifications: false }));
    }
  };

  const triggerSystemNotification = async (title, message) => {
    if (!user) return;
    try {
      await api.addNotification(user.email, title, message);
      await fetchNotifications();
    } catch (e) {
      console.error('Error logging notification:', e);
    }
  };

  const deleteNotification = async (id) => {
    await api.deleteNotification(id);
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  // ─────────────────────────────────────────────────────────
  // Reports
  // ─────────────────────────────────────────────────────────
  const fetchReports = async () => {
    if (!user) return;
    setLoading(prev => ({ ...prev, reports: true }));
    try {
      const data = await api.getFinalReports(user.email);
      setSeatingPlans(data);
    } catch (e) {
      console.error('Error fetching seating plans:', e);
    } finally {
      setLoading(prev => ({ ...prev, reports: false }));
    }
  };

  // ─────────────────────────────────────────────────────────
  // Seating Plan Algorithm
  // ─────────────────────────────────────────────────────────
  const generateSeatingPlan = async (examType, examDate, examTime, roomCount, useAllStudents = false) => {
    if (rooms.length === 0) throw new Error('No rooms available in the system.');
    if (students.length === 0) throw new Error('No students available in the system.');

    const selectedRooms = rooms.slice(0, roomCount);
    let studentsList = useAllStudents
      ? [...students]
      : students.filter(s => s.examType && s.examType.toLowerCase() === examType.toLowerCase());

    if (studentsList.length === 0) {
      throw new Error(`No students found assigned to exam type: ${examType}`);
    }

    studentsList.sort((a, b) => a.regNo.localeCompare(b.regNo));

    const finalAllocations = [];
    let studentIndex = 0;

    const activeFaculty = faculties.filter(f => f.status === 'Active' || f.status === 'Confirmed');
    const uniqueRooms = selectedRooms.map(r => r.roomNumber);
    const facultyAssignments = {};

    uniqueRooms.forEach((roomNum, idx) => {
      facultyAssignments[roomNum] = idx < activeFaculty.length
        ? activeFaculty[idx].name
        : 'Not Assigned';
    });

    selectedRooms.forEach(room => {
      for (let seat = 1; seat <= room.capacity; seat++) {
        if (studentIndex < studentsList.length) {
          const student = studentsList[studentIndex];
          finalAllocations.push({
            studentName: student.name,
            regNo: student.regNo,
            branch: student.branch,
            year: Number(student.year),
            roomNumber: room.roomNumber,
            seatNo: seat,
            building: room.building,
            invigilator: facultyAssignments[room.roomNumber] || 'Not Assigned',
            subject: examType,
            date: examDate,
            time: examTime,
          });
          studentIndex++;
        } else {
          break;
        }
      }
    });

    if (finalAllocations.length === 0) throw new Error('No allocations could be made.');

    const plan = {
      examType,
      examDate,
      examTime,
      roomsUsed: selectedRooms.length,
      totalStudents: finalAllocations.length,
      allocations: finalAllocations,
    };

    setCurrentSeatingPlan(plan);
    return plan;
  };

  const saveSeatingPlanToBackend = async (plan) => {
    if (!user) return;
    try {
      await api.deleteFinalReports(user.email);
    } catch (_) {}

    const promises = plan.allocations.map(alloc =>
      api.addFinalReport({
        userEmail: user.email,
        studentName: alloc.studentName,
        regNo: alloc.regNo,
        branch: alloc.branch,
        seatNo: alloc.seatNo,
        roomNumber: alloc.roomNumber,
        building: alloc.building,
        invigilator: alloc.invigilator,
        subject: plan.examType,
        date: plan.examDate,
        time: plan.examTime,
      })
    );

    await Promise.all(promises);
    await triggerSystemNotification(
      'Seating Plan Saved',
      `Generated plan for ${plan.examType} has been sync'd to the cloud.`
    );
    await fetchReports();
  };

  // Don't render children until Firebase has resolved its initial auth state
  if (!authReady) return null;

  return (
    <AppContext.Provider value={{
      user,
      students,
      rooms,
      faculties,
      notifications,
      seatingPlans,
      currentSeatingPlan,
      setCurrentSeatingPlan,
      loading,
      theme,
      toggleTheme,
      loginUser,
      registerUser,
      logoutUser,
      forgotPassword,
      updateUserProfile,
      addStudent,
      updateStudent,
      deleteStudent,
      bulkDeleteStudents,
      addRoom,
      updateRoom,
      deleteRoom,
      addFaculty,
      deleteFaculty,
      deleteNotification,
      triggerSystemNotification,
      generateSeatingPlan,
      saveSeatingPlanToBackend,
      refreshAllData,
    }}>
      {children}
    </AppContext.Provider>
  );
};
