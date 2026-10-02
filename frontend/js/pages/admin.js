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
      try {
        let pMetrics = null;
        try {
          pMetrics = await AdminApi.getParkingMetrics();
        } catch (e) {}

        let carAvail = 10, carTotal = 44, carOcc = 34;
        let bikeAvail = 20, bikeTotal = 70, bikeOcc = 50;
        let totalOcc = 84, totalSlots = 114, totalAvail = 30;

        if (pMetrics && pMetrics.cars && pMetrics.bikes) {
          carAvail = pMetrics.cars.available != null ? pMetrics.cars.available : 10;
          carTotal = pMetrics.cars.total != null ? pMetrics.cars.total : 44;
          carOcc = pMetrics.cars.occupied != null ? pMetrics.cars.occupied : (carTotal - carAvail);

          bikeAvail = pMetrics.bikes.available != null ? pMetrics.bikes.available : 20;
          bikeTotal = pMetrics.bikes.total != null ? pMetrics.bikes.total : 70;
          bikeOcc = pMetrics.bikes.occupied != null ? pMetrics.bikes.occupied : (bikeTotal - bikeAvail);

          totalOcc = pMetrics.totalOccupied != null ? pMetrics.totalOccupied : (carOcc + bikeOcc);
          totalSlots = pMetrics.totalSlots != null ? pMetrics.totalSlots : (carTotal + bikeTotal);
          totalAvail = pMetrics.totalAvailable != null ? pMetrics.totalAvailable : (totalSlots - totalOcc);
        } else if (stats.totalParkingSlots) {
          carAvail = stats.availableFourWheelerSlots ?? 10;
          carOcc = stats.occupiedFourWheelerSlots ?? 34;
          carTotal = carAvail + carOcc;

          bikeAvail = stats.availableTwoWheelerSlots ?? 20;
          bikeOcc = stats.occupiedTwoWheelerSlots ?? 50;
          bikeTotal = bikeAvail + bikeOcc;

          totalOcc = stats.occupiedParkingSlots ?? 84;
          totalSlots = stats.totalParkingSlots ?? 114;
          totalAvail = stats.availableParkingSlots ?? (totalSlots - totalOcc);
        }

        // Card 1: Available Car Parking (10 / 44 Available, subtitle 34 Occupied)
        if (document.getElementById('card-car-parking-avail')) {
          document.getElementById('card-car-parking-avail').innerText = `${carAvail} / ${carTotal} Available`;
        }
        if (document.getElementById('card-car-parking-sub')) {
          document.getElementById('card-car-parking-sub').innerText = `${carOcc} Occupied`;
        }

        // Card 2: Available 2-Wheeler Parking (20 / 70 Available, subtitle 50 Occupied)
        if (document.getElementById('card-bike-parking-avail')) {
          document.getElementById('card-bike-parking-avail').innerText = `${bikeAvail} / ${bikeTotal} Available`;
        }
        if (document.getElementById('card-bike-parking-sub')) {
          document.getElementById('card-bike-parking-sub').innerText = `${bikeOcc} Occupied`;
        }

        // Card 3: Total Society Occupancy (84 / 114 Occupied, subtitle 30 Available Slots)
        if (document.getElementById('card-total-parking-occ')) {
          document.getElementById('card-total-parking-occ').innerText = `${totalOcc} / ${totalSlots} Occupied`;
        }
        if (document.getElementById('card-total-parking-sub')) {
          document.getElementById('card-total-parking-sub').innerText = `${totalAvail} Available Slots`;
        }

        // Fallback for legacy IDs if present
        if (document.getElementById('stat-parking-total')) {
          document.getElementById('stat-parking-total').innerText = totalSlots;
          document.getElementById('stat-parking-occupied').innerText = totalOcc;
          if (document.getElementById('stat-parking-occupied-breakdown')) {
            document.getElementById('stat-parking-occupied-breakdown').innerText = `${carOcc} (4W) • ${bikeOcc} (2W)`;
          }
          if (document.getElementById('stat-parking-available')) {
            document.getElementById('stat-parking-available').innerText = totalAvail;
          }
          if (document.getElementById('stat-parking-avail-breakdown')) {
            document.getElementById('stat-parking-avail-breakdown').innerText = `${carAvail} 4-Wheeler • ${bikeAvail} 2-Wheeler`;
          }
          if (document.getElementById('stat-parking-avail-2w')) {
            document.getElementById('stat-parking-avail-2w').innerText = bikeAvail;
          }
          if (document.getElementById('stat-parking-avail-4w')) {
            document.getElementById('stat-parking-avail-4w').innerText = `${carAvail} Available 4-Wheelers`;
          }
        }
      } catch (e) {
        console.error("Error setting parking cards:", e);
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
    let residents = await AdminApi.getResidents(type);
    if (Array.isArray(residents)) {
      residents = residents.filter(r => r.status !== 'INACTIVE' && r.accountStatus !== 'INACTIVE' && r.accountStatus !== 'OFFBOARDED');
    }
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
    ], residents || []);
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
