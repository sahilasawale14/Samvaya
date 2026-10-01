// ============================================================================
// AUTHENTICATION API - REAL DB-BOUND LOGIN
// ============================================================================

const AuthApi = {
  async login(username, password) {
    const data = await Api.post('/auth/login', { username, password });
    if (data && (data.id || data.userId)) {
      // Save authenticated user profile to local storage
      localStorage.setItem('currentUser', JSON.stringify(data));
      localStorage.setItem(CONFIG.AUTH_STORAGE_KEY, JSON.stringify(data));
      if (data.token) {
        localStorage.setItem(CONFIG.TOKEN_STORAGE_KEY, data.token);
      }
      return data;
    }
    throw new Error('Authentication failed: No user record returned');
  },

  createUser(userData) {
    return Api.post('/auth/users', userData);
  },

  registerResident(residentData) {
    return Api.post('/auth/register-resident', residentData);
  }
};
