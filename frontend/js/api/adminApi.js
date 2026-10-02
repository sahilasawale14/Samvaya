// ============================================================================
// ADMIN API CLIENT (Samvaya Unified Portal)
// ============================================================================

const AdminApi = {
  getDashboard() {
    return Api.get('/admin/dashboard');
  },

  getResidents(type = '') {
    return Api.get(`/admin/residents${type ? `?type=${type}` : ''}`);
  },

  createResident(residentData) {
    return Api.post('/admin/residents', residentData);
  },

  offboardResident(residentId, data = {}) {
    return Api.post(`/admin/residents/${residentId}/offboard`, data);
  },

  updateResident(residentId, data) {
    return Api.put(`/admin/residents/${residentId}`, data);
  },

  deleteResident(residentId) {
    return Api.delete(`/admin/residents/${residentId}`);
  },

  getOwners() {
    return Api.get('/admin/owners');
  },

  getFlats() {
    return Api.get('/admin/flats');
  },

  createFlat(flatData) {
    return Api.post('/admin/flats', flatData);
  },

  getUsers() {
    return Api.get('/admin/users');
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

  assignParkingSlot(slotId, flatId) {
    return Api.post(`/parking/assign?slotId=${slotId}&flatId=${flatId}`);
  },

  vacateParkingSlot(slotId) {
    return Api.post(`/parking/vacate?slotId=${slotId}`);
  },

  vacateParkingForFlat(flatId) {
    return Api.post(`/parking/vacate-flat/${flatId}`);
  },

  // Maintenance Billing & Financials
  getBills() {
    return Api.get('/payments/bills');
  },

  getPayments() {
    return Api.get('/payments');
  },

  generateMonthlyBills(month, ratePerSqFt = 3.50) {
    return Api.post(`/admin/bills/generate-monthly?month=${encodeURIComponent(month)}&ratePerSqFt=${ratePerSqFt}`);
  },

  // Complaints Desk
  getComplaints() {
    return Api.get('/complaints');
  },

  updateComplaintStatus(id, status, staffId = null, remarks = '') {
    const params = new URLSearchParams({ status });
    if (staffId) params.append('staffId', staffId);
    if (remarks) params.append('remarks', remarks);
    return Api.put(`/complaints/${id}/status?${params.toString()}`);
  },

  deleteComplaint(id) {
    return Api.delete(`/complaints/${id}`);
  },

  // Notices
  getNotices() {
    return Api.get('/notices');
  },

  createNotice(noticeData) {
    return Api.post('/notices', noticeData);
  },

  deleteNotice(id) {
    return Api.delete(`/notices/${id}`);
  },

  getDocuments() {
    return Api.get('/notices/documents');
  },

  // Amenities
  getAmenities() {
    return Api.get('/amenities');
  },

  createAmenity(amenityData) {
    return Api.post('/amenities', amenityData);
  },

  deleteAmenity(id) {
    return Api.delete(`/amenities/${id}`);
  },

  getBookings() {
    return Api.get('/amenities/bookings');
  },

  // Security & Incidents
  getVisitors() {
    return Api.get('/visitors');
  },

  getDeliveries() {
    return Api.get('/deliveries');
  },

  getIncidents() {
    return Api.get('/incidents');
  },

  updateIncidentStatus(id, status, notes = '') {
    const params = new URLSearchParams({ status });
    if (notes) params.append('resolutionNotes', notes);
    return Api.put(`/incidents/${id}/status?${params.toString()}`);
  }
};
