const BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  ? 'http://localhost:8000'
  : 'https://planmyseat.onrender.com';

const toCamel = (obj) => {
  if (Array.isArray(obj)) {
    return obj.map(v => toCamel(v));
  } else if (obj !== null && obj !== undefined && obj.constructor === Object) {
    return Object.keys(obj).reduce((result, key) => {
      const camelKey = key.replace(/([-_][a-z])/ig, ($1) => {
        return $1.toUpperCase().replace('-', '').replace('_', '');
      });
      result[camelKey] = toCamel(obj[key]);
      return result;
    }, {});
  }
  return obj;
};

const handleResponse = async (response) => {
  if (!response.ok) {
    let errorMessage = 'Network error occurred';
    try {
      const errBody = await response.json();
      errorMessage = errBody.message || errorMessage;
    } catch (_) {}
    throw new Error(errorMessage);
  }
  const data = await response.json();
  return toCamel(data);
};

export const api = {
  // Auth
  login: async (email, password) => {
    const res = await fetch(`${BASE_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    return handleResponse(res);
  },

  register: async (fullName, email, password, role, collegeOrganization) => {
    const res = await fetch(`${BASE_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        full_name: fullName,
        email,
        password,
        role,
        college_organization: collegeOrganization
      }),
    });
    return handleResponse(res);
  },

  // Password Recovery
  forgotPassword: async (email) => {
    const res = await fetch(`${BASE_URL}/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    return handleResponse(res);
  },

  verifyOtp: async (email, otp) => {
    const res = await fetch(`${BASE_URL}/verify-otp`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, otp }),
    });
    return handleResponse(res);
  },

  resetPassword: async (email, newPassword) => {
    const res = await fetch(`${BASE_URL}/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, new_password: newPassword }),
    });
    return handleResponse(res);
  },

  // Profiles
  getProfile: async (email) => {
    const res = await fetch(`${BASE_URL}/profile/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  updateProfile: async (email, fullName, collegeOrganization) => {
    const res = await fetch(`${BASE_URL}/profile/${encodeURIComponent(email)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        full_name: fullName,
        college_organization: collegeOrganization
      }),
    });
    return handleResponse(res);
  },

  changePassword: async (email, oldPassword, newPassword) => {
    const res = await fetch(`${BASE_URL}/change-password/${encodeURIComponent(email)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        old_password: oldPassword,
        new_password: newPassword
      }),
    });
    return handleResponse(res);
  },

  // Students
  getStudents: async (email) => {
    const res = await fetch(`${BASE_URL}/students/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  addStudent: async (userEmail, name, regNo, branch, year, examType) => {
    const res = await fetch(`${BASE_URL}/students`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: userEmail,
        name,
        reg_no: regNo,
        branch,
        year: String(year),
        exam_type: examType
      }),
    });
    return handleResponse(res);
  },

  updateStudent: async (regNo, studentData) => {
    const res = await fetch(`${BASE_URL}/students/${encodeURIComponent(regNo)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: studentData.userEmail,
        name: studentData.name,
        reg_no: regNo,
        branch: studentData.branch,
        year: String(studentData.year),
        exam_type: studentData.examType
      }),
    });
    return handleResponse(res);
  },

  deleteStudent: async (studentId) => {
    const res = await fetch(`${BASE_URL}/students/${studentId}`, {
      method: 'DELETE'
    });
    return handleResponse(res);
  },

  deleteBulkStudents: async (regNos) => {
    const res = await fetch(`${BASE_URL}/students`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(regNos)
    });
    return handleResponse(res);
  },

  // Rooms
  getRooms: async (email) => {
    const res = await fetch(`${BASE_URL}/rooms/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  addRoom: async (userEmail, roomNumber, capacity, building) => {
    const res = await fetch(`${BASE_URL}/rooms`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: userEmail,
        room_number: roomNumber,
        capacity: Number(capacity),
        building
      }),
    });
    return handleResponse(res);
  },

  updateRoom: async (roomId, roomData) => {
    const res = await fetch(`${BASE_URL}/rooms/${roomId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: roomData.userEmail,
        room_number: roomData.roomNumber,
        capacity: Number(roomData.capacity),
        building: roomData.building
      }),
    });
    return handleResponse(res);
  },

  deleteRoom: async (roomId) => {
    const res = await fetch(`${BASE_URL}/rooms/${roomId}`, {
      method: 'DELETE'
    });
    return handleResponse(res);
  },

  // Invigilators / Faculties
  getFaculties: async (email) => {
    const res = await fetch(`${BASE_URL}/faculties/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  addFaculty: async (facultyData) => {
    const res = await fetch(`${BASE_URL}/faculties`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: facultyData.userEmail,
        faculty_id: facultyData.facultyId,
        name: facultyData.name,
        designation: facultyData.designation,
        department: facultyData.department,
        phone: facultyData.phone,
        experience: String(facultyData.experience),
        papers: String(facultyData.papers || 0),
        rating: String(facultyData.rating || "0.0"),
        status: facultyData.status
      }),
    });
    return handleResponse(res);
  },

  deleteFaculty: async (facultyId) => {
    const res = await fetch(`${BASE_URL}/faculties/${facultyId}`, {
      method: 'DELETE'
    });
    return handleResponse(res);
  },

  // Seating plan / reports
  getFinalReports: async (email) => {
    const res = await fetch(`${BASE_URL}/final-reports/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  addFinalReport: async (reportData) => {
    const res = await fetch(`${BASE_URL}/final-reports`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: reportData.userEmail,
        student_name: reportData.studentName,
        reg_no: reportData.regNo,
        branch: reportData.branch,
        seat_no: Number(reportData.seatNo),
        room_number: reportData.roomNumber,
        building: reportData.building,
        invigilator: reportData.invigilator,
        subject: reportData.subject,
        date: reportData.date,
        time: reportData.time
      }),
    });
    return handleResponse(res);
  },

  deleteFinalReports: async (email) => {
    const res = await fetch(`${BASE_URL}/final-reports/${encodeURIComponent(email)}`, {
      method: 'DELETE'
    });
    return handleResponse(res);
  },

  // Notifications
  getNotifications: async (email) => {
    const res = await fetch(`${BASE_URL}/notifications/${encodeURIComponent(email)}`);
    return handleResponse(res);
  },

  addNotification: async (userEmail, title, message, sender = 'System') => {
    const today = new Date();
    const dateStr = today.toISOString().split('T')[0];
    const timeStr = today.toTimeString().split(' ')[0];
    const res = await fetch(`${BASE_URL}/notifications`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        user_email: userEmail,
        title,
        message,
        date: dateStr,
        time: timeStr,
        sender
      }),
    });
    return handleResponse(res);
  },

  deleteNotification: async (notificationId) => {
    const res = await fetch(`${BASE_URL}/notifications/${notificationId}`, {
      method: 'DELETE'
    });
    return handleResponse(res);
  },

  // Support
  submitFeedback: async (feedbackData) => {
    const res = await fetch(`${BASE_URL}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: feedbackData.name,
        email: feedbackData.email,
        feedback_type: feedbackData.feedbackType,
        rating: Number(feedbackData.rating),
        message: feedbackData.message,
        type: feedbackData.type || "",
        title: feedbackData.title || "",
        description: feedbackData.description || "",
        priority: feedbackData.priority || ""
      }),
    });
    return handleResponse(res);
  },

  submitBugReport: async (bugData) => {
    // Uses standard fetch multipart boundary auto-generation
    const formData = new FormData();
    formData.append('bug_title', bugData.title);
    formData.append('severity', bugData.severity);
    formData.append('frequency', bugData.frequency);
    formData.append('description', bugData.description);
    formData.append('steps_to_reproduce', bugData.stepsToReproduce);
    formData.append('expected_behavior', bugData.expectedBehavior);
    formData.append('actual_behavior', bugData.actualBehavior);
    if (bugData.screenshot) {
      formData.append('screenshot', bugData.screenshot);
    }

    const res = await fetch(`${BASE_URL}/bug-report`, {
      method: 'POST',
      body: formData,
    });
    return handleResponse(res);
  },

  submitFeatureRequest: async (featureData) => {
    const res = await fetch(`${BASE_URL}/feature-request`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        feature_title: featureData.title,
        description: featureData.description,
        category: featureData.category,
        priority: featureData.priority,
        use_case: featureData.useCase,
        expected_benefit: featureData.expectedBenefit
      }),
    });
    return handleResponse(res);
  }
};
