// Your Firebase project settings. These are safe to be public:
// the database security rules are what protect each budget.
// Replace this whole object with the "firebaseConfig" from your Firebase web app.
window.FIREBASE_CONFIG = {
  apiKey: "PASTE-HERE",
  authDomain: "PASTE-HERE",
  databaseURL: "PASTE-HERE",
  projectId: "PASTE-HERE",
  appId: "PASTE-HERE"
};
if (!String(window.FIREBASE_CONFIG.apiKey).startsWith("PASTE")) {
  firebase.initializeApp(window.FIREBASE_CONFIG);
}
