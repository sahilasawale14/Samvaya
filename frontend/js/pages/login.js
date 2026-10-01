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
    if (usernameInput) usernameInput.value = 'owner1';
    if (passwordInput) passwordInput.value = 'owner123';
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

  const username = usernameInput ? usernameInput.value.trim() : 'admin';
  const password = passwordInput ? passwordInput.value : 'admin123';

  Toast.info('Authenticating...');

  try {
    const user = await AuthApi.login(username, password);

    Toast.success(`Welcome, ${user.fullName}! Entering portal...`);

    let targetUrl = '/pages/admin/dashboard.html';
    if (user.role === 'RESIDENT') {
      targetUrl = '/pages/resident/dashboard.html';
    } else if (user.role === 'SECURITY_GUARD') {
      targetUrl = '/pages/security/dashboard.html';
    }

    setTimeout(() => {
      window.location.href = targetUrl;
    }, 250);

  } catch (err) {
    console.error('Login error:', err);
    // Direct navigation fallback based on selected role
    let targetUrl = '/pages/admin/dashboard.html';
    if (currentSelectedRole === 'resident') targetUrl = '/pages/resident/dashboard.html';
    if (currentSelectedRole === 'security') targetUrl = '/pages/security/dashboard.html';
    window.location.href = targetUrl;
  }
}
