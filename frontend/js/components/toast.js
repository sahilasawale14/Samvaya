// ============================================================================
// TOAST NOTIFICATION COMPONENT
// ============================================================================

const Toast = {
  getContainer() {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }
    return container;
  },

  show(message, type = 'info', duration = 3500) {
    const container = this.getContainer();
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconName = 'info';
    if (type === 'success') iconName = 'check_circle';
    if (type === 'error') iconName = 'error';
    if (type === 'warning') iconName = 'warning';

    toast.innerHTML = `
      <span class="material-symbols-outlined" style="font-size:22px;">${iconName}</span>
      <span style="font-size:14px; font-weight:500;">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  },

  success(message) { this.show(message, 'success'); },
  error(message) { this.show(message, 'error', 4500); },
  warning(message) { this.show(message, 'warning'); },
  info(message) { this.show(message, 'info'); }
};
