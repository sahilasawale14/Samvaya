// ============================================================================
// SAMVAYA JAVASCRIPT CONFIGURATION & RESILIENT AUTHENTICATION
// ============================================================================

const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api',
  SOCIETY_NAME: 'Samvaya Luxury Enclave',
  AUTH_STORAGE_KEY: 'samvaya_auth_user',
  TOKEN_STORAGE_KEY: 'samvaya_auth_token'
};

// Global Helper to get Current Authenticated User
function getCurrentUser() {
  const userJson = localStorage.getItem(CONFIG.AUTH_STORAGE_KEY);
  try {
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    return null;
  }
}

// Global Helper to check Role Access
function checkAuth(allowedRoles = []) {
  let user = getCurrentUser();
  
  // If no user found in local storage, automatically initialize demo user for this portal
  if (!user) {
    let defaultRole = (allowedRoles.length > 0) ? allowedRoles[0] : 'ADMIN';
    let defaultName = 'Vikramaditya Singhania';
    let residentType = null;
    let flatNumber = '101';
    let wing = 'A';
    let resId = 1;

    if (defaultRole === 'RESIDENT') {
      residentType = 'OWNER';
    } else if (defaultRole === 'SECURITY_GUARD') {
      defaultName = 'Ramesh Kumar';
    }

    user = {
      userId: 1,
      username: defaultRole.toLowerCase(),
      fullName: defaultName,
      email: `${defaultRole.toLowerCase()}@samvaya.com`,
      role: defaultRole,
      residentType: residentType,
      residentId: resId,
      flatId: 1,
      flatNumber: flatNumber,
      wing: wing,
      token: 'DEMO-TOKEN'
    };
    localStorage.setItem(CONFIG.AUTH_STORAGE_KEY, JSON.stringify(user));
  }

  // If user role doesn't match allowed role, adjust gracefully for seamless previewing
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    user.role = allowedRoles[0];
    localStorage.setItem(CONFIG.AUTH_STORAGE_KEY, JSON.stringify(user));
  }

  return true;
}

// Global Logout
function logout() {
  localStorage.removeItem(CONFIG.AUTH_STORAGE_KEY);
  localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
  window.location.href = '/index.html';
}
