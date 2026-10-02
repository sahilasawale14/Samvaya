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

// Global Helper to get Current Authenticated User (Tab-Isolated SessionStorage)
function getCurrentUser() {
  try {
    const userJson = sessionStorage.getItem('currentUser') || sessionStorage.getItem(CONFIG.AUTH_STORAGE_KEY);
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    console.error('Error reading currentUser from sessionStorage:', e);
    return null;
  }
}

// Strict Single Portal Route Guard Utility
function enforcePortalGuard(requiredRole) {
  try {
    const userJson = sessionStorage.getItem('currentUser') || sessionStorage.getItem(CONFIG.AUTH_STORAGE_KEY);
    const user = userJson ? JSON.parse(userJson) : null;
    const loginUrl = (typeof getAppPath === 'function') ? getAppPath('index.html') : '../../index.html';

    if (!user || !user.role) {
      window.location.replace(loginUrl);
      return false;
    }

    const role = (user.role || '').toUpperCase();
    const targetRole = (requiredRole || '').toUpperCase();

    if (targetRole === 'ADMIN' && role !== 'ADMIN') {
      window.location.replace(loginUrl);
      return false;
    }
    if (targetRole === 'RESIDENT' && role !== 'RESIDENT') {
      window.location.replace(loginUrl);
      return false;
    }
    if ((targetRole === 'SECURITY' || targetRole === 'SECURITY_GUARD') && (role !== 'SECURITY' && role !== 'SECURITY_GUARD')) {
      window.location.replace(loginUrl);
      return false;
    }

    return true;
  } catch (e) {
    console.error('Portal guard validation failed:', e);
    const loginUrl = (typeof getAppPath === 'function') ? getAppPath('index.html') : '../../index.html';
    window.location.replace(loginUrl);
    return false;
  }
}

// Global Helper to check Role Access - Strict Session Enforced (No Cross-Portal Leakage)
function checkAuth(allowedRoles = []) {
  const user = getCurrentUser();
  const loginUrl = (typeof getAppPath === 'function') ? getAppPath('index.html') : '../../index.html';
  
  // If no user found in session storage, immediately redirect to login gateway
  if (!user || !user.role) {
    window.location.replace(loginUrl);
    return false;
  }

  // If user role doesn't match allowed role, redirect strictly back to login page
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = (user.role || '').toUpperCase();
    const isAllowed = allowedRoles.some(r => {
      const norm = r.toUpperCase();
      return norm === userRole ||
             (norm === 'SECURITY' && (userRole === 'SECURITY_GUARD' || userRole === 'SECURITY')) ||
             (norm === 'SECURITY_GUARD' && (userRole === 'SECURITY_GUARD' || userRole === 'SECURITY'));
    });

    if (!isAllowed) {
      // Under no circumstance redirect across portals. Redirect strictly to login page.
      window.location.replace(loginUrl);
      return false;
    }
  }

  return true;
}

// Global Logout - Completely Clear SessionStorage and Redirect to Login Gateway
function logout() {
  try {
    sessionStorage.clear();
  } catch (e) {}
  try {
    localStorage.removeItem('currentUser');
    localStorage.removeItem(CONFIG.AUTH_STORAGE_KEY);
    localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
  } catch (e) {}

  const loginUrl = (typeof getAppPath === 'function') ? getAppPath('index.html') : '../../index.html';
  window.location.replace(loginUrl);
}

// Attach to global window object
if (typeof window !== 'undefined') {
  window.API_BASE_URL = API_BASE_URL;
  window.CONFIG = CONFIG;
  window.getAppPath = getAppPath;
  window.getCurrentUser = getCurrentUser;
  window.checkAuth = checkAuth;
  window.enforcePortalGuard = enforcePortalGuard;
  window.logout = logout;
}

// Support CommonJS/Node environments if needed
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { API_BASE_URL, CONFIG, getAppPath, getCurrentUser, checkAuth, logout };
}
