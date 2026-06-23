# PlanMySeat

PlanMySeat is an **Automatic Exam Seating Allocation System** designed to streamline the complex process of organizing seating arrangements for examinations. It caters to educational institutions by providing an efficient, automated, and error-free way to allocate students to exam rooms while ensuring required constraints (like spacing, no two students from the same subject sitting next to each other, etc.) are met.

## Project Structure

This comprehensive solution is divided into three main components:

### 1. Web Application (`PlanMySeat web`)
A responsive administrative portal built with **React** and **Vite**. This portal is used by Principals and Faculties to manage the exam data.
- **Key Features:**
  - **Role-based Dashboards:** Dedicated dashboards for Principals and Faculties.
  - **Resource Management:** Interfaces to manage Students, Faculties, and Rooms.
  - **Seating Setup:** Tools to define constraints and initiate the seating allocation process.
  - **Plan Generation:** View, export, and publish the automatically generated seating plans.
  - **Notifications & Support:** Built-in alerts and support ticketing.

### 2. Backend Service (`PlanMySeat backend`)
The core engine powering the data processing and seating algorithm, built with **Python**.
- **Key Features:**
  - Handles API requests from the Web App and Android App.
  - Executes the complex seating arrangement algorithms based on inputs (number of students, room capacity, subject distribution).
  - Interacts with the database to securely store and retrieve institutional data.

### 3. Android App (`android app`)
A mobile application built to provide quick access to seating information for students and faculty members on the go.
- **Key Features:**
  - Easy-to-use mobile interface.
  - Students can check their assigned room and seat number instantly.
  - Faculties can view their invigilation duties and room assignments.

## Getting Started

### Prerequisites
- **Node.js** (v18 or higher) for the Web Application.
- **Python** (v3.9 or higher) for the Backend Service.
- **Android Studio** for running and building the Android App.

### Setup Instructions
1. **Web App:** Navigate to `PlanMySeat web/web-app`, run `npm install` to fetch dependencies, and use `npm run dev` to start the local development server.
2. **Backend:** Navigate to `PlanMySeat backend`, install required Python packages, and start the backend server (e.g., using `uvicorn main:app --reload` if it's FastAPI, or similar depending on the framework).
3. **Android App:** Open the `android app` directory in Android Studio, sync Gradle, and run the app on an emulator or a physical device.

## CI/CD
This project uses **GitHub Actions** for Continuous Integration. Every push and pull request to the `main` branch automatically triggers a build workflow to ensure the web application is compiling successfully.
