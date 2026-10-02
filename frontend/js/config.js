// ============================================================================
// SAMVAYA JAVASCRIPT CONFIGURATION & SESSION MANAGEMENT
// ============================================================================

const API_BASE_URL = (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'))
  ? 'http://localhost:8080/api'
  : 'https://samvaya-production.up.railway.app/api';

const CONFIG = {
  API_BASE_URL: API_BASE_URL,
  SOCIETY_NAME: 'Samvaya Luxury Enclave',
  AUTH_STORAGE_KEY: 'samvaya_auth_user',
  CURRENT_USER_KEY: 'currentUser',
  TOKEN_STORAGE_KEY: 'samvaya_auth_token'
};

// Global helper to safely resolve paths on Vercel, web servers, and local file testing
function getAppPath(targetPath) {
  if (!targetPath) return '/index.html';
  if (targetPath.startsWith('http://') || targetPath.startsWith('https://')) return targetPath;
  if (targetPath.startsWith('../') || targetPath.startsWith('./')) return targetPath;
  const cleanTarget = targetPath.startsWith('/') ? targetPath.slice(1) : targetPath;

  // If running from local file:// protocol
  if (typeof window !== 'undefined' && window.location.protocol === 'file:') {
    const currentPath = window.location.pathname.replace(/\\/g, '/');
    if (currentPath.includes('/pages/')) {
      return '../../' + cleanTarget;
    }
    return './' + cleanTarget;
  }

  // On web server / Vercel / Railway: root-relative path is always reliable and unambiguous
  return '/' + cleanTarget;
}

// Global Helper to get Current Authenticated User
function getCurrentUser() {
  try {
    const userJson = localStorage.getItem('currentUser') || localStorage.getItem(CONFIG.AUTH_STORAGE_KEY);
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    console.error('Error reading currentUser from localStorage:', e);
    return null;
  }
}

// Global Helper to check Role Access - Strict Session Enforced
function checkAuth(allowedRoles = []) {
  const user = getCurrentUser();
  
  // If no user found in local storage, immediately redirect to login gateway
  if (!user) {
    window.location.href = getAppPath('index.html');
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
        window.location.href = getAppPath('pages/resident/dashboard.html');
      } else if (userRole.includes('SECURITY')) {
        window.location.href = getAppPath('pages/security/dashboard.html');
      } else {
        window.location.href = getAppPath('pages/admin/dashboard.html');
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
  window.location.href = getAppPath('index.html');
}

// Attach to global window object
if (typeof window !== 'undefined') {
  window.API_BASE_URL = API_BASE_URL;
  window.CONFIG = CONFIG;
  window.getAppPath = getAppPath;
  window.getCurrentUser = getCurrentUser;
  window.checkAuth = checkAuth;
  window.logout = logout;
}

// Support CommonJS/Node environments if needed
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { API_BASE_URL, CONFIG, getAppPath, getCurrentUser, checkAuth, logout };
}
