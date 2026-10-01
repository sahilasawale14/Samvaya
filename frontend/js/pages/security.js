// ============================================================================
// SECURITY GATE OPERATIONS CONTROLLER
// ============================================================================

const SecurityPage = {
  async initDashboard() {
    Sidebar.render('sidebar-container', 'dashboard');
    Navbar.render('navbar-container', 'Gate Operations & Live Security Command');

    try {
      const stats = await SecurityApi.getDashboard();

      document.getElementById('stat-visitors-today').innerText = stats.expectedVisitorsToday || 0;
      document.getElementById('stat-visitors-inside').innerText = stats.visitorsCurrentlyInside || 0;
      document.getElementById('stat-deliveries-pending').innerText = stats.deliveriesPendingAtGate || 0;
      document.getElementById('stat-workers-inside').innerText = stats.temporaryWorkersInside || 0;
      document.getElementById('stat-incidents-active').innerText = stats.activeIncidentsCount || 0;

      // Render Visitors Table
      Table.render('gate-visitors-table', [
        { label: 'Visitor Name', render: r => `<b>${r.visitorName}</b>` },
        { label: 'Phone', key: 'phone' },
        { label: 'Destination Unit', render: r => `<b>Wing ${r.wing}-${r.flatNumber}</b>` },
        { label: 'Host Resident', key: 'residentName' },
        { label: 'Pass Code', render: r => `<code style="font-weight:700; color:var(--primary);">${r.passCode || '-'}</code>` },
        { label: 'Status', render: r => `<span class="badge ${r.status === 'INSIDE' ? 'badge-success' : 'badge-warning'}">${r.status}</span>` },
        { label: 'Actions', render: r => `
          ${r.status === 'EXPECTED' ? `<button class="btn btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="SecurityPage.markVisitorArrival(${r.id})">Mark Arrived</button>` : ''}
          ${r.status === 'ARRIVED' ? `<button class="btn btn-primary" style="padding:4px 8px; font-size:11px;" onclick="SecurityPage.allowVisitorEntry(${r.id})">Allow Entry</button>` : ''}
          ${r.status === 'INSIDE' ? `<button class="btn btn-ghost" style="color:var(--danger); padding:4px 8px; font-size:11px;" onclick="SecurityPage.markVisitorExit(${r.id})">Log Exit</button>` : ''}
          ${r.status === 'EXITED' ? `<span style="color:var(--outline); font-size:12px;">Completed</span>` : ''}
        `}
      ], stats.recentGateVisitors || []);

      // Render Deliveries Table
      Table.render('gate-deliveries-table', [
        { label: 'Courier Company', render: r => `<b>${r.company}</b>` },
        { label: 'Delivery Boy', render: r => `${r.deliveryPersonName || 'Courier'} (${r.phone || '-'})` },
        { label: 'Destination', render: r => `Wing ${r.wing}-${r.flatNumber} (${r.residentName})` },
        { label: 'Reference / OTP', render: r => `<code>${r.referenceNumber || '-'}</code>` },
        { label: 'Status', render: r => `<span class="badge ${r.status === 'ARRIVED' ? 'badge-warning' : (r.status === 'COMPLETED' ? 'badge-success' : 'badge-info')}">${r.status}</span>` },
        { label: 'Actions', render: r => `
          ${r.status === 'EXPECTED' ? `<button class="btn btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="SecurityPage.markDeliveryArrival(${r.id})">At Gate</button>` : ''}
          ${r.status === 'ARRIVED' ? `<button class="btn btn-primary" style="padding:4px 8px; font-size:11px;" onclick="SecurityPage.completeDelivery(${r.id})">Handover</button>` : ''}
          ${r.status === 'COMPLETED' ? `<span style="color:var(--success); font-size:12px;">✓ Completed</span>` : ''}
        `}
      ], stats.recentGateDeliveries || []);

    } catch (e) {
      console.error(e);
    }
  },

  async markVisitorArrival(id) {
    await SecurityApi.recordVisitorArrival(id);
    Toast.info('Visitor arrival logged at gate. Resident notified.');
    this.initDashboard();
  },

  async allowVisitorEntry(id) {
    await SecurityApi.recordVisitorEntry(id, 'Main Gate 1', 'Gate ID verification complete');
    Toast.success('Visitor granted entry into society');
    this.initDashboard();
  },

  async markVisitorExit(id) {
    await SecurityApi.recordVisitorExit(id);
    Toast.success('Visitor departure logged');
    this.initDashboard();
  },

  async markDeliveryArrival(id) {
    await SecurityApi.recordDeliveryArrival(id);
    Toast.info('Delivery arrived at gate');
    this.initDashboard();
  },

  async completeDelivery(id) {
    await SecurityApi.completeDelivery(id);
    Toast.success('Delivery marked complete');
    this.initDashboard();
  },

  async handleLiveGateSearch(query) {
    if (!query || query.trim().length < 2) {
      this.initDashboard();
      return;
    }
    const results = await SecurityApi.search(query);
    if (results.visitors) {
      Table.render('gate-visitors-table', [
        { label: 'Visitor Name', render: r => `<b>${r.visitorName}</b>` },
        { label: 'Phone', key: 'phone' },
        { label: 'Destination Unit', render: r => `Wing ${r.flat ? r.flat.wing : ''}-${r.flat ? r.flat.flatNumber : ''}` },
        { label: 'Pass Code', render: r => `<code>${r.passCode || '-'}</code>` },
        { label: 'Status', render: r => `<span class="badge badge-info">${r.status}</span>` }
      ], results.visitors);
    }
  }
};
