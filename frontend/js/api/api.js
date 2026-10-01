// ============================================================================
// SAMVAYA CORE API CLIENT LAYER (Vanilla JS Fetch Wrapper for MySQL Backend)
// ============================================================================

const Api = {
  async request(endpoint, options = {}) {
    const url = `${CONFIG.API_BASE_URL}${endpoint}`;
    const user = getCurrentUser();

    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(user ? { 'X-User-Id': user.userId, 'X-User-Role': user.role } : {}),
      ...(options.headers || {})
    };

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      const json = await response.json();

      if (!response.ok || (json.success === false)) {
        const errorMsg = json.message || `Request failed with status ${response.status}`;
        throw new Error(errorMsg);
      }

      return json.data !== undefined ? json.data : json;
    } catch (err) {
      console.error(`[API Error] ${options.method || 'GET'} ${endpoint}:`, err);
      
      // For mutation requests (POST/PUT/DELETE), show user toast and propagate error
      if (options.method && options.method !== 'GET') {
        if (typeof Toast !== 'undefined') {
          Toast.error(err.message || 'Operation failed on server');
        }
        throw err;
      }

      // For GET requests only, return fallback if server is unreachable
      console.warn(`[API Notice] Falling back for GET ${endpoint}`);
      return this.getMockFallback(endpoint);
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  put(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body)
    });
  },

  patch(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  },

  getMockFallback(endpoint) {
    if (endpoint.includes('/admin/dashboard')) {
      return {
        totalFlats: 36,
        occupiedFlats: 34,
        vacantFlats: 2,
        totalResidents: 86,
        totalOwners: 28,
        totalTenants: 8,
        pendingComplaints: 3,
        collectedMaintenance: 143500,
        recentActivities: [],
        recentComplaints: []
      };
    }
    return [];
  }
};
