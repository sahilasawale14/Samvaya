// ============================================================================
// DYNAMIC SIDEBAR COMPONENT (Admin, Resident, Security)
// ============================================================================

const Sidebar = {
  render(containerId, activePageKey) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const user = getCurrentUser() || { fullName: 'User', role: 'ADMIN', username: 'admin' };
    const role = (user.role || 'ADMIN').toUpperCase();

    let menuHtml = '';

    if (role === 'ADMIN') {
      menuHtml = `
        <div class="sidebar-menu-category">Main</div>
        <a href="/pages/admin/dashboard.html" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </a>
        <a href="/pages/admin/residents.html" class="nav-item ${activePageKey === 'residents' ? 'active' : ''}">
          <span class="material-symbols-outlined">groups</span>
          <span>Residents</span>
        </a>
        <a href="/pages/admin/owners.html" class="nav-item ${activePageKey === 'owners' ? 'active' : ''}">
          <span class="material-symbols-outlined">badge</span>
          <span>Owners</span>
        </a>
        <a href="/pages/admin/flats.html" class="nav-item ${activePageKey === 'flats' ? 'active' : ''}">
          <span class="material-symbols-outlined">apartment</span>
          <span>Flats</span>
        </a>
        <a href="/pages/admin/users.html" class="nav-item ${activePageKey === 'users' ? 'active' : ''}">
          <span class="material-symbols-outlined">manage_accounts</span>
          <span>User Management</span>
        </a>

        <div class="sidebar-menu-category">Finance & Services</div>
        <a href="/pages/admin/payments.html" class="nav-item ${activePageKey === 'payments' ? 'active' : ''}">
          <span class="material-symbols-outlined">receipt_long</span>
          <span>Maintenance & Bills</span>
        </a>
        <a href="/pages/admin/complaints.html" class="nav-item ${activePageKey === 'complaints' ? 'active' : ''}">
          <span class="material-symbols-outlined">report_problem</span>
          <span>Complaints</span>
        </a>
        <a href="/pages/admin/amenities.html" class="nav-item ${activePageKey === 'amenities' ? 'active' : ''}">
          <span class="material-symbols-outlined">pool</span>
          <span>Amenities</span>
        </a>

        <div class="sidebar-menu-category">Communication & Staff</div>
        <a href="/pages/admin/notices.html" class="nav-item ${activePageKey === 'notices' ? 'active' : ''}">
          <span class="material-symbols-outlined">campaign</span>
          <span>Notices</span>
        </a>
        <a href="/pages/admin/documents.html" class="nav-item ${activePageKey === 'documents' ? 'active' : ''}">
          <span class="material-symbols-outlined">folder</span>
          <span>Documents</span>
        </a>
        <a href="/pages/admin/staff.html" class="nav-item ${activePageKey === 'staff' ? 'active' : ''}">
          <span class="material-symbols-outlined">engineering</span>
          <span>Staff Management</span>
        </a>
        <a href="/pages/admin/security-overview.html" class="nav-item ${activePageKey === 'security-overview' ? 'active' : ''}">
          <span class="material-symbols-outlined">shield_person</span>
          <span>Security Overview</span>
        </a>

        <div class="sidebar-menu-category">System</div>
        <a href="/pages/admin/reports.html" class="nav-item ${activePageKey === 'reports' ? 'active' : ''}">
          <span class="material-symbols-outlined">analytics</span>
          <span>Reports</span>
        </a>
        <a href="/pages/admin/activity-logs.html" class="nav-item ${activePageKey === 'activity-logs' ? 'active' : ''}">
          <span class="material-symbols-outlined">history</span>
          <span>Activity Logs</span>
        </a>
        <a href="/pages/admin/settings.html" class="nav-item ${activePageKey === 'settings' ? 'active' : ''}">
          <span class="material-symbols-outlined">settings</span>
          <span>Settings</span>
        </a>
      `;
    } else if (role === 'RESIDENT') {
      menuHtml = `
        <div class="sidebar-menu-category">Home & Community</div>
        <a href="/pages/resident/dashboard.html" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">home</span>
          <span>My Home</span>
        </a>
        <a href="/pages/resident/profile.html" class="nav-item ${activePageKey === 'profile' ? 'active' : ''}">
          <span class="material-symbols-outlined">person</span>
          <span>Profile & Household</span>
        </a>
        <a href="/pages/resident/visitors.html" class="nav-item ${activePageKey === 'visitors' ? 'active' : ''}">
          <span class="material-symbols-outlined">person_pin_circle</span>
          <span>Visitor Passes</span>
        </a>
        <a href="/pages/resident/deliveries.html" class="nav-item ${activePageKey === 'deliveries' ? 'active' : ''}">
          <span class="material-symbols-outlined">package_2</span>
          <span>Expected Deliveries</span>
        </a>

        <div class="sidebar-menu-category">Services & Finance</div>
        <a href="/pages/resident/domestic-staff.html" class="nav-item ${activePageKey === 'domestic-staff' ? 'active' : ''}">
          <span class="material-symbols-outlined">cleaning_services</span>
          <span>Domestic Staff</span>
        </a>
        <a href="/pages/resident/vehicles.html" class="nav-item ${activePageKey === 'vehicles' ? 'active' : ''}">
          <span class="material-symbols-outlined">directions_car</span>
          <span>Vehicles & Parking</span>
        </a>
        <a href="/pages/resident/complaints.html" class="nav-item ${activePageKey === 'complaints' ? 'active' : ''}">
          <span class="material-symbols-outlined">help_center</span>
          <span>Complaints</span>
        </a>
        <a href="/pages/resident/service-requests.html" class="nav-item ${activePageKey === 'service-requests' ? 'active' : ''}">
          <span class="material-symbols-outlined">home_repair_service</span>
          <span>Service Requests</span>
        </a>
        <a href="/pages/resident/payments.html" class="nav-item ${activePageKey === 'payments' ? 'active' : ''}">
          <span class="material-symbols-outlined">credit_card</span>
          <span>Maintenance Dues</span>
        </a>
        <a href="/pages/resident/amenities.html" class="nav-item ${activePageKey === 'amenities' ? 'active' : ''}">
          <span class="material-symbols-outlined">sports_tennis</span>
          <span>Book Amenities</span>
        </a>

        <div class="sidebar-menu-category">Updates</div>
        <a href="/pages/resident/notices.html" class="nav-item ${activePageKey === 'notices' ? 'active' : ''}">
          <span class="material-symbols-outlined">campaign</span>
          <span>Notices</span>
        </a>
        <a href="/pages/resident/documents.html" class="nav-item ${activePageKey === 'documents' ? 'active' : ''}">
          <span class="material-symbols-outlined">description</span>
          <span>Society Documents</span>
        </a>
        <a href="/pages/resident/notifications.html" class="nav-item ${activePageKey === 'notifications' ? 'active' : ''}">
          <span class="material-symbols-outlined">notifications</span>
          <span>Notifications</span>
        </a>
      `;
    } else if (role === 'SECURITY_GUARD') {
      menuHtml = `
        <div class="sidebar-menu-category">Gate Operations</div>
        <a href="/pages/security/dashboard.html" class="nav-item ${activePageKey === 'dashboard' ? 'active' : ''}">
          <span class="material-symbols-outlined">local_police</span>
          <span>Gate Command</span>
        </a>
        <a href="/pages/security/visitors.html" class="nav-item ${activePageKey === 'visitors' ? 'active' : ''}">
          <span class="material-symbols-outlined">person_check</span>
          <span>Visitor Check-In</span>
        </a>
        <a href="/pages/security/deliveries.html" class="nav-item ${activePageKey === 'deliveries' ? 'active' : ''}">
          <span class="material-symbols-outlined">local_shipping</span>
          <span>Delivery Check-In</span>
        </a>
        <a href="/pages/security/workers.html" class="nav-item ${activePageKey === 'workers' ? 'active' : ''}">
          <span class="material-symbols-outlined">handyman</span>
          <span>Temporary Workers</span>
        </a>
        <a href="/pages/security/vehicles.html" class="nav-item ${activePageKey === 'vehicles' ? 'active' : ''}">
          <span class="material-symbols-outlined">directions_car</span>
          <span>Vehicle Verification</span>
        </a>

        <div class="sidebar-menu-category">Logs & Incidents</div>
        <a href="/pages/security/live-activity.html" class="nav-item ${activePageKey === 'live-activity' ? 'active' : ''}">
          <span class="material-symbols-outlined">stream</span>
          <span>Live Gate Activity</span>
        </a>
        <a href="/pages/security/incidents.html" class="nav-item ${activePageKey === 'incidents' ? 'active' : ''}">
          <span class="material-symbols-outlined">warning</span>
          <span>Report Incident</span>
        </a>
        <a href="/pages/security/history.html" class="nav-item ${activePageKey === 'history' ? 'active' : ''}">
          <span class="material-symbols-outlined">history</span>
          <span>Security History</span>
        </a>
      `;
    }

    const initials = (user.fullName || 'U').split(' ').map(n => n[0]).join('').toUpperCase();

    container.innerHTML = `
      <div class="sidebar-header">
        <div class="sidebar-logo-icon">S</div>
        <div>
          <div class="sidebar-brand-name">SAMVAYA</div>
          <div class="sidebar-role-tag">${role.replace('_', ' ')} PORTAL</div>
        </div>
      </div>
      <div class="sidebar-menu">
        ${menuHtml}
      </div>
      <div class="sidebar-footer">
        <div class="user-profile-summary">
          <div class="avatar">${initials}</div>
          <div class="user-info-text">
            <div class="user-name" title="${user.fullName}">${user.fullName}</div>
            <div class="user-role">${user.residentType ? `${user.residentType} • ` : ''}${user.role}</div>
          </div>
        </div>
        <button onclick="logout()" class="btn-ghost" title="Sign Out" style="padding:8px; border-radius:50%;">
          <span class="material-symbols-outlined" style="font-size:20px; color:#ffffff;">logout</span>
        </button>
      </div>
    `;
  }
};
