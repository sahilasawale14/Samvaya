// ============================================================================
// SECURITY GATE OPERATIONS CONTROLLER
// ============================================================================

const SecurityPage = {
  VISITOR_PASSES_KEY: 'samvaya_visitor_passes',
  activeTab: 'expected',
  expectedCache: [],
  insideCache: [],

  readVisitorPasses() {
    const key = SecurityPage.VISITOR_PASSES_KEY;
    const parseList = (raw) => {
      if (!raw) return [];
      try {
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
      } catch (e) {
        return [];
      }
    };

    let list = parseList(localStorage.getItem(key));
    if (!list.length) {
      list = parseList(sessionStorage.getItem(key));
    }
    if (!list.length) {
      list = parseList(localStorage.getItem('samvaya_preapproved_visitors'));
    }
    if (!list.length) {
      list = parseList(sessionStorage.getItem('samvaya_preapproved_visitors'));
    }
    if (!list.length && window._samvaya_visitor_passes_cache && window._samvaya_visitor_passes_cache.length) {
      list = window._samvaya_visitor_passes_cache;
    }
    return list;
  },

  writeVisitorPasses(list) {
    const key = SecurityPage.VISITOR_PASSES_KEY;
    const cleanList = Array.isArray(list) ? list : [];

    window._samvaya_visitor_passes_cache = cleanList;

    const sanitizeForStorage = (items, maxPhotoChars = 40000) => {
      return items.map(item => {
        if (item.photo && item.photo.length > maxPhotoChars) {
          const initial = (item.visitorName ? item.visitorName[0] : 'G').toUpperCase();
          return {
            ...item,
            photo: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160"><rect width="160" height="160" fill="%230d9488" rx="80"/><text x="50%" y="54%" font-size="64" font-family="sans-serif" font-weight="bold" fill="%23ffffff" dominant-baseline="middle" text-anchor="middle">${initial}</text></svg>`,
            primaryGuestPhoto: null
          };
        }
        return item;
      });
    };

    try {
      localStorage.setItem(key, JSON.stringify(cleanList));
      localStorage.setItem('samvaya_preapproved_visitors', JSON.stringify(cleanList));
    } catch (err) {
      console.warn('localStorage full or quota exceeded, attempting quota recovery in security:', err);
      try {
        const compact = sanitizeForStorage(cleanList, 15000);
        localStorage.setItem(key, JSON.stringify(compact));
        localStorage.setItem('samvaya_preapproved_visitors', JSON.stringify(compact));
      } catch (e2) {}
    }

    try {
      sessionStorage.setItem(key, JSON.stringify(cleanList));
      sessionStorage.setItem('samvaya_preapproved_visitors', JSON.stringify(cleanList));
    } catch (e) {}

    try {
      window.dispatchEvent(new CustomEvent('samvaya:passes-updated', { detail: cleanList }));
    } catch (e) {}
  },

  // --------------------------------------------------------------------------
  // TAB NAVIGATION
  // --------------------------------------------------------------------------
  switchTab(tab) {
    SecurityPage.activeTab = tab;
    const btnExpected = document.getElementById('tab-btn-expected');
    const btnInside = document.getElementById('tab-btn-inside');
    const panelExpected = document.getElementById('tab-panel-expected');
    const panelInside = document.getElementById('tab-panel-inside');

    if (tab === 'expected') {
      if (btnExpected) {
        btnExpected.style.background = 'var(--primary)';
        btnExpected.style.color = '#ffffff';
        btnExpected.style.borderColor = 'var(--primary)';
      }
      if (btnInside) {
        btnInside.style.background = 'var(--surface)';
        btnInside.style.color = 'var(--on-surface-variant)';
        btnInside.style.borderColor = 'var(--outline-variant)';
      }
      if (panelExpected) panelExpected.style.display = 'block';
      if (panelInside) panelInside.style.display = 'none';
    } else {
      if (btnExpected) {
        btnExpected.style.background = 'var(--surface)';
        btnExpected.style.color = 'var(--on-surface-variant)';
        btnExpected.style.borderColor = 'var(--outline-variant)';
      }
      if (btnInside) {
        btnInside.style.background = 'var(--success)';
        btnInside.style.color = '#ffffff';
        btnInside.style.borderColor = 'var(--success)';
      }
      if (panelExpected) panelExpected.style.display = 'none';
      if (panelInside) panelInside.style.display = 'block';
    }
  },

  async initVisitorsPage() {
    SecurityPage.switchTab('expected');
    await SecurityPage.renderAllTables();
  },

  async renderAllTables() {
    await Promise.allSettled([
      SecurityPage.loadExpectedVisitors(),
      SecurityPage.loadInsideVisitors()
    ]);
  },

  // --------------------------------------------------------------------------
  // TAB 1: EXPECTED GATE PASSES
  // --------------------------------------------------------------------------
  async loadExpectedVisitors() {
    try {
      let expectedList = [];
      const localList = SecurityPage.readVisitorPasses();

      const deletedIds = new Set((() => {
        try { return JSON.parse(localStorage.getItem('samvaya_deleted_visitor_ids') || '[]'); } catch(e) { return []; }
      })().map(String));

      // 1. Process local passes
      const localStatusMap = new Map();
      localList.forEach(v => {
        const st = (v.status || v.approvalStatus || '').toUpperCase();
        if (v.id) localStatusMap.set(String(v.id), st);
        if (v.passCode) localStatusMap.set(String(v.passCode), st);
        const key = (v.passCode || v.id || v.visitorName).toString();
        localStatusMap.set(key, st);

        const isExpected = (st === 'EXPECTED' || st === 'PRE_APPROVED')
          && st !== 'CHECKED_IN' && st !== 'INSIDE' && st !== 'CHECKED_OUT' && st !== 'EXITED' && st !== 'REVOKED' && st !== 'REJECTED';

        if (isExpected && !deletedIds.has(String(v.id)) && !deletedIds.has(String(v.passCode))) {
          expectedList.push(v);
        }
      });

      // 2. Fetch remote passes
      try {
        const remote = await SecurityApi.getExpectedVisitors();
        if (Array.isArray(remote) && remote.length) {
          const existingKeys = new Set(expectedList.map(v => (v.passCode || v.id || v.visitorName).toString()));
          remote.forEach(v => {
            const key = (v.passCode || v.id || v.visitorName).toString();
            const idStr = v.id ? String(v.id) : null;
            const passCodeStr = v.passCode ? String(v.passCode) : null;

            if (idStr && deletedIds.has(idStr)) return;
            if (passCodeStr && deletedIds.has(passCodeStr)) return;

            const localSt = localStatusMap.get(key) || (idStr ? localStatusMap.get(idStr) : null) || (passCodeStr ? localStatusMap.get(passCodeStr) : null);
            const effectiveStatus = (localSt || v.status || v.approvalStatus || '').toUpperCase();

            const isExpected = (effectiveStatus === 'EXPECTED' || effectiveStatus === 'PRE_APPROVED')
              && effectiveStatus !== 'CHECKED_IN' && effectiveStatus !== 'INSIDE' && effectiveStatus !== 'CHECKED_OUT' && effectiveStatus !== 'EXITED' && effectiveStatus !== 'REVOKED' && effectiveStatus !== 'REJECTED';

            if (isExpected && !existingKeys.has(key)) {
              existingKeys.add(key);
              expectedList.push(v);
            }
          });
        }
      } catch (err) {
        // Offline / fallback to localList
      }

      SecurityPage.expectedCache = expectedList;

      // Update counters
      const badge = document.getElementById('tab-expected-badge');
      if (badge) badge.textContent = expectedList.length;
      const legacyBadge = document.getElementById('preapproved-visitors-count');
      if (legacyBadge) legacyBadge.textContent = expectedList.length;

      // Render Tab 1 Table
      const columns = [
        {
          label: 'Photo Avatar',
          render: r => {
            const photo = r.primaryGuestPhoto || r.photo;
            return photo ? `
              <img src="${photo}" alt="${r.visitorName}" style="width:42px; height:42px; border-radius:50%; object-fit:cover; border:2px solid var(--primary); cursor:pointer;" onclick="SecurityPage.openVerifyPassModal('${r.id}')" title="Click to inspect face photo">
            ` : `
              <div style="width:42px; height:42px; border-radius:50%; background:var(--surface-variant); display:flex; align-items:center; justify-content:center; color:var(--on-surface-variant); cursor:pointer;" onclick="SecurityPage.openVerifyPassModal('${r.id}')" title="Click to view details">
                <span class="material-symbols-outlined" style="font-size:22px;">person</span>
              </div>
            `;
          }
        },
        {
          label: 'Visitor Name & Phone',
          render: r => `
            <div>
              <div style="font-weight:700; color:var(--on-surface); font-size:13px;">${r.visitorName}</div>
              <div style="font-size:11px; color:var(--on-surface-variant);">${r.phone || '-'}</div>
            </div>
          `
        },
        {
          label: 'Visiting Flat',
          render: r => {
            const wing = r.wing || (r.flat ? r.flat.wing : '');
            const flat = r.flatNumber || (r.flat ? r.flat.flatNumber : '');
            let dest = 'Unit';
            if (flat) {
              const fStr = String(flat);
              if (fStr.toUpperCase().includes('WING') || fStr.includes('-')) {
                dest = fStr;
              } else if (wing) {
                dest = `Wing ${wing}-${fStr}`;
              } else {
                dest = `Flat ${fStr}`;
              }
            }
            return `<b style="color:var(--primary); font-size:13px;">${dest}</b>`;
          }
        },
        {
          label: 'Guests',
          render: r => {
            const count = r.totalGuestCount || r.totalGuests || r.numberOfVisitors || 1;
            return `<span class="badge ${count > 1 ? 'badge-primary' : 'badge-neutral'}" style="font-size:11px; font-weight:600; display:inline-flex; align-items:center; gap:4px;">
              <span class="material-symbols-outlined" style="font-size:14px;">groups</span>
              ${count} Person${count > 1 ? 's' : ''}
            </span>`;
          }
        },
        {
          label: 'Pass Code',
          render: r => `<code style="font-size:14px; font-weight:700; color:var(--secondary); letter-spacing:1px; background:rgba(13,148,136,0.08); padding:3px 8px; border-radius:4px;">${r.passCode || '-'}</code>`
        },
        {
          label: 'Actions',
          render: r => {
            const safeName = (r.visitorName || '').replace(/'/g, "\\'");
            return `
              <div style="display:flex; align-items:center; gap:8px;">
                <button class="btn btn-primary" style="background:var(--success); color:white; padding:6px 12px; font-size:12px; font-weight:600; display:inline-flex; align-items:center; gap:4px; border:none; border-radius:var(--radius-sm); cursor:pointer;" onclick="SecurityPage.verifyAndCheckInPass('${r.id}')" title="Verify Identity & Allow Entry">
                  <span class="material-symbols-outlined" style="font-size:16px;">check_circle</span> Verify & Check-In
                </button>
                <button class="btn btn-ghost" style="color:var(--danger); padding:6px 8px; font-size:12px; display:inline-flex; align-items:center; border:none; background:transparent; cursor:pointer;" onclick="SecurityPage.deleteVisitorPass('${r.id}', '${safeName}')" title="Delete Pass">
                  <span class="material-symbols-outlined" style="font-size:18px;">delete</span>
                </button>
              </div>
            `;
          }
        }
      ];

      Table.render('expected-visitors-table', columns, expectedList, 'No expected visitors awaiting gate clearance.');
      if (document.getElementById('preapproved-security-visitors-table')) {
        Table.render('preapproved-security-visitors-table', columns, expectedList, 'No pre-approved visitors.');
      }
    } catch (err) {
      console.error('Failed to load expected visitors:', err);
    }
  },

  // --------------------------------------------------------------------------
  // TAB 2: CURRENTLY INSIDE CAMPUS
  // --------------------------------------------------------------------------
  async loadInsideVisitors() {
    try {
      let insideList = [];
      const localList = SecurityPage.readVisitorPasses();

      const deletedIds = new Set((() => {
        try { return JSON.parse(localStorage.getItem('samvaya_deleted_visitor_ids') || '[]'); } catch(e) { return []; }
      })().map(String));

      // 1. Process local passes
      const exitedInLocal = new Set();
      localList.forEach(v => {
        const st = (v.status || v.approvalStatus || '').toUpperCase();
        if (st === 'CHECKED_OUT' || st === 'EXITED') {
          if (v.id) exitedInLocal.add(String(v.id));
          if (v.passCode) exitedInLocal.add(String(v.passCode));
          exitedInLocal.add((v.passCode || v.id || v.visitorName).toString());
        }

        const isInside = (st === 'CHECKED_IN' || st === 'INSIDE' || st === 'VERIFIED_ENTRY')
          && st !== 'CHECKED_OUT' && st !== 'EXITED' && st !== 'REVOKED' && st !== 'REJECTED';

        if (isInside && !deletedIds.has(String(v.id)) && !deletedIds.has(String(v.passCode))) {
          insideList.push(v);
        }
      });

      // 2. Fetch remote passes
      try {
        const remote = await SecurityApi.getVisitorsInside();
        if (Array.isArray(remote) && remote.length) {
          const existingKeys = new Set(insideList.map(v => (v.passCode || v.id || v.visitorName).toString()));
          remote.forEach(v => {
            const key = (v.passCode || v.id || v.visitorName).toString();
            const idStr = v.id ? String(v.id) : null;
            const passCodeStr = v.passCode ? String(v.passCode) : null;

            if (idStr && deletedIds.has(idStr)) return;
            if (passCodeStr && deletedIds.has(passCodeStr)) return;
            if (exitedInLocal.has(key) || (idStr && exitedInLocal.has(idStr)) || (passCodeStr && exitedInLocal.has(passCodeStr))) return;

            const effectiveStatus = (v.status || v.approvalStatus || '').toUpperCase();
            const isInside = (effectiveStatus === 'CHECKED_IN' || effectiveStatus === 'INSIDE' || effectiveStatus === 'VERIFIED_ENTRY')
              && effectiveStatus !== 'CHECKED_OUT' && effectiveStatus !== 'EXITED' && effectiveStatus !== 'REVOKED';

            if (isInside && !existingKeys.has(key)) {
              existingKeys.add(key);
              insideList.push(v);
            }
          });
        }
      } catch (err) {
        // Offline / fallback to localList
      }

      SecurityPage.insideCache = insideList;

      // Update counters
      const badge = document.getElementById('tab-inside-badge');
      if (badge) badge.textContent = insideList.length;
      const legacyBadge = document.getElementById('active-visitors-count');
      if (legacyBadge) legacyBadge.textContent = insideList.length;

      // Render Tab 2 Table
      const columns = [
        {
          label: 'Photo Avatar',
          render: r => {
            const photo = r.primaryGuestPhoto || r.photo;
            return photo ? `
              <img src="${photo}" alt="${r.visitorName}" style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:2px solid var(--success);">
            ` : `
              <div style="width:40px; height:40px; border-radius:50%; background:var(--surface-variant); display:flex; align-items:center; justify-content:center; color:var(--on-surface-variant);">
                <span class="material-symbols-outlined" style="font-size:22px;">person</span>
              </div>
            `;
          }
        },
        {
          label: 'Visitor Name',
          render: r => {
            const count = r.totalGuestCount || r.totalGuests || r.numberOfVisitors || 1;
            return `
              <div>
                <div style="font-weight:700; color:var(--on-surface); font-size:13px;">${r.visitorName}</div>
                <div style="font-size:11px; color:var(--on-surface-variant);">${r.phone || '-'}</div>
                ${count > 1 ? `<span class="badge badge-neutral" style="font-size:10px; padding:1px 6px; margin-top:2px; display:inline-block;">Group of ${count}</span>` : ''}
              </div>
            `;
          }
        },
        {
          label: 'Flat No',
          render: r => {
            const wing = r.wing || (r.flat ? r.flat.wing : '');
            const flat = r.flatNumber || (r.flat ? r.flat.flatNumber : '');
            let dest = 'Unit';
            if (flat) {
              const fStr = String(flat);
              if (fStr.toUpperCase().includes('WING') || fStr.includes('-')) {
                dest = fStr;
              } else if (wing) {
                dest = `Wing ${wing}-${fStr}`;
              } else {
                dest = `Flat ${fStr}`;
              }
            }
            return `<b>${dest}</b>`;
          }
        },
        {
          label: 'Entry Time',
          render: r => {
            if (!r.entryTime) return 'Just now';
            try {
              return `<span style="font-size:12px; font-weight:600; color:var(--on-surface);">${new Date(r.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>`;
            } catch (e) {
              return 'Just now';
            }
          }
        },
        {
          label: 'Actions',
          render: r => {
            const safeName = (r.visitorName || '').replace(/'/g, "\\'");
            return `
              <div style="display:flex; align-items:center; gap:8px;">
                <button class="btn btn-secondary" style="background:#f59e0b; color:white; border:none; padding:6px 12px; font-size:12px; font-weight:600; display:inline-flex; align-items:center; gap:4px; border-radius:var(--radius-sm); cursor:pointer;" onclick="SecurityPage.markVisitorExit('${r.id}')" title="Log Visitor Departure">
                  <span class="material-symbols-outlined" style="font-size:16px;">logout</span> Mark Exit
                </button>
                <button class="btn btn-ghost" style="color:var(--danger); padding:6px 8px; font-size:12px; display:inline-flex; align-items:center; border:none; background:transparent; cursor:pointer;" onclick="SecurityPage.deleteVisitorPass('${r.id}', '${safeName}')" title="Delete Log Record">
                  <span class="material-symbols-outlined" style="font-size:18px;">delete</span>
                </button>
              </div>
            `;
          }
        }
      ];

      Table.render('inside-visitors-table', columns, insideList, 'No visitors currently recorded inside society premises.');
      if (document.getElementById('active-security-visitors-table')) {
        Table.render('active-security-visitors-table', columns, insideList, 'No active visitors inside.');
      }
    } catch (err) {
      console.error('Failed to load inside visitors:', err);
    }
  },

  // --------------------------------------------------------------------------
  // LIFECYCLE ACTION: VERIFY & CHECK-IN
  // --------------------------------------------------------------------------
  async verifyAndCheckInPass(id) {
    try {
      const nowIso = new Date().toISOString();
      const list = SecurityPage.readVisitorPasses();
      let found = false;

      const updated = list.map(v => {
        if (String(v.id) === String(id) || String(v.passCode) === String(id)) {
          found = true;
          return {
            ...v,
            status: 'CHECKED_IN',
            approvalStatus: 'VERIFIED_ENTRY',
            entryTime: nowIso
          };
        }
        return v;
      });

      if (!found) {
        // Fallback from expectedCache
        const cached = (SecurityPage.expectedCache || []).find(v => String(v.id) === String(id) || String(v.passCode) === String(id));
        if (cached) {
          updated.push({
            ...cached,
            status: 'CHECKED_IN',
            approvalStatus: 'VERIFIED_ENTRY',
            entryTime: nowIso
          });
        }
      }

      SecurityPage.writeVisitorPasses(updated);

      try {
        await SecurityApi.checkInPass(id);
      } catch (e) {
        console.warn('Backend checkInPass failed, updated locally:', e);
      }

      Toast.success('Visitor identity verified! Checked into society premises.');

      // Immediately refresh both tables without requiring page reload
      await SecurityPage.renderAllTables();
    } catch (err) {
      console.error('Error during verify and check-in:', err);
      Toast.error('Failed to check in visitor');
    }
  },

  // --------------------------------------------------------------------------
  // LIFECYCLE ACTION: MARK EXIT (CHECK OUT)
  // --------------------------------------------------------------------------
  async markVisitorExit(id) {
    try {
      const nowIso = new Date().toISOString();
      const list = SecurityPage.readVisitorPasses();
      let found = false;

      const updated = list.map(v => {
        if (String(v.id) === String(id) || String(v.passCode) === String(id)) {
          found = true;
          return {
            ...v,
            status: 'CHECKED_OUT',
            approvalStatus: 'CHECKED_OUT',
            exitTime: nowIso
          };
        }
        return v;
      });

      if (!found) {
        const cached = (SecurityPage.insideCache || []).find(v => String(v.id) === String(id) || String(v.passCode) === String(id));
        if (cached) {
          updated.push({
            ...cached,
            status: 'CHECKED_OUT',
            approvalStatus: 'CHECKED_OUT',
            exitTime: nowIso
          });
        }
      }

      SecurityPage.writeVisitorPasses(updated);

      try {
        await SecurityApi.checkOutVisitor(id);
      } catch (e) {
        console.warn('Backend checkOutVisitor failed, updated locally:', e);
      }

      Toast.success('Visitor departure logged successfully.');

      // Immediately refresh both tables without requiring page reload
      await SecurityPage.renderAllTables();
    } catch (err) {
      console.error('Error during mark visitor exit:', err);
      Toast.error('Failed to log visitor departure');
    }
  },

  // --------------------------------------------------------------------------
  // LIFECYCLE ACTION: DELETE PASS / LOG RECORD
  // --------------------------------------------------------------------------
  async deleteVisitorPass(id, visitorName) {
    const displayName = visitorName || 'this visitor pass';
    if (!confirm(`Are you sure you want to permanently delete the pass for ${displayName}? This action cannot be undone.`)) {
      return;
    }

    try {
      // 1. Mark ID as deleted
      try {
        const delList = JSON.parse(localStorage.getItem('samvaya_deleted_visitor_ids') || '[]');
        delList.push(String(id));
        localStorage.setItem('samvaya_deleted_visitor_ids', JSON.stringify(delList));
      } catch (e) {}

      // 2. Remove from local storage
      const list = SecurityPage.readVisitorPasses();
      const updated = list.filter(v => String(v.id) !== String(id) && String(v.passCode) !== String(id));
      SecurityPage.writeVisitorPasses(updated);

      // 3. Call backend delete API
      try {
        await SecurityApi.deleteVisitor(id);
      } catch (e) {
        console.warn('Backend delete visitor failed, purged locally:', e);
      }

      Toast.success(`Pass for ${displayName} deleted successfully.`);

      // 4. Immediately refresh both tables
      await SecurityPage.renderAllTables();
    } catch (err) {
      console.error('Failed to delete visitor pass:', err);
      Toast.error('Failed to delete visitor pass');
    }
  },

  // --------------------------------------------------------------------------
  // WALK-IN MODAL & SUBMISSION
  // --------------------------------------------------------------------------
  openWalkInModal() {
    if (typeof Modal !== 'undefined') {
      Modal.open('walkin-checkin-modal');
    }
  },

  async checkInVisitorSubmit(event) {
    if (event) event.preventDefault();

    const name = (document.getElementById('sec-vis-name') ? document.getElementById('sec-vis-name').value : '').trim();
    const phone = (document.getElementById('sec-vis-phone') ? document.getElementById('sec-vis-phone').value : '').trim();
    const wing = document.getElementById('sec-vis-wing') ? document.getElementById('sec-vis-wing').value : 'A';
    const flatNumber = (document.getElementById('sec-vis-flat') ? document.getElementById('sec-vis-flat').value : '').trim();
    const vehicleNo = (document.getElementById('sec-vis-vehicle') ? document.getElementById('sec-vis-vehicle').value : '').trim();
    const purpose = document.getElementById('sec-vis-purpose') ? document.getElementById('sec-vis-purpose').value : 'Guest';

    const nowIso = new Date().toISOString();
    const payload = {
      visitorName: name,
      phone: phone,
      wing: wing,
      flatNumber: flatNumber,
      vehicleNo: vehicleNo,
      purpose: purpose,
      status: 'CHECKED_IN',
      approvalStatus: 'VERIFIED_ENTRY',
      entryTime: nowIso
    };

    // Store locally immediately
    const list = SecurityPage.readVisitorPasses();
    const newPass = {
      id: Date.now(),
      visitorName: name,
      phone: phone,
      wing: wing,
      flatNumber: flatNumber,
      vehicleNo: vehicleNo,
      vehicleNumber: vehicleNo,
      purpose: purpose,
      status: 'CHECKED_IN',
      approvalStatus: 'VERIFIED_ENTRY',
      entryTime: nowIso,
      passCode: String(Math.floor(100000 + Math.random() * 900000)),
      totalGuestCount: 1,
      numberOfVisitors: 1
    };
    list.unshift(newPass);
    SecurityPage.writeVisitorPasses(list);

    try {
      await SecurityApi.checkInVisitor(payload);
    } catch (e) {
      console.warn('Backend checkInVisitor failed, saved locally:', e);
    }

    Toast.success(`Visitor ${name} recorded inside campus!`);
    const form = document.getElementById('sec-checkin-form');
    if (form) form.reset();
    if (typeof Modal !== 'undefined') {
      Modal.close('walkin-checkin-modal');
    }

    // Refresh and switch to Inside tab
    await SecurityPage.renderAllTables();
    SecurityPage.switchTab('inside');
  },

  // --------------------------------------------------------------------------
  // FACE-VERIFY MODAL INSPECTION
  // --------------------------------------------------------------------------
  async openVerifyPassModal(visitorId) {
    let visitor = (SecurityPage.expectedCache || []).find(v => String(v.id) === String(visitorId) || String(v.passCode) === String(visitorId));
    if (!visitor) {
      const passes = SecurityPage.readVisitorPasses();
      visitor = passes.find(v => String(v.id) === String(visitorId) || String(v.passCode) === String(visitorId));
    }
    if (!visitor) {
      try {
        const list = await SecurityApi.getExpectedVisitors();
        visitor = (list || []).find(v => String(v.id) === String(visitorId) || String(v.passCode) === String(visitorId));
      } catch (err) {}
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
    let unitStr = 'Unit';
    if (flatVal) {
      const fStr = String(flatVal);
      if (fStr.toUpperCase().includes('WING') || fStr.includes('-')) {
        unitStr = fStr;
      } else if (wingVal) {
        unitStr = `Wing ${wingVal}-${fStr}`;
      } else {
        unitStr = `Flat ${fStr}`;
      }
    }
    if (unitEl) unitEl.textContent = unitStr;
    if (hostEl) hostEl.textContent = visitor.residentName || (visitor.flat && visitor.flat.currentResidentName) || 'Resident Host';
    if (timeEl) timeEl.textContent = `${visitor.expectedDate || 'Today'} ${visitor.expectedTime || ''}`;
    if (passcodeEl) passcodeEl.textContent = visitor.passCode || 'N/A';

    const count = visitor.totalGuestCount || visitor.totalGuests || visitor.numberOfVisitors || 1;
    if (groupBadge) {
      groupBadge.innerHTML = `<span class="material-symbols-outlined" style="font-size:16px;">groups</span> Group of ${count} Person${count > 1 ? 's' : ''}`;
    }

    const photoSrc = visitor.primaryGuestPhoto || visitor.photo;
    if (photoSrc) {
      if (photoImg) {
        photoImg.src = photoSrc;
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

    if (typeof Modal !== 'undefined') {
      Modal.close('verify-pass-modal');
    }
    await this.verifyAndCheckInPass(id);
  },

  async rejectCurrentPreApprovedEntry() {
    const idInput = document.getElementById('modal-vis-id');
    const id = idInput ? idInput.value : null;
    if (!id) return;

    const reason = prompt('Please enter the reason for denying entry (e.g. Photo mismatch, unauthorized guests):', 'Photo mismatch or unauthorized entry');
    if (reason === null) return;

    await this.rejectPreApprovedEntry(id, reason);
  },

  async rejectPreApprovedEntry(id, reason = 'Photo mismatch or entry denied') {
    try {
      const list = SecurityPage.readVisitorPasses();
      const updated = list.map(v => (String(v.id) === String(id) || String(v.passCode) === String(id)) ? { ...v, status: 'REJECTED', approvalStatus: 'REJECTED' } : v);
      SecurityPage.writeVisitorPasses(updated);

      try {
        await SecurityApi.rejectEntry(id, reason);
      } catch (e) {}

      Toast.warning('Gate pass rejected. Entry has been denied.');
      if (typeof Modal !== 'undefined') {
        Modal.close('verify-pass-modal');
      }
      await SecurityPage.renderAllTables();
    } catch (e) {
      Toast.error('Failed to reject visitor pass');
    }
  },

  // Legacy alias helpers
  async allowPreApprovedEntryDirect(id) {
    await this.verifyAndCheckInPass(id);
  },

  async markVisitorCheckOut(id) {
    await this.markVisitorExit(id);
  },

  // --------------------------------------------------------------------------
  // DASHBOARD SUPPORT (FOR dashboard.html)
  // --------------------------------------------------------------------------
  async initDashboard() {
    Sidebar.render('sidebar-container', 'dashboard');
    Navbar.render('navbar-container', 'Gate Operations & Live Security Command');

    try {
      let stats = {};
      try {
        stats = await SecurityApi.getDashboard();
      } catch (e) {}

      const localPasses = SecurityPage.readVisitorPasses();
      let recentVisitors = Array.isArray(stats.recentGateVisitors) ? [...stats.recentGateVisitors] : [];
      if (localPasses.length > 0) {
        const seen = new Set(recentVisitors.map(v => (v.passCode || v.id || v.visitorName).toString()));
        localPasses.forEach(p => {
          const key = (p.passCode || p.id || p.visitorName).toString();
          if (!seen.has(key)) {
            seen.add(key);
            recentVisitors.unshift(p);
          }
        });
      }

      const pendingPassesCount = localPasses.filter(p => (p.status === 'PRE_APPROVED' || p.status === 'EXPECTED')).length;
      const insidePassesCount = localPasses.filter(p => (p.status === 'INSIDE' || p.status === 'CHECKED_IN')).length;

      const statToday = document.getElementById('stat-visitors-today');
      if (statToday) statToday.innerText = Math.max(pendingPassesCount, stats.expectedVisitorsToday || 0);
      const statInside = document.getElementById('stat-visitors-inside');
      if (statInside) statInside.innerText = insidePassesCount + (stats.visitorsCurrentlyInside || 0);

      const delivPending = document.getElementById('stat-deliveries-pending');
      if (delivPending) delivPending.innerText = stats.deliveriesPendingAtGate || 0;
      const workersInside = document.getElementById('stat-workers-inside');
      if (workersInside) workersInside.innerText = stats.temporaryWorkersInside || 0;

      // Dynamic Parking Metrics for Security Command
      try {
        let pMetrics = null;
        try {
          pMetrics = await SecurityApi.getParkingMetrics();
        } catch (e) {}

        let carAvail = 10, carTotal = 44, carOcc = 34;
        let bikeAvail = 20, bikeTotal = 70, bikeOcc = 50;
        let totalOcc = 84, totalSlots = 114, totalAvail = 30;

        if (pMetrics && pMetrics.cars && pMetrics.bikes && pMetrics.totalSlots > 0 && pMetrics.cars.total > 0) {
          carAvail = pMetrics.cars.available != null ? pMetrics.cars.available : 10;
          carTotal = pMetrics.cars.total != null ? pMetrics.cars.total : 44;
          carOcc = pMetrics.cars.occupied != null ? pMetrics.cars.occupied : (carTotal - carAvail);

          bikeAvail = pMetrics.bikes.available != null ? pMetrics.bikes.available : 20;
          bikeTotal = pMetrics.bikes.total != null ? pMetrics.bikes.total : 70;
          bikeOcc = pMetrics.bikes.occupied != null ? pMetrics.bikes.occupied : (bikeTotal - bikeAvail);

          totalOcc = pMetrics.totalOccupied != null ? pMetrics.totalOccupied : (carOcc + bikeOcc);
          totalSlots = pMetrics.totalSlots != null ? pMetrics.totalSlots : (carTotal + bikeTotal);
          totalAvail = pMetrics.totalAvailable != null ? pMetrics.totalAvailable : (totalSlots - totalOcc);
        } else if (stats.totalParkingSlots && stats.totalParkingSlots > 0) {
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

        if (document.getElementById('sec-car-parking-avail')) {
          document.getElementById('sec-car-parking-avail').innerText = `${carAvail} / ${carTotal} Available`;
        }
        if (document.getElementById('sec-car-parking-sub')) {
          document.getElementById('sec-car-parking-sub').innerText = `${carOcc} Occupied`;
        }

        if (document.getElementById('sec-bike-parking-avail')) {
          document.getElementById('sec-bike-parking-avail').innerText = `${bikeAvail} / ${bikeTotal} Available`;
        }
        if (document.getElementById('sec-bike-parking-sub')) {
          document.getElementById('sec-bike-parking-sub').innerText = `${bikeOcc} Occupied`;
        }

        if (document.getElementById('sec-total-parking-occ')) {
          document.getElementById('sec-total-parking-occ').innerText = `${totalOcc} / ${totalSlots} Occupied`;
        }
        if (document.getElementById('sec-total-parking-sub')) {
          document.getElementById('sec-total-parking-sub').innerText = `${totalAvail} Available Slots`;
        }
      } catch (err) {
        console.error("Error setting security parking cards:", err);
      }

      // Render Visitors Table on dashboard if present
      if (document.getElementById('gate-visitors-table')) {
        Table.render('gate-visitors-table', [
          { label: 'Visitor Name', render: r => `<b>${r.visitorName}</b>` },
          { label: 'Phone', key: 'phone' },
          { label: 'Destination Unit', render: r => {
            const flat = r.flatNumber || (r.flat ? r.flat.flatNumber : '');
            const wing = r.wing || (r.flat ? r.flat.wing : '');
            if (!flat) return '-';
            const fStr = String(flat);
            if (fStr.toUpperCase().includes('WING') || fStr.includes('-')) return `<b>${fStr}</b>`;
            return `<b>Wing ${wing ? `${wing}-` : ''}${fStr}</b>`;
          }},
          { label: 'Host Resident', render: r => r.residentName || (r.flat && r.flat.currentResidentName) || 'Resident Host' },
          { label: 'Pass Code', render: r => `<code style="font-weight:700; color:var(--primary);">${r.passCode || '-'}</code>` },
          { label: 'Status', render: r => `<span class="badge ${r.status === 'INSIDE' || r.status === 'CHECKED_IN' ? 'badge-success' : (r.status === 'PRE_APPROVED' || r.status === 'EXPECTED' ? 'badge-info' : 'badge-warning')}">${r.status}</span>` },
          { label: 'Actions', render: r => `
            ${(r.status === 'PRE_APPROVED' || r.status === 'EXPECTED') ? `<button class="btn btn-primary" style="padding:4px 8px; font-size:11px;" onclick="SecurityPage.verifyAndCheckInPass('${r.id}')">Allow Entry</button>` : ''}
            ${(r.status === 'INSIDE' || r.status === 'CHECKED_IN') ? `<button class="btn btn-ghost" style="color:var(--danger); padding:4px 8px; font-size:11px;" onclick="SecurityPage.markVisitorExit('${r.id}')">Log Exit</button>` : ''}
            ${(r.status === 'EXITED' || r.status === 'CHECKED_OUT') ? `<span style="color:var(--outline); font-size:12px;">Completed</span>` : ''}
          `}
        ], recentVisitors || []);
      }

      // Render Deliveries Table on dashboard if present
      if (document.getElementById('gate-deliveries-table')) {
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
      }

    } catch (e) {
      console.error(e);
    }
  },

  async markVisitorArrival(id) {
    try {
      await SecurityApi.recordVisitorArrival(id);
    } catch (e) {}
    Toast.info('Visitor arrival logged at gate. Resident notified.');
    this.initDashboard();
  },

  async allowVisitorEntry(id) {
    await this.verifyAndCheckInPass(id);
    this.initDashboard();
  },

  async markDeliveryArrival(id) {
    try {
      await SecurityApi.recordDeliveryArrival(id);
    } catch (e) {}
    Toast.info('Delivery arrived at gate');
    this.initDashboard();
  },

  async completeDelivery(id) {
    try {
      await SecurityApi.completeDelivery(id);
    } catch (e) {}
    Toast.success('Delivery marked complete');
    this.initDashboard();
  },

  async handleLiveGateSearch(query) {
    if (!query || query.trim().length < 2) {
      this.initDashboard();
      return;
    }
    try {
      const results = await SecurityApi.search(query);
      if (results && results.visitors && document.getElementById('gate-visitors-table')) {
        Table.render('gate-visitors-table', [
          { label: 'Visitor Name', render: r => `<b>${r.visitorName}</b>` },
          { label: 'Phone', key: 'phone' },
          { label: 'Destination Unit', render: r => `Wing ${r.flat ? r.flat.wing : ''}-${r.flat ? r.flat.flatNumber : ''}` },
          { label: 'Pass Code', render: r => `<code>${r.passCode || '-'}</code>` },
          { label: 'Status', render: r => `<span class="badge badge-info">${r.status}</span>` }
        ], results.visitors);
      }
    } catch (e) {}
  }
};
