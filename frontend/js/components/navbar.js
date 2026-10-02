// ============================================================================
// TOPBAR & NAVBAR COMPONENT
// ============================================================================

const Navbar = {
  render(containerId, title = 'Overview') {
    const container = document.getElementById(containerId);
    if (!container) return;

    const user = getCurrentUser() || {};
    let unitInfo = '';
    const userRole = (user.role || '').toUpperCase();
    if (userRole === 'RESIDENT' && user.flatNumber) {
      const flat = user.flatNumber;
      if (flat.toLowerCase().startsWith('wing') || flat.includes('-')) {
        unitInfo = `(${flat.replace(/^Wing\s*/i, 'Wing ')})`;
      } else {
        unitInfo = `(Wing ${user.wing || 'A'}-${flat})`;
      }
    } else if (userRole === 'ADMIN') {
      unitInfo = '(Admin)';
    } else if (userRole.includes('SECURITY')) {
      unitInfo = '(Gate Command)';
    } else if (user.role) {
      unitInfo = `(${user.role.replace('_', ' ')})`;
    }

    container.innerHTML = `
      <div class="topbar-left">
        <button class="mobile-menu-btn" onclick="Sidebar.toggleMobile()" aria-label="Toggle navigation menu" title="Menu">
          <span class="material-symbols-outlined">menu</span>
        </button>
        <h1 class="topbar-title">${title}</h1>
      </div>
      <div class="topbar-actions">
        ${user.fullName ? `
          <div class="topbar-user-pill" style="display:inline-flex; align-items:center; gap:8px; padding:6px 12px; background:var(--surface-container-low); border-radius:20px; font-size:13px; font-weight:600; color:var(--on-surface);">
            <span class="material-symbols-outlined" style="font-size:18px; color:var(--primary);">account_circle</span>
            <span>${user.fullName} <span style="font-weight:400; color:var(--outline);">${unitInfo}</span></span>
          </div>
        ` : ''}
        <div class="global-search-container">
          <span class="material-symbols-outlined search-icon">search</span>
          <input type="text" id="global-search-input" placeholder="Search residents, units, visitors..." onkeyup="if(event.key==='Enter')handleGlobalSearch(this.value)">
        </div>
        <button class="action-icon-btn" title="Emergency Alerts" onclick="alert('Emergency Hotline: Gate 1 (+91 97111 99887) | Control Room (+91 98200 11223)')">
          <span class="material-symbols-outlined" style="color:var(--tertiary);">emergency</span>
        </button>
        <button class="action-icon-btn" title="Notifications" onclick="toggleNotifications()">
          <span class="material-symbols-outlined">notifications</span>
          <span class="notification-badge"></span>
        </button>
      </div>
    `;
  }
};

function handleGlobalSearch(q) {
  if (!q || !q.trim()) return;
  const user = getCurrentUser();
  if (user && user.role === 'SECURITY_GUARD') {
    window.location.href = `/pages/security/dashboard.html?q=${encodeURIComponent(q)}`;
  } else {
    Toast.info(`Searching society records for "${q}"...`);
  }
}

function toggleNotifications() {
  Toast.info("You have 3 unread society notices & visitor notifications.");
}
