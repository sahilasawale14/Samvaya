// ============================================================================
// SECURITY API CLIENT
// ============================================================================

const SecurityApi = {
  getDashboard() {
    return Api.get('/security/dashboard');
  },

  search(query) {
    return Api.get(`/security/search?q=${encodeURIComponent(query)}`);
  },

  // Parking Management
  getParkingSlots() {
    return Api.get('/parking');
  },

  getAvailableParkingSlots() {
    return Api.get('/parking/available');
  },

  getParkingStats() {
    return Api.get('/parking/stats');
  },

  getParkingMetrics() {
    return Api.get('/security/parking/metrics').catch(() => Api.get('/parking/metrics'));
  },


  assignParkingSlot(slotId, flatId) {
    return Api.post(`/parking/assign?slotId=${slotId}&flatId=${flatId}`);
  },

  vacateParkingSlot(slotId) {
    return Api.post(`/parking/vacate?slotId=${slotId}`);
  },

  // Gate Visitors
  getAllVisitors() {
    return Api.get('/visitors');
  },

  getActiveVisitors() {
    return Api.get('/security/visitors/active');
  },

  checkInVisitor(visitorData) {
    return Api.post('/security/visitors/check-in', visitorData);
  },

  checkOutVisitor(visitorId) {
    return Api.put(`/security/visitors/${visitorId}/check-out`);
  },

  getExpectedVisitors() {
    return Api.get('/visitors/expected');
  },

  getVisitorsInside() {
    return Api.get('/security/visitors/active');
  },

  recordVisitorArrival(visitorId) {
    return Api.post(`/visitors/${visitorId}/arrive`);
  },

  recordVisitorEntry(visitorId, gateNumber = 'Main Gate 1', notes = '') {
    const params = new URLSearchParams({ gateNumber });
    if (notes) params.append('notes', notes);
    return Api.post(`/visitors/${visitorId}/entry?${params.toString()}`);
  },

  recordVisitorExit(visitorId) {
    return Api.put(`/security/visitors/${visitorId}/check-out`);
  },

  getPreApprovedVisitors() {
    return Api.get('/security/visitors/pre-approved');
  },

  verifyEntry(visitorId, gateNumber = 'Main Gate 1', notes = 'Photo match verified by gate security') {
    const params = new URLSearchParams({ gateNumber });
    if (notes) params.append('notes', notes);
    return Api.put(`/security/visitors/${visitorId}/verify-entry?${params.toString()}`);
  },

  rejectEntry(visitorId, remarks = 'Photo mismatch or entry denied') {
    return Api.put(`/security/visitors/${visitorId}/reject-entry`, { remarks });
  },

  deleteVisitor(visitorId) {
    return Api.delete(`/security/visitors/${visitorId}`).catch(() => Api.delete(`/visitors/${visitorId}`));
  },

  // Gate Deliveries
  getAllDeliveries() {
    return Api.get('/deliveries');
  },

  recordDeliveryArrival(deliveryId) {
    return Api.post(`/deliveries/${deliveryId}/arrive`);
  },

  completeDelivery(deliveryId) {
    return Api.post(`/deliveries/${deliveryId}/complete`);
  },

  getIncidents() {
    return Api.get('/incidents');
  },

  reportIncident(incidentData) {
    return Api.post('/incidents', incidentData);
  }
};
