// ============================================================================
// RESIDENT PAGE CONTROLLER - REAL DB DATA INTEGRATED
// ============================================================================

const ResidentPage = {
  checkResidentAuth() {
    const user = typeof getCurrentUser === 'function' ? getCurrentUser() : null;
    if (!user || user.role !== 'RESIDENT') {
      const loginUrl = (typeof getAppPath === 'function') ? getAppPath('index.html') : '../../index.html';
      window.location.href = loginUrl;
      return null;
    }
    return user;
  },

  async initDashboard() {
    Sidebar.render('sidebar-container', 'dashboard');
    Navbar.render('navbar-container', 'Resident Home Services');

    const user = ResidentPage.checkResidentAuth();
    if (!user) return;
    const residentId = user.residentId || user.id || user.userId;

    try {
      const stats = await ResidentApi.getDashboard(residentId);

      const nameEl = document.getElementById('res-name-title');
      if (nameEl) {
        nameEl.innerText = `Welcome, ${user.fullName || stats.residentName || 'Resident'}!`;
      }

      const flat = user.flatNumber || stats.flatNumber || '101';
      const roleType = user.residentType || stats.residentType || 'OWNER';
      let unitText = '';
      if (flat.toLowerCase().startsWith('wing') || flat.includes('-')) {
        unitText = `${flat.replace(/^Wing\s*/i, 'Wing ')} • ${roleType}`;
      } else {
        const wing = user.wing || stats.wing || 'A';
        unitText = `Wing ${wing}-${flat} • ${roleType}`;
      }

      const badgeEl = document.getElementById('res-unit-badge');
      if (badgeEl) {
        badgeEl.innerHTML = `<span class="material-symbols-outlined" style="font-size:16px;">apartment</span><span>${unitText}</span>`;
      }

      const visCountEl = document.getElementById('stat-visitors-count');
      if (visCountEl) visCountEl.innerText = stats.expectedVisitorsCount || 0;
      const delCountEl = document.getElementById('stat-deliveries-count');
      if (delCountEl) delCountEl.innerText = stats.activeDeliveriesCount || 0;
      const compCountEl = document.getElementById('stat-complaints-count');
      if (compCountEl) compCountEl.innerText = stats.pendingComplaintsCount || 0;
      const duesCountEl = document.getElementById('stat-dues-amount');
      if (duesCountEl) duesCountEl.innerText = `₹${(stats.pendingMaintenanceAmount || 0).toLocaleString('en-IN')}`;

      // Render Visitors
      Table.render('upcoming-visitors-table', [
        { label: 'Visitor Name', key: 'visitorName' },
        { label: 'Purpose', key: 'purpose' },
        { label: 'Expected Date & Time', render: r => `${r.expectedDate} at ${r.expectedTime}` },
        { label: 'Pass Code', render: r => `<code style="background:var(--surface-container-low); padding:3px 8px; font-weight:700;">${r.passCode || '-'}</code>` },
        { label: 'Gate Status', render: r => `<span class="badge ${r.status === 'INSIDE' ? 'badge-success' : 'badge-warning'}">${r.status}</span>` }
      ], stats.upcomingVisitors || []);

      // Render Deliveries
      Table.render('active-deliveries-table', [
        { label: 'Company', render: r => `<b>${r.company}</b>` },
        { label: 'Delivery Person', render: r => `${r.deliveryPersonName || 'Courier'} (${r.phone || '-'})` },
        { label: 'Ref Number', key: 'referenceNumber' },
        { label: 'Status', render: r => `<span class="badge ${r.status === 'ARRIVED' ? 'badge-warning' : (r.status === 'COMPLETED' ? 'badge-success' : 'badge-info')}">${r.status}</span>` }
      ], stats.activeDeliveries || []);

    } catch (e) {
      console.error('Error loading resident dashboard:', e);
    }
  },

  async initVisitors() {
    Sidebar.render('sidebar-container', 'visitors');
    Navbar.render('navbar-container', 'My Expected Visitors & Guest Passes');

    const user = ResidentPage.checkResidentAuth();
    if (!user) return;
    const residentId = user.residentId || user.id || user.userId;

    const visitors = await ResidentApi.getVisitors(residentId);
    Table.render('visitors-data-table', [
      { label: 'Visitor Name', render: r => `<b>${r.visitorName}</b> (${r.numberOfVisitors} Guests)` },
      { label: 'Phone', key: 'phone' },
      { label: 'Purpose', key: 'purpose' },
      { label: 'Expected Schedule', render: r => `${r.expectedDate} @ ${r.expectedTime}` },
      { label: 'Vehicle Number', render: r => r.vehicleNumber || 'No vehicle' },
      { label: 'Pass Code', render: r => `<code style="font-size:14px; font-weight:700; color:var(--primary);">${r.passCode || '-'}</code>` },
      { label: 'Status', render: r => `<span class="badge ${r.status === 'INSIDE' ? 'badge-success' : (r.status === 'EXITED' ? 'badge-neutral' : 'badge-warning')}">${r.status}</span>` },
      { label: 'Approval', render: r => `
        ${r.approvalStatus === 'PENDING' ? `
          <button class="btn btn-primary" style="padding:4px 8px; font-size:11px;" onclick="ResidentPage.approveVisitor(${r.id}, true)">Approve</button>
          <button class="btn btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="ResidentPage.approveVisitor(${r.id}, false)">Deny</button>
        ` : `<span class="badge ${r.approvalStatus === 'APPROVED' ? 'badge-success' : 'badge-danger'}">${r.approvalStatus}</span>`}
      `}
    ], visitors);
  },

  async createVisitorPassSubmit(event) {
    if (event) event.preventDefault();

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || user.id || user.userId) : null;

    const visitorData = {
      residentId: residentId,
      flatId: user ? user.flatId : null,
      visitorName: document.getElementById('visitor-name').value,
      phone: document.getElementById('visitor-phone').value,
      purpose: document.getElementById('visitor-purpose').value,
      expectedDate: document.getElementById('visitor-date').value,
      expectedTime: document.getElementById('visitor-time').value,
      vehicleNumber: document.getElementById('visitor-vehicle').value,
      numberOfVisitors: parseInt(document.getElementById('visitor-count').value || 1)
    };

    try {
      await ResidentApi.createVisitorPass(visitorData);
      Toast.success('Visitor pre-approval pass generated!');
      Modal.close('new-visitor-modal');
      this.initVisitors();
    } catch (e) {
      Toast.error(e.message || 'Failed to create visitor pass');
    }
  },

  async approveVisitor(visitorId, approved) {
    await ResidentApi.approveVisitor(visitorId, approved);
    Toast.success(approved ? 'Visitor approved for entry' : 'Visitor entry denied');
    this.initVisitors();
  },

  async initComplaints() {
    Sidebar.render('sidebar-container', 'complaints');
    Navbar.render('navbar-container', 'My Maintenance Complaints');

    const user = ResidentPage.checkResidentAuth();
    if (!user) return;
    const residentId = user.residentId;
    const userId = user.id || user.userId;

    const complaints = await ResidentApi.getComplaints(residentId, userId);
    Table.render('resident-complaints-table', [
      { label: 'ID', render: r => `#CMP-${r.id}` },
      { label: 'Category', key: 'category' },
      { label: 'Title', render: r => `<b>${r.title}</b><br><small style="color:var(--outline);">${r.description}</small>` },
      { label: 'Priority', render: r => `<span class="badge ${r.priority === 'HIGH' ? 'badge-danger' : 'badge-warning'}">${r.priority}</span>` },
      { label: 'Status', render: r => `<span class="badge ${r.status === 'RESOLVED' ? 'badge-success' : 'badge-warning'}">${r.status}</span>` },
      { label: 'Assigned Staff', key: 'assignedStaffName' },
      { label: 'Admin Remarks', render: r => r.adminRemarks || 'Under review' }
    ], complaints);
  },

  async createComplaintSubmit(event) {
    if (event) event.preventDefault();

    const user = getCurrentUser();
    const residentId = user ? user.residentId : null;
    const userId = user ? (user.id || user.userId) : null;

    const complaintData = {
      residentId: residentId,
      userId: userId,
      flatId: user ? user.flatId : null,
      category: document.getElementById('comp-category').value,
      title: document.getElementById('comp-title').value,
      description: document.getElementById('comp-description').value,
      priority: document.getElementById('comp-priority').value
    };

    try {
      await ResidentApi.createComplaint(complaintData);
      Toast.success('Complaint submitted successfully!');
      Modal.close('new-complaint-modal');
      this.initComplaints();
    } catch (e) {
      Toast.error(e.message || 'Failed to submit complaint');
    }
  },

  async initAmenities() {
    Sidebar.render('sidebar-container', 'amenities');
    Navbar.render('navbar-container', 'Society Amenities & Facility Booking');

    const user = ResidentPage.checkResidentAuth();
    if (!user) return;
    const residentId = user.residentId || user.id || user.userId;

    const amenities = await ResidentApi.getAmenities();
    const bookings = await ResidentApi.getBookings(residentId);

    const cardsContainer = document.getElementById('amenities-cards-grid');
    if (cardsContainer) {
      cardsContainer.innerHTML = amenities.map(a => `
        <div class="card-section" style="display:flex; flex-direction:column; justify-content:space-between; margin-bottom:0;">
          <div>
            <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:12px;">
              <h3 style="font-size:18px;">${a.name}</h3>
              <span class="badge badge-success">Available</span>
            </div>
            <p style="font-size:14px; color:var(--on-surface-variant); margin-bottom:16px;">${a.description}</p>
            <div style="font-size:13px; color:var(--primary); font-weight:600; margin-bottom:6px;">
              <span class="material-symbols-outlined" style="font-size:16px;">schedule</span> Operating Hours: ${a.openTime} - ${a.closeTime}
            </div>
            <div style="font-size:13px; color:var(--primary); font-weight:600; margin-bottom:16px;">
              <span class="material-symbols-outlined" style="font-size:16px;">payments</span> Rate: ${a.hourlyRate > 0 ? `₹${a.hourlyRate}/hour` : 'Complimentary'}
            </div>
          </div>
          <button class="btn btn-primary" onclick="ResidentPage.openBookingModal(${a.id}, '${a.name}')">Book Facility</button>
        </div>
      `).join('');
    }

    Table.render('resident-bookings-table', [
      { label: 'Facility', key: 'amenityName' },
      { label: 'Date', key: 'bookingDate' },
      { label: 'Time Slot', render: r => `${r.startTime} - ${r.endTime}` },
      { label: 'Guests', key: 'numberOfGuests' },
      { label: 'Total Amount', render: r => r.totalAmount > 0 ? `₹${r.totalAmount}` : 'Free' },
      { label: 'Status', render: r => `<span class="badge ${r.status === 'CONFIRMED' ? 'badge-success' : 'badge-danger'}">${r.status}</span>` },
      { label: 'Action', render: r => r.status === 'CONFIRMED' ? `
        <button class="btn btn-ghost" style="color:var(--danger);" onclick="ResidentPage.cancelBooking(${r.id})">Cancel</button>
      ` : '-' }
    ], bookings);
  },

  openBookingModal(amenityId, amenityName) {
    document.getElementById('booking-amenity-id').value = amenityId;
    document.getElementById('booking-amenity-name').value = amenityName;
    Modal.open('amenity-booking-modal');
  },

  async submitAmenityBooking(event) {
    if (event) event.preventDefault();

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || user.id || user.userId) : null;

    const bookingData = {
      amenityId: document.getElementById('booking-amenity-id').value,
      residentId: residentId,
      bookingDate: document.getElementById('booking-date').value,
      startTime: document.getElementById('booking-start-time').value + ':00',
      endTime: document.getElementById('booking-end-time').value + ':00',
      numberOfGuests: parseInt(document.getElementById('booking-guests').value || 1)
    };

    try {
      await ResidentApi.bookAmenity(bookingData);
      Toast.success('Facility booked successfully!');
      Modal.close('amenity-booking-modal');
      this.initAmenities();
    } catch (e) {
      Toast.error(e.message || 'Failed to book amenity');
    }
  },

  async cancelBooking(bookingId) {
    if (confirm('Are you sure you want to cancel this booking?')) {
      await ResidentApi.cancelBooking(bookingId);
      Toast.success('Booking cancelled');
      this.initAmenities();
    }
  },

  async initPayments() {
    Sidebar.render('sidebar-container', 'payments');
    Navbar.render('navbar-container', 'Maintenance Bills & Online Payments');

    const user = ResidentPage.checkResidentAuth();
    if (!user) return;
    const flatId = user.flatId || null;
    const residentId = user.residentId || user.id || user.userId;

    const bills = await ResidentApi.getBills(flatId, residentId);
    window._residentBills = bills || [];

    Table.render('resident-bills-table', [
      { label: 'Billing Period', key: 'billMonth' },
      { label: 'Unit Type', render: r => `<span class="badge badge-info">${r.flatType || '2BHK'}</span>` },
      { label: 'Carpet Area', render: r => `${r.carpetAreaSqFt || 900} sq.ft` },
      { label: 'Fixed Fees', render: r => `₹${parseFloat(r.totalFixedCharges || 2500).toFixed(2)}` },
      { label: 'Area Charge', render: r => `₹${parseFloat(r.variableAreaCharge || r.maintenanceCharge).toFixed(2)}` },
      { label: 'Total Payable', render: r => `<b style="font-size:15px; color:var(--primary);">₹${r.totalAmount}</b>` },
      { label: 'Due Date', key: 'dueDate' },
      { label: 'Status', render: r => `<span class="badge ${r.status === 'PAID' ? 'badge-success' : 'badge-danger'}">${r.status}</span>` },
      { label: 'Actions', render: r => `
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="ResidentPage.viewReceipt(${r.id})">Receipt</button>
          ${r.status !== 'PAID' ? `
            <button class="btn btn-accent" style="padding:4px 10px; font-size:11px;" onclick="ResidentPage.payMaintenanceBill(${r.id})">Pay UPI</button>
          ` : `<span style="color:var(--success); font-weight:700; font-size:12px; align-self:center;">Paid</span>`}
        </div>
      `}
    ], bills);
  },

  viewReceipt(billId) {
    const bill = (window._residentBills || []).find(b => b.id === billId);
    if (!bill) return;

    const rate = bill.ratePerSqFt || 3.50;
    const carpet = bill.carpetAreaSqFt || 900.0;
    const sec = bill.securityCharge || 1000.00;
    const lift = bill.liftElectricityCharge || 800.00;
    const sink = bill.sinkingFund || 500.00;
    const admin = bill.administrativeFee || 200.00;
    const totalFixed = bill.totalFixedCharges || 2500.00;
    const variable = bill.variableAreaCharge || (rate * carpet);

    const html = `
      <div style="margin-bottom:16px;">
        <div style="font-size:16px; font-weight:700; color:var(--primary);">
          Wing ${bill.wing}-${bill.flatNumber} (${bill.flatType || '2BHK'})
        </div>
        <div style="font-size:13px; color:var(--on-surface-variant);">
          Resident: <b>${bill.residentName}</b> | Period: <b>${bill.billMonth}</b>
        </div>
      </div>

      <div class="itemized-receipt-box">
        <div style="font-size:12px; font-weight:700; color:var(--secondary); text-transform:uppercase; margin-bottom:8px;">
          1. Standard Fixed Charges (Identical Across All Society Flats)
        </div>
        <div class="receipt-row">
          <span>Security Personnel & Perimeter Tech</span>
          <span>₹${parseFloat(sec).toFixed(2)}</span>
        </div>
        <div class="receipt-row">
          <span>Lift Operations & Common Electricity</span>
          <span>₹${parseFloat(lift).toFixed(2)}</span>
        </div>
        <div class="receipt-row">
          <span>Sinking Fund Reserves</span>
          <span>₹${parseFloat(sink).toFixed(2)}</span>
        </div>
        <div class="receipt-row">
          <span>Administrative Society Fee</span>
          <span>₹${parseFloat(admin).toFixed(2)}</span>
        </div>
        <div class="receipt-row" style="font-weight:700; background:rgba(0,106,99,0.06); padding:6px 8px; border-radius:4px;">
          <span>Subtotal Fixed Charges</span>
          <span style="color:var(--secondary);">₹${parseFloat(totalFixed).toFixed(2)}</span>
        </div>

        <div style="font-size:12px; font-weight:700; color:var(--primary); text-transform:uppercase; margin-top:16px; margin-bottom:8px;">
          2. Variable Area Charge (Proportional to Carpet Area)
        </div>
        <div class="receipt-row">
          <span>Carpet Area: <b>${carpet} sq.ft</b> × Rate <b>₹${rate}/sq.ft</b></span>
          <span style="font-weight:700;">₹${parseFloat(variable).toFixed(2)}</span>
        </div>

        <div class="receipt-row total-row">
          <span>Total Maintenance Payable</span>
          <span>₹${parseFloat(bill.totalAmount).toFixed(2)}</span>
        </div>
      </div>

      <div style="margin-top:16px; font-size:12px; color:var(--outline); line-height:1.4;">
        Due Date: <b>${bill.dueDate}</b> • Status: <span class="badge ${bill.status === 'PAID' ? 'badge-success' : 'badge-danger'}">${bill.status}</span>
      </div>
    `;

    document.getElementById('resident-receipt-body').innerHTML = html;
    Modal.open('resident-receipt-modal');
  },

  async payMaintenanceBill(billId) {
    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

    if (confirm('Proceed with instant UPI payment simulation for this maintenance invoice?')) {
      await ResidentApi.payBill(billId, residentId, 'UPI');
      Toast.success('Maintenance bill paid successfully! Receipt generated.');
      this.initPayments();
    }
  }
};
