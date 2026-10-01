// ============================================================================
// LOGIN CONTROLLER (Immediate, Resilient Multi-Role Navigation)
// ============================================================================

let currentSelectedRole = 'admin';

function selectRole(role) {
  currentSelectedRole = role;
  const roleCardContainer = document.getElementById('role-selection');
  const loginContainer = document.getElementById('login-container');
  
  if (roleCardContainer) {
    roleCardContainer.style.display = 'none';
  }
  
  if (loginContainer) {
    loginContainer.style.display = 'block';
    loginContainer.style.opacity = '1';
    loginContainer.style.transform = 'translateY(0)';
  }

  // Pre-fill test credentials for quick demo access
  const usernameInput = document.getElementById('userId');
  const passwordInput = document.getElementById('password');
  const titleElem = document.getElementById('login-title');
  const subElem = document.getElementById('login-subtitle');

  if (role === 'admin') {
    if (titleElem) titleElem.innerText = 'Admin Sign In';
    if (subElem) subElem.innerText = 'Society management & governance portal';
    if (usernameInput) usernameInput.value = 'admin';
    if (passwordInput) passwordInput.value = 'admin123';
  } else if (role === 'resident') {
    if (titleElem) titleElem.innerText = 'Resident Sign In';
    if (subElem) subElem.innerText = 'Owner & Tenant home services portal';
    if (usernameInput) { usernameInput.value = ''; usernameInput.placeholder = 'Enter resident username'; }
    if (passwordInput) { passwordInput.value = ''; passwordInput.placeholder = 'Enter resident password'; }
  } else if (role === 'security') {
    if (titleElem) titleElem.innerText = 'Security Gate Sign In';
    if (subElem) subElem.innerText = 'Gate operations & visitor entry portal';
    if (usernameInput) usernameInput.value = 'guard1';
    if (passwordInput) passwordInput.value = 'guard123';
  }
}

function showRoleSelection() {
  const roleCardContainer = document.getElementById('role-selection');
  const loginContainer = document.getElementById('login-container');
  
  if (loginContainer) {
    loginContainer.style.display = 'none';
  }
  
  if (roleCardContainer) {
    roleCardContainer.style.display = 'grid';
  }
}

async function handleLoginSubmit(event) {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }

  const usernameInput = document.getElementById('userId') || document.getElementById('username');
  const passwordInput = document.getElementById('password');

  const username = usernameInput ? usernameInput.value.trim() : '';
  const password = passwordInput ? passwordInput.value : '';

  if (!username || !password) {
    Toast.error('Please enter both username and password.');
    return;
  }

  Toast.info('Authenticating credentials...');

  try {
    const user = await AuthApi.login(username, password);

    // Save session data to localStorage as requested
    localStorage.setItem('currentUser', JSON.stringify(user));
    localStorage.setItem(CONFIG.AUTH_STORAGE_KEY, JSON.stringify(user));

    Toast.success(`Welcome, ${user.fullName}! Entering portal...`);

    const roleUpper = (user.role || '').toUpperCase();
    let targetUrl = '/pages/admin/dashboard.html';

    if (roleUpper === 'RESIDENT') {
      targetUrl = '/pages/resident/dashboard.html';
    } else if (roleUpper === 'SECURITY' || roleUpper === 'SECURITY_GUARD') {
      targetUrl = '/pages/security/dashboard.html';
    } else if (roleUpper === 'ADMIN') {
      targetUrl = '/pages/admin/dashboard.html';
    }

    setTimeout(() => {
      window.location.href = targetUrl;
    }, 250);

  } catch (err) {
    console.error('Login error:', err);
    Toast.error(err.message || 'Invalid username or password. Please try again.');
  }
}
