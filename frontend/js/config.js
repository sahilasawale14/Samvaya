// ============================================================================
// SAMVAYA JAVASCRIPT CONFIGURATION & SESSION MANAGEMENT
// ============================================================================

const API_BASE_URL = (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'))
  ? 'http://localhost:8080/api'
  : 'https://YOUR-BACKEND.up.railway.app/api';

const CONFIG = {
  API_BASE_URL: API_BASE_URL,
  SOCIETY_NAME: 'Samvaya Luxury Enclave',
  AUTH_STORAGE_KEY: 'samvaya_auth_user',
  CURRENT_USER_KEY: 'currentUser',
  TOKEN_STORAGE_KEY: 'samvaya_auth_token'
};

// Global Helper to get Current Authenticated User
function getCurrentUser() {
  const userJson = localStorage.getItem('currentUser') || localStorage.getItem(CONFIG.AUTH_STORAGE_KEY);
  try {
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    return null;
  }
}

// Global Helper to check Role Access - Strict DB Session Enforced
function checkAuth(allowedRoles = []) {
  const user = getCurrentUser();
  
  // If no user found in local storage, immediately redirect to login gateway
  if (!user) {
    window.location.href = '/index.html';
    return false;
  }

  // If user role doesn't match allowed role, redirect strictly to their home portal
  if (allowedRoles.length > 0) {
    const userRole = (user.role || '').toUpperCase();
    const isAllowed = allowedRoles.some(r => {
      const norm = r.toUpperCase();
      return norm === userRole ||
             (norm === 'SECURITY' && (userRole === 'SECURITY_GUARD' || userRole === 'SECURITY')) ||
             (norm === 'SECURITY_GUARD' && (userRole === 'SECURITY_GUARD' || userRole === 'SECURITY'));
    });

    if (!isAllowed) {
      if (userRole === 'RESIDENT') {
        window.location.href = '/pages/resident/dashboard.html';
      } else if (userRole.includes('SECURITY')) {
        window.location.href = '/pages/security/dashboard.html';
      } else {
        window.location.href = '/pages/admin/dashboard.html';
      }
      return false;
    }
  }

  return true;
}

// Global Logout
function logout() {
  localStorage.removeItem('currentUser');
  localStorage.removeItem(CONFIG.AUTH_STORAGE_KEY);
  localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
  window.location.href = '/index.html';
}

// Attach to global window object
if (typeof window !== 'undefined') {
  window.API_BASE_URL = API_BASE_URL;
  window.CONFIG = CONFIG;
  window.getCurrentUser = getCurrentUser;
  window.checkAuth = checkAuth;
  window.logout = logout;
}

// Support CommonJS/Node environments if needed
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { API_BASE_URL, CONFIG, getCurrentUser, checkAuth, logout };
}
