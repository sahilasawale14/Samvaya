// ============================================================================
// SAMVAYA CORE API CLIENT LAYER (Vanilla JS Fetch Wrapper for MySQL Backend)
// ============================================================================

const Api = {
  async request(endpoint, options = {}) {
    const url = `${CONFIG.API_BASE_URL}${endpoint}`;
    const user = getCurrentUser();

      const userHeaderId = user ? (user.userId || user.id) : null;
      const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...(userHeaderId ? { 'X-User-Id': userHeaderId, 'X-User-Role': user.role } : {}),
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

      // Return empty array/null instead of fake mock fallbacks
      return Array.isArray(err) ? [] : (endpoint.includes('dashboard') ? {} : []);
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
  }
};
