// ============================================================================
// RESIDENT PAGE CONTROLLER
// ============================================================================

const ResidentPage = {
  async initDashboard() {
    Sidebar.render('sidebar-container', 'dashboard');
    Navbar.render('navbar-container', 'Resident Home Services');

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

    try {
      const stats = await ResidentApi.getDashboard(residentId);

      document.getElementById('res-name-title').innerText = `Welcome, ${stats.residentName || 'Resident'}!`;
      document.getElementById('res-unit-badge').innerText = `Wing ${stats.wing || 'A'}-${stats.flatNumber || '101'} • ${stats.residentType || 'OWNER'}`;

      document.getElementById('stat-visitors-count').innerText = stats.expectedVisitorsCount || 0;
      document.getElementById('stat-deliveries-count').innerText = stats.activeDeliveriesCount || 0;
      document.getElementById('stat-complaints-count').innerText = stats.pendingComplaintsCount || 0;
      document.getElementById('stat-dues-amount').innerText = `₹${(stats.pendingMaintenanceAmount || 0).toLocaleString('en-IN')}`;

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
      console.error(e);
    }
  },

  async initVisitors() {
    Sidebar.render('sidebar-container', 'visitors');
    Navbar.render('navbar-container', 'My Expected Visitors & Guest Passes');

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

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
    const residentId = user ? (user.residentId || 1) : 1;

    const visitorData = {
      residentId: residentId,
      visitorName: document.getElementById('visitor-name').value,
      phone: document.getElementById('visitor-phone').value,
      purpose: document.getElementById('visitor-purpose').value,
      expectedDate: document.getElementById('visitor-date').value,
      expectedTime: document.getElementById('visitor-time').value,
      vehicleNumber: document.getElementById('visitor-vehicle').value,
      numberOfVisitors: parseInt(document.getElementById('visitor-count').value || 1)
    };

    await ResidentApi.createVisitorPass(visitorData);
    Toast.success('Visitor pre-approval pass generated!');
    Modal.close('new-visitor-modal');
    this.initVisitors();
  },

  async approveVisitor(visitorId, approved) {
    await ResidentApi.approveVisitor(visitorId, approved);
    Toast.success(approved ? 'Visitor approved for entry' : 'Visitor entry denied');
    this.initVisitors();
  },

  async initComplaints() {
    Sidebar.render('sidebar-container', 'complaints');
    Navbar.render('navbar-container', 'My Maintenance Complaints');

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

    const complaints = await ResidentApi.getComplaints(residentId);
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
    const residentId = user ? (user.residentId || 1) : 1;

    const complaintData = {
      residentId: residentId,
      category: document.getElementById('comp-category').value,
      title: document.getElementById('comp-title').value,
      description: document.getElementById('comp-description').value,
      priority: document.getElementById('comp-priority').value
    };

    await ResidentApi.createComplaint(complaintData);
    Toast.success('Complaint submitted successfully!');
    Modal.close('new-complaint-modal');
    this.initComplaints();
  },

  async initAmenities() {
    Sidebar.render('sidebar-container', 'amenities');
    Navbar.render('navbar-container', 'Society Amenities & Facility Booking');

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

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
    const residentId = user ? (user.residentId || 1) : 1;

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
    } catch (e) {}
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

    const user = getCurrentUser();
    const residentId = user ? (user.residentId || 1) : 1;

    const bills = await ResidentApi.getBills(residentId);
    Table.render('resident-bills-table', [
      { label: 'Bill Month', key: 'billMonth' },
      { label: 'Maintenance Fee', render: r => `₹${r.maintenanceCharge}` },
      { label: 'Water & Parking', render: r => `₹${(parseFloat(r.waterCharge || 0) + parseFloat(r.parkingCharge || 0))}` },
      { label: 'Total Amount', render: r => `<b>₹${r.totalAmount}</b>` },
      { label: 'Due Date', key: 'dueDate' },
      { label: 'Status', render: r => `<span class="badge ${r.status === 'PAID' ? 'badge-success' : 'badge-danger'}">${r.status}</span>` },
      { label: 'Action', render: r => r.status !== 'PAID' ? `
        <button class="btn btn-accent" style="padding:4px 12px; font-size:12px;" onclick="ResidentPage.payMaintenanceBill(${r.id})">Pay Now</button>
      ` : `<span style="color:var(--success); font-weight:700; font-size:13px;">✓ Paid</span>` }
    ], bills);
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
