// ============================================================================
// ADMIN API CLIENT
// ============================================================================

const AdminApi = {
  getDashboard() {
    return Api.get('/admin/dashboard');
  },

  getResidents(type = '') {
    return Api.get(`/admin/residents${type ? `?type=${type}` : ''}`);
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

  getBills() {
    return Api.get('/payments/bills');
  },

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

  getIncidents() {
    return Api.get('/incidents');
  },

  updateIncidentStatus(id, status, notes = '') {
    const params = new URLSearchParams({ status });
    if (notes) params.append('resolutionNotes', notes);
    return Api.put(`/incidents/${id}/status?${params.toString()}`);
  }
};
