// ============================================================================
// RESIDENT API CLIENT
// ============================================================================

const ResidentApi = {
  getDashboard(residentId) {
    return Api.get(`/resident/dashboard/${residentId}`);
  },

  getProfile(residentId) {
    return Api.get(`/resident/profile/${residentId}`);
  },

  getVisitors(residentId) {
    return Api.get(`/visitors?residentId=${residentId}`);
  },

  createVisitorPass(visitorData) {
    return Api.post('/visitors', visitorData);
  },

  approveVisitor(visitorId, approved) {
    return Api.post(`/visitors/${visitorId}/approve?approved=${approved}`);
  },

  getDeliveries(residentId) {
    return Api.get(`/deliveries?residentId=${residentId}`);
  },

  createExpectedDelivery(deliveryData) {
    return Api.post('/deliveries', deliveryData);
  },

  getComplaints(residentId, userId) {
    const params = new URLSearchParams();
    if (userId) params.append('userId', userId);
    if (residentId) params.append('residentId', residentId);
    const qs = params.toString();
    return Api.get(`/resident/complaints${qs ? `?${qs}` : ''}`);
  },

  createComplaint(complaintData) {
    return Api.post('/resident/complaints', complaintData);
  },

  getBills(flatId, residentId) {
    const params = new URLSearchParams();
    if (flatId) params.append('flatId', flatId);
    if (residentId) params.append('residentId', residentId);
    const qs = params.toString();
    return Api.get(`/resident/bills/my-bills${qs ? `?${qs}` : ''}`);
  },

  payBill(billId, residentId, paymentMode = 'UPI') {
    return Api.post(`/payments/pay?billId=${billId}&residentId=${residentId}&paymentMode=${paymentMode}`);
  },

  getAmenities() {
    return Api.get('/amenities');
  },

  getBookings(residentId) {
    return Api.get(`/amenities/bookings?residentId=${residentId}`);
  },

  bookAmenity(bookingData) {
    return Api.post('/amenities/bookings', bookingData);
  },

  cancelBooking(bookingId) {
    return Api.delete(`/amenities/bookings/${bookingId}`);
  },

  getNotices() {
    return Api.get('/notices');
  },

  getDocuments() {
    return Api.get('/notices/documents');
  }
};
