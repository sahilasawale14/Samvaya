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

  getComplaints(residentId) {
    return Api.get(`/complaints?residentId=${residentId}`);
  },

  createComplaint(complaintData) {
    return Api.post('/complaints', complaintData);
  },

  getBills(residentId) {
    return Api.get(`/payments/bills?residentId=${residentId}`);
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
