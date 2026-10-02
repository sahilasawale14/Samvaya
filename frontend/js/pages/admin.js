// ============================================================================
// ADMIN PAGE CONTROLLER
// ============================================================================

const AdminPage = {
  async initDashboard() {
    Sidebar.render('sidebar-container', 'dashboard');
    Navbar.render('navbar-container', 'Society Overview & Analytics');

    try {
      const stats = await AdminApi.getDashboard();
      
      // Update UI numbers
      document.getElementById('stat-total-flats').innerText = stats.totalFlats || 0;
      document.getElementById('stat-occupied-flats').innerText = `${stats.occupiedFlats || 0} Occupied / ${stats.vacantFlats || 0} Vacant`;
      document.getElementById('stat-total-residents').innerText = stats.totalResidents || 0;
      document.getElementById('stat-owners-tenants').innerText = `${stats.totalOwners || 0} Owners • ${stats.totalTenants || 0} Tenants`;
      document.getElementById('stat-pending-complaints').innerText = stats.pendingComplaints || 0;
      document.getElementById('stat-maint-collected').innerText = `₹${(stats.collectedMaintenance || 0).toLocaleString('en-IN')}`;

      // Update Parking Metrics
      if (document.getElementById('stat-parking-total')) {
        document.getElementById('stat-parking-total').innerText = stats.totalParkingSlots || 0;
        document.getElementById('stat-parking-occupied').innerText = stats.occupiedParkingSlots || 0;
        document.getElementById('stat-parking-occupied-breakdown').innerText = 
          `${stats.occupiedFourWheelerSlots || 0} (4W) • ${stats.occupiedTwoWheelerSlots || 0} (2W)`;
        document.getElementById('stat-parking-available').innerText = stats.availableParkingSlots || 0;
        document.getElementById('stat-parking-avail-breakdown').innerText = 
          `${stats.availableFourWheelerSlots || 0} 4-Wheeler • ${stats.availableTwoWheelerSlots || 0} 2-Wheeler`;
        document.getElementById('stat-parking-avail-2w').innerText = stats.availableTwoWheelerSlots || 0;
        document.getElementById('stat-parking-avail-4w').innerText = `${stats.availableFourWheelerSlots || 0} Available 4-Wheelers`;
      }

      // Render Recent Activity Logs
      const activityContainer = document.getElementById('activity-feed-list');
      if (activityContainer && stats.recentActivities) {
        activityContainer.innerHTML = stats.recentActivities.map(act => `
          <div class="activity-item">
            <div class="activity-icon">
              <span class="material-symbols-outlined" style="font-size:18px;">bolt</span>
            </div>
            <div class="activity-content">
              <div class="activity-title">${act.description}</div>
              <div class="activity-time">${act.adminName} • ${act.timestamp}</div>
            </div>
          </div>
        `).join('');
      }

      // Render Recent Complaints
      Table.render('recent-complaints-table', [
        { label: 'Flat', render: r => `<b>Wing ${r.wing}-${r.flatNumber}</b>` },
        { label: 'Category', key: 'category' },
        { label: 'Title', key: 'title' },
        { label: 'Priority', render: r => `<span class="badge ${r.priority === 'HIGH' ? 'badge-danger' : 'badge-warning'}">${r.priority}</span>` },
        { label: 'Status', render: r => `<span class="badge ${r.status === 'NEW' ? 'badge-info' : (r.status === 'IN_PROGRESS' ? 'badge-warning' : 'badge-success')}">${r.status}</span>` },
        { label: 'Assigned Staff', key: 'assignedStaffName' }
      ], stats.recentComplaints || []);

    } catch (e) {
      console.error(e);
    }
  },

  async initResidents() {
    Sidebar.render('sidebar-container', 'residents');
    Navbar.render('navbar-container', 'Resident Management');
    this.loadResidentsTable();
  },

  async loadResidentsTable(type = '') {
    const residents = await AdminApi.getResidents(type);
    Table.render('residents-data-table', [
      { label: 'Full Name', render: r => `<b>${r.fullName}</b>` },
      { label: 'Type', render: r => `<span class="badge ${r.residentType === 'OWNER' ? 'badge-info' : 'badge-neutral'}">${r.residentType}</span>` },
      { label: 'Flat', render: r => r.flatNumber ? `Wing ${r.wing || ''}-${r.flatNumber} (${r.bhkType || ''})` : '<span style="color:var(--on-surface-variant);">-</span>' },
      { label: 'Contact', render: r => `${r.phone}<br><small style="color:var(--outline);">${r.email}</small>` },
      { label: 'Emergency Contact', render: r => `${r.emergencyContactName || '-'}<br><small>${r.emergencyContactPhone || ''}</small>` },
      { label: 'Move In Date', key: 'moveInDate' },
      { 
        label: 'Account Status', 
        render: r => {
          const isInactive = r.status === 'INACTIVE' || r.accountStatus === 'INACTIVE' || r.accountStatus === 'OFFBOARDED';
          return isInactive
            ? `<span class="badge" style="background:#fee2e2; color:#991b1b; border:1px solid #f87171;">INACTIVE (Moved Out)</span>`
            : `<span class="badge badge-success">ACTIVE</span>`;
        }
      }
    ], residents);
  },

  async initFlats() {
    Sidebar.render('sidebar-container', 'flats');
    Navbar.render('navbar-container', 'Flats & Units Directory');
    const flats = await AdminApi.getFlats();
    Table.render('flats-data-table', [
      { label: 'Wing', key: 'wing' },
      { label: 'Flat No.', render: r => `<b>${r.flatNumber}</b>` },
      { label: 'Floor', key: 'floorNumber' },
      { label: 'Unit Type', render: r => `<span class="badge badge-info">${r.flatType || r.bhkType}</span>` },
      { label: 'Carpet Area', render: r => `<b>${r.carpetAreaSqFt || r.squareFeet} sq.ft</b>` },
      { 
        label: 'Occupancy Status', 
        render: r => {
          const occ = r.occupancyStatus || (r.status === 'VACANT' ? 'VACANT' : (r.residentType === 'TENANT' ? 'OCCUPIED_TENANT' : 'OCCUPIED_OWNER'));
          if (occ === 'OCCUPIED_OWNER') {
            return `<span class="badge" style="background:#059669; color:#fff; font-weight:600;">Occupied (Owner)</span>`;
          } else if (occ === 'OCCUPIED_TENANT') {
            return `<span class="badge" style="background:#2563eb; color:#fff; font-weight:600;">Occupied (Tenant)</span>`;
          } else {
            return `<span class="badge" style="background:#d97706; color:#fff; font-weight:600;">Vacant</span>`;
          }
        }
      },
      { label: 'Current Resident', render: r => r.currentResidentName && r.currentResidentName !== 'None' ? `<b>${r.currentResidentName}</b>` : '<span style="color:var(--on-surface-variant); font-style:italic;">None</span>' }
    ], flats);
  },

  async initComplaints() {
    Sidebar.render('sidebar-container', 'complaints');
    Navbar.render('navbar-container', 'Society Complaints & Redressal');
    const complaints = await AdminApi.getComplaints();
    Table.render('complaints-data-table', [
      { label: 'ID', render: r => `#CMP-${r.id}` },
      { label: 'Unit', render: r => `<b>Wing ${r.wing}-${r.flatNumber}</b>` },
      { label: 'Complainant', key: 'residentName' },
      { label: 'Category', key: 'category' },
      { label: 'Title & Issue', render: r => `<b>${r.title}</b><br><small style="color:var(--on-surface-variant);">${r.description}</small>` },
      { label: 'Priority', render: r => `<span class="badge ${r.priority === 'HIGH' ? 'badge-danger' : 'badge-warning'}">${r.priority}</span>` },
      { label: 'Status', render: r => {
        if (r.status === 'RESOLVED' || r.status === 'CLOSED') {
          return `<span class="badge badge-success">${r.status}</span>`;
        } else if (r.status === 'IN_PROGRESS' || r.status === 'ASSIGNED') {
          return `<span class="badge badge-warning">${r.status}</span>`;
        }
        return `<span class="badge badge-info">${r.status}</span>`;
      }},
      { label: 'Assigned Staff', key: 'assignedStaffName' },
      { label: 'Actions', render: r => `
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary" style="padding:4px 10px; font-size:12px;" onclick="AdminPage.openAssignModal(${r.id})">Update</button>
          <button class="btn btn-ghost" style="color:var(--danger); padding:4px 8px; font-size:12px;" onclick="AdminPage.deleteComplaint(${r.id})" title="Delete ticket">
            <span class="material-symbols-outlined" style="font-size:16px;">delete</span>
          </button>
        </div>
      `}
    ], complaints);
  },

  openAssignModal(id) {
    document.getElementById('complaint-id-input').value = id;
    Modal.open('update-complaint-modal');
  },

  async submitComplaintUpdate() {
    const id = document.getElementById('complaint-id-input').value;
    const status = document.getElementById('complaint-status-select').value;
    const staffId = document.getElementById('complaint-staff-select').value;
    const remarks = document.getElementById('complaint-remarks-input').value;

    await AdminApi.updateComplaintStatus(id, status, staffId, remarks);
    Toast.success('Complaint status updated successfully');
    Modal.close('update-complaint-modal');
    this.initComplaints();
  },

  deleteComplaint(id) {
    if (window.Modal && typeof window.Modal.confirm === 'function') {
      window.Modal.confirm({
        title: 'Delete Complaint Ticket?',
        message: `Ticket #CMP-${id} and its resolution history will be permanently deleted from the database.`,
        confirmText: 'Delete Ticket',
        danger: true,
        onConfirm: async () => {
          try {
            await AdminApi.deleteComplaint(id);
            Toast.success('Complaint ticket deleted from database!');
            AdminPage.initComplaints();
          } catch (err) {
            Toast.error('Failed to delete complaint');
          }
        }
      });
    } else {
      if (confirm(`Are you sure you want to delete complaint ticket #CMP-${id}?`)) {
        AdminApi.deleteComplaint(id).then(() => {
          Toast.success('Complaint ticket deleted from database!');
          AdminPage.initComplaints();
        });
      }
    }
  }
};
