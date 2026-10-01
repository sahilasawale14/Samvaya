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

  getAllVisitors() {
    return Api.get('/visitors');
  },

  getExpectedVisitors() {
    return Api.get('/visitors/expected');
  },

  getVisitorsInside() {
    return Api.get('/visitors/inside');
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
    return Api.post(`/visitors/${visitorId}/exit`);
  },

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
