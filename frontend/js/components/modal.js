// ============================================================================
// MODAL CONTROLLER COMPONENT
// ============================================================================

const Modal = {
  open(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
    }
  },

  close(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('active');
    }
  },

  setupBackdropClose() {
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
      backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) {
          backdrop.classList.remove('active');
        }
      });
    });
  },

  confirm(options = {}) {
    const title = options.title || 'Are you sure?';
    const message = options.message || 'This action cannot be undone.';
    const confirmText = options.confirmText || 'Delete';
    const danger = options.danger !== false;
    const onConfirm = options.onConfirm;

    let confirmModal = document.getElementById('global-confirm-modal');
    if (!confirmModal) {
      confirmModal = document.createElement('div');
      confirmModal.id = 'global-confirm-modal';
      confirmModal.className = 'modal-backdrop';
      document.body.appendChild(confirmModal);
    }

    confirmModal.innerHTML = `
      <div class="modal-container" style="max-width:420px; text-align:center; padding:24px; z-index:99999;">
        <div style="width:52px; height:52px; border-radius:50%; background:${danger ? '#fce8e6' : '#e6f4ea'}; color:${danger ? '#c5221f' : '#137333'}; display:flex; align-items:center; justify-content:center; margin:0 auto 16px auto;">
          <span class="material-symbols-outlined" style="font-size:28px;">${danger ? 'delete_forever' : 'help_outline'}</span>
        </div>
        <h3 style="font-size:18px; margin-bottom:8px; color:var(--on-surface); font-weight:700;">${title}</h3>
        <p style="font-size:14px; color:var(--on-surface-variant); margin-bottom:24px; line-height:1.5;">${message}</p>
        <div style="display:flex; gap:12px; justify-content:center;">
          <button type="button" class="btn btn-ghost" id="confirm-modal-cancel-btn" style="flex:1;">Cancel</button>
          <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'}" id="confirm-modal-action-btn" style="flex:1; background:${danger ? '#c5221f' : 'var(--primary)'}; color:#fff; font-weight:600;">${confirmText}</button>
        </div>
      </div>
    `;

    confirmModal.classList.add('active');

    const cancelBtn = confirmModal.querySelector('#confirm-modal-cancel-btn');
    const actionBtn = confirmModal.querySelector('#confirm-modal-action-btn');

    const handleClose = () => {
      confirmModal.classList.remove('active');
    };

    cancelBtn.onclick = handleClose;
    actionBtn.onclick = async () => {
      handleClose();
      if (typeof onConfirm === 'function') {
        await onConfirm();
      }
    };
  }
};

window.Modal = Modal;

document.addEventListener('DOMContentLoaded', () => {
  Modal.setupBackdropClose();
});
