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

      // Dynamic Parking Metrics for Security Command
      try {
        let pMetrics = null;
        try {
          pMetrics = await SecurityApi.getParkingMetrics();
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
        if (document.getElementById('sec-car-parking-avail')) {
          document.getElementById('sec-car-parking-avail').innerText = `${carAvail} / ${carTotal} Available`;
        }
        if (document.getElementById('sec-car-parking-sub')) {
          document.getElementById('sec-car-parking-sub').innerText = `${carOcc} Occupied`;
        }

        // Card 2: Available 2-Wheeler Parking (20 / 70 Available, subtitle 50 Occupied)
        if (document.getElementById('sec-bike-parking-avail')) {
          document.getElementById('sec-bike-parking-avail').innerText = `${bikeAvail} / ${bikeTotal} Available`;
        }
        if (document.getElementById('sec-bike-parking-sub')) {
          document.getElementById('sec-bike-parking-sub').innerText = `${bikeOcc} Occupied`;
        }

        // Card 3: Total Society Occupancy (84 / 114 Occupied, subtitle 30 Available Slots)
        if (document.getElementById('sec-total-parking-occ')) {
          document.getElementById('sec-total-parking-occ').innerText = `${totalOcc} / ${totalSlots} Occupied`;
        }
        if (document.getElementById('sec-total-parking-sub')) {
          document.getElementById('sec-total-parking-sub').innerText = `${totalAvail} Available Slots`;
        }

        // Legacy fallbacks if present
        if (document.getElementById('sec-parking-total')) {
          document.getElementById('sec-parking-total').innerText = totalSlots;
          document.getElementById('sec-parking-occupied').innerText = totalOcc;
          if (document.getElementById('sec-parking-occupied-breakdown')) {
            document.getElementById('sec-parking-occupied-breakdown').innerText = `${carOcc} (4W) • ${bikeOcc} (2W)`;
          }
          if (document.getElementById('sec-parking-available')) {
            document.getElementById('sec-parking-available').innerText = totalAvail;
          }
          if (document.getElementById('sec-parking-avail-breakdown')) {
            document.getElementById('sec-parking-avail-breakdown').innerText = `${carAvail} 4-Wheeler • ${bikeAvail} 2-Wheeler`;
          }
          if (document.getElementById('sec-parking-avail-2w')) {
            document.getElementById('sec-parking-avail-2w').innerText = bikeAvail;
          }
          if (document.getElementById('sec-parking-avail-4w')) {
            document.getElementById('sec-parking-avail-4w').innerText = `${carAvail} Available 4-Wheelers`;
          }
        }
      } catch (err) {
        console.error("Error setting security parking cards:", err);
      }


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
  },

  async checkInVisitorSubmit(event) {
    if (event) event.preventDefault();

    const payload = {
      visitorName: document.getElementById('sec-vis-name').value.trim(),
      phone: document.getElementById('sec-vis-phone').value.trim(),
      wing: document.getElementById('sec-vis-wing').value,
      flatNumber: document.getElementById('sec-vis-flat').value.trim(),
      vehicleNo: document.getElementById('sec-vis-vehicle').value.trim(),
      purpose: document.getElementById('sec-vis-purpose').value.trim()
    };

    try {
      await SecurityApi.checkInVisitor(payload);
      Toast.success(`Visitor ${payload.visitorName} checked in (INSIDE society).`);
      document.getElementById('sec-checkin-form').reset();
      if (typeof loadActiveVisitors === 'function') {
        await loadActiveVisitors();
      }
      if (typeof loadAllVisitors === 'function') {
        await loadAllVisitors();
      }
    } catch (e) {
      Toast.error(e.message || 'Failed to check in visitor');
    }
  },

  async markVisitorCheckOut(visitorId) {
    try {
      await SecurityApi.checkOutVisitor(visitorId);
      Toast.success('Visitor marked as EXITED');
      if (typeof loadActiveVisitors === 'function') {
        await loadActiveVisitors();
      }
      if (typeof loadAllVisitors === 'function') {
        await loadAllVisitors();
      }
    } catch (e) {
      Toast.error(e.message || 'Failed to record visitor exit');
    }
  },

  preApprovedCache: [],

  async openVerifyPassModal(visitorId) {
    let visitor = null;
    if (this.preApprovedCache && this.preApprovedCache.length) {
      visitor = this.preApprovedCache.find(v => v.id == visitorId);
    }
    if (!visitor) {
      try {
        const list = await SecurityApi.getPreApprovedVisitors();
        this.preApprovedCache = list || [];
        visitor = this.preApprovedCache.find(v => v.id == visitorId);
      } catch (err) {
        console.error('Failed to fetch visitor detail for verification modal', err);
      }
    }

    if (!visitor) {
      Toast.error('Visitor pass details not found');
      return;
    }

    const idInput = document.getElementById('modal-vis-id');
    const nameEl = document.getElementById('modal-vis-name');
    const phoneEl = document.getElementById('modal-vis-phone');
    const unitEl = document.getElementById('modal-vis-unit');
    const hostEl = document.getElementById('modal-vis-host');
    const timeEl = document.getElementById('modal-vis-time');
    const passcodeEl = document.getElementById('modal-vis-passcode');
    const groupBadge = document.getElementById('modal-group-badge');
    const photoImg = document.getElementById('modal-guest-photo');
    const photoPlaceholder = document.getElementById('modal-guest-photo-placeholder');

    if (idInput) idInput.value = visitor.id;
    if (nameEl) nameEl.textContent = visitor.visitorName || 'Lead Visitor';
    if (phoneEl) phoneEl.textContent = visitor.phone || 'N/A';
    const wingVal = visitor.wing || (visitor.flat ? visitor.flat.wing : '');
    const flatVal = visitor.flatNumber || (visitor.flat ? visitor.flat.flatNumber : '');
    if (unitEl) unitEl.textContent = `Wing ${wingVal}-${flatVal}`;
    if (hostEl) hostEl.textContent = visitor.residentName || (visitor.flat && visitor.flat.currentResidentName) || 'Resident Host';
    if (timeEl) timeEl.textContent = `${visitor.expectedDate || 'Today'} ${visitor.expectedTime || ''}`;
    if (passcodeEl) passcodeEl.textContent = visitor.passCode || 'N/A';

    const count = visitor.totalGuestCount || visitor.numberOfVisitors || 1;
    if (groupBadge) {
      groupBadge.innerHTML = `<span class="material-symbols-outlined" style="font-size:16px;">groups</span> Group of ${count} Person${count > 1 ? 's' : ''}`;
    }

    if (visitor.primaryGuestPhoto) {
      if (photoImg) {
        photoImg.src = visitor.primaryGuestPhoto;
        photoImg.style.display = 'block';
      }
      if (photoPlaceholder) photoPlaceholder.style.display = 'none';
    } else {
      if (photoImg) {
        photoImg.src = '';
        photoImg.style.display = 'none';
      }
      if (photoPlaceholder) photoPlaceholder.style.display = 'flex';
    }

    if (typeof Modal !== 'undefined') {
      Modal.open('verify-pass-modal');
    }
  },

  async approveCurrentPreApprovedEntry() {
    const idInput = document.getElementById('modal-vis-id');
    const id = idInput ? idInput.value : null;
    if (!id) return;
    await this.approvePreApprovedEntry(id);
  },

  async approvePreApprovedEntry(id) {
    const approveBtn = document.getElementById('btn-modal-approve-entry');
    if (approveBtn) {
      approveBtn.disabled = true;
      approveBtn.innerHTML = '<span class="material-symbols-outlined" style="font-size:18px;">hourglass_top</span> Verifying...';
    }

    try {
      await SecurityApi.verifyEntry(id, 'Main Gate 1', 'Physical face match verified against pre-approved pass');
      Toast.success('Visitor face pass verified! Entry recorded into society.');
      if (typeof Modal !== 'undefined') {
        Modal.close('verify-pass-modal');
      }
      if (typeof loadPreApprovedVisitors === 'function') {
        await loadPreApprovedVisitors();
      }
      if (typeof loadActiveVisitors === 'function') {
        await loadActiveVisitors();
      }
      if (typeof loadAllVisitors === 'function') {
        await loadAllVisitors();
      }
    } catch (e) {
      Toast.error(e.message || 'Failed to verify visitor entry');
    } finally {
      if (approveBtn) {
        approveBtn.disabled = false;
        approveBtn.innerHTML = '<span class="material-symbols-outlined" style="font-size:18px;">check_circle</span> Approve Entry';
      }
    }
  },

  async rejectCurrentPreApprovedEntry() {
    const idInput = document.getElementById('modal-vis-id');
    const id = idInput ? idInput.value : null;
    if (!id) return;

    const reason = prompt('Please enter the reason for denying entry (e.g. Photo mismatch, unauthorized guests):', 'Photo mismatch or unauthorized entry');
    if (reason === null) return; // User cancelled

    await this.rejectPreApprovedEntry(id, reason);
  },

  async rejectPreApprovedEntry(id, reason = 'Photo mismatch or entry denied') {
    const denyBtn = document.getElementById('btn-modal-deny-entry');
    if (denyBtn) {
      denyBtn.disabled = true;
    }

    try {
      await SecurityApi.rejectEntry(id, reason);
      Toast.warning('Gate pass rejected. Entry has been denied.');
      if (typeof Modal !== 'undefined') {
        Modal.close('verify-pass-modal');
      }
      if (typeof loadPreApprovedVisitors === 'function') {
        await loadPreApprovedVisitors();
      }
      if (typeof loadAllVisitors === 'function') {
        await loadAllVisitors();
      }
    } catch (e) {
      Toast.error(e.message || 'Failed to reject visitor entry');
    } finally {
      if (denyBtn) {
        denyBtn.disabled = false;
      }
    }
  }
};
