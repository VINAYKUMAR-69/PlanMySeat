// Firebase SDK imports
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { initializeFirestore, persistentLocalCache, persistentMultipleTabManager } from "firebase/firestore";
import { getStorage } from "firebase/storage";

// ─────────────────────────────────────────────────────────
// Firebase project configuration for PlanMySeat
// Project: automaticexamseattin-7867fc75
// ─────────────────────────────────────────────────────────
const firebaseConfig = {
  apiKey: "AIzaSyCVNleD3loE7t22vAWHAHZ-DgTibNFtcxQ",
  authDomain: "automaticexamseattin-7867fc75.firebaseapp.com",
  projectId: "automaticexamseattin-7867fc75",
  storageBucket: "automaticexamseattin-7867fc75.firebasestorage.app",
  messagingSenderId: "597324872367",
  appId: "1:597324872367:web:eed0196784b56fe5e7b25a",
};

// Initialize Firebase application
const app = initializeApp(firebaseConfig);

// ─────────────────────────────────────────────────────────
// Exported Firebase service instances
// ─────────────────────────────────────────────────────────

/** Firebase Authentication – use for sign-in, sign-up, password reset, etc. */
export const auth = getAuth(app);

/** Cloud Firestore – initialized with offline persistence caching using IndexedDB */
export const db = initializeFirestore(app, {
  localCache: persistentLocalCache({
    tabManager: persistentMultipleTabManager()
  })
});

/** Firebase Storage – use for file uploads (screenshots, documents, etc.) */
export const storage = getStorage(app);

/** The root Firebase app instance (rarely needed directly) */
export default app;
