// ============================================================================
// DYNAMIC SIDEBAR COMPONENT (Admin, Resident, Security)
// Streamlined Modern Workspace Navigation
// ============================================================================

const Sidebar = {
  render(containerId, activePageKey) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const user = getCurrentUser() || { fullName: 'User', role: 'GUEST', username: 'guest' };
    const role = (user.role || 'GUEST').toUpperCase();
    const resolveLink = (p) => (typeof getAppPath === 'function') ? getAppPath(p) : '/' + (p.startsWith('/') ? p.slice(1) : p);

    let menuHtml = '';

    if (role === 'ADMIN') {
      menuHtml = `
        <div class="sidebar-menu-category">Overview</div>
        <a href="${resolveLink('pages/admin/dashboard.html')}" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </a>

        <div class="sidebar-menu-category">Unified Workspaces</div>
        <a href="${resolveLink('pages/admin/people-units.html')}" class="nav-item ${activePageKey === 'people-units' ? 'active' : ''}">
          <span class="material-symbols-outlined">groups</span>
          <span>People & Units</span>
        </a>
        <a href="${resolveLink('pages/admin/gate-security.html')}" class="nav-item ${activePageKey === 'gate-security' ? 'active' : ''}">
          <span class="material-symbols-outlined">shield_person</span>
          <span>Gate & Security</span>
        </a>
        <a href="${resolveLink('pages/admin/financials.html')}" class="nav-item ${activePageKey === 'financials' ? 'active' : ''}">
          <span class="material-symbols-outlined">receipt_long</span>
          <span>Financials</span>
        </a>
        <a href="${resolveLink('pages/admin/community-desk.html')}" class="nav-item ${activePageKey === 'community-desk' ? 'active' : ''}">
          <span class="material-symbols-outlined">forum</span>
          <span>Community & Desk</span>
        </a>
      `;
    } else if (role === 'RESIDENT') {
      menuHtml = `
        <div class="sidebar-menu-category">Overview</div>
        <a href="${resolveLink('pages/resident/dashboard.html')}" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">home</span>
          <span>My Home</span>
        </a>
        
        <div class="sidebar-menu-category">Gate & Services</div>
        <a href="${resolveLink('pages/resident/visitors.html')}" class="nav-item ${activePageKey === 'visitors' ? 'active' : ''}">
          <span class="material-symbols-outlined">person_pin_circle</span>
          <span>Visitor Passes</span>
        </a>
        <a href="${resolveLink('pages/resident/deliveries.html')}" class="nav-item ${activePageKey === 'deliveries' ? 'active' : ''}">
          <span class="material-symbols-outlined">package_2</span>
          <span>Expected Deliveries</span>
        </a>
        <a href="${resolveLink('pages/resident/payments.html')}" class="nav-item ${activePageKey === 'payments' ? 'active' : ''}">
          <span class="material-symbols-outlined">credit_card</span>
          <span>Maintenance Dues</span>
        </a>
        <a href="${resolveLink('pages/resident/complaints.html')}" class="nav-item ${activePageKey === 'complaints' ? 'active' : ''}">
          <span class="material-symbols-outlined">help_center</span>
          <span>Complaints & Desk</span>
        </a>
        <a href="${resolveLink('pages/resident/amenities.html')}" class="nav-item ${activePageKey === 'amenities' ? 'active' : ''}">
          <span class="material-symbols-outlined">sports_tennis</span>
          <span>Book Amenities</span>
        </a>
      `;
    } else if (role === 'SECURITY_GUARD' || role === 'SECURITY') {
      menuHtml = `
        <div class="sidebar-menu-category">Command & Control</div>
        <a href="${resolveLink('pages/security/dashboard.html')}" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">local_police</span>
          <span>Gate Command</span>
        </a>

        <div class="sidebar-menu-category">Gate Operations</div>
        <a href="${resolveLink('pages/security/visitors.html')}" class="nav-item ${activePageKey === 'visitors' ? 'active' : ''}">
          <span class="material-symbols-outlined">person_check</span>
          <span>Visitor Check-In</span>
        </a>
        <a href="${resolveLink('pages/security/deliveries.html')}" class="nav-item ${activePageKey === 'deliveries' ? 'active' : ''}">
          <span class="material-symbols-outlined">local_shipping</span>
          <span>Delivery Check-In</span>
        </a>
        <a href="${resolveLink('pages/security/vehicles.html')}" class="nav-item ${activePageKey === 'vehicles' ? 'active' : ''}">
          <span class="material-symbols-outlined">local_parking</span>
          <span>Parking & Vehicles</span>
        </a>
        <a href="${resolveLink('pages/security/incidents.html')}" class="nav-item ${activePageKey === 'incidents' ? 'active' : ''}">
          <span class="material-symbols-outlined">warning</span>
          <span>Report Incident</span>
        </a>
      `;
    }

    const initials = (user.fullName || 'User').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
    let unitBadge = '';
    if (role === 'RESIDENT' && user.flatNumber) {
      const flat = user.flatNumber;
      if (flat.toLowerCase().startsWith('wing') || flat.includes('-')) {
        unitBadge = flat.replace(/^Wing\s*/i, 'Wing ');
      } else {
        unitBadge = `Wing ${user.wing || 'A'}-${flat}`;
      }
    } else if (user.residentType) {
      unitBadge = user.residentType;
    } else if (role === 'ADMIN') {
      unitBadge = 'Society Admin';
    } else if (role.includes('SECURITY')) {
      unitBadge = 'Gate Command';
    } else {
      unitBadge = role.replace('_', ' ');
    }

    container.innerHTML = `
      <div class="sidebar-header">
        <div class="sidebar-logo-icon">S</div>
        <div style="flex:1;">
          <div class="sidebar-brand-name">SAMVAYA</div>
          <div class="sidebar-role-tag">${role.replace('_', ' ')} PORTAL</div>
        </div>
        <button class="sidebar-close-btn" onclick="Sidebar.closeMobile()" aria-label="Close navigation menu" title="Close">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      <div class="sidebar-menu">
        ${menuHtml}
      </div>
      <div class="sidebar-footer">
        <div class="user-profile-summary">
          <div class="avatar">${initials}</div>
          <div class="user-info-text">
            <div class="user-name" title="${user.fullName}">${user.fullName}</div>
            <div class="user-role">${unitBadge}</div>
          </div>
        </div>
        <button onclick="logout()" class="btn-ghost" title="Sign Out" style="padding:8px; border-radius:50%;">
          <span class="material-symbols-outlined" style="font-size:20px; color:#ffffff;">logout</span>
        </button>
      </div>
    `;

    // Ensure mobile drawer backdrop overlay exists
    let overlay = document.getElementById('sidebar-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'sidebar-overlay';
      overlay.className = 'sidebar-overlay';
      overlay.onclick = () => Sidebar.closeMobile();
      document.body.appendChild(overlay);
    }

    // Auto-close drawer on navigation tap
    container.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', () => Sidebar.closeMobile());
    });
  },

  toggleMobile() {
    const sidebar = document.querySelector('.app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) {
      const isOpen = sidebar.classList.toggle('open');
      if (overlay) overlay.classList.toggle('active', isOpen);
      document.body.classList.toggle('sidebar-open', isOpen);
    }
  },

  openMobile() {
    const sidebar = document.querySelector('.app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) {
      sidebar.classList.add('open');
      if (overlay) overlay.classList.add('active');
      document.body.classList.add('sidebar-open');
    }
  },

  closeMobile() {
    const sidebar = document.querySelector('.app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) {
      sidebar.classList.remove('open');
      if (overlay) overlay.classList.remove('active');
      document.body.classList.remove('sidebar-open');
    }
  }
};
