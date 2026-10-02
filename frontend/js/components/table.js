// ============================================================================
// DATA TABLE RENDERER COMPONENT
// ============================================================================

const Table = {
  render(containerId, columns = [], rows = [], emptyMessage = 'No records found.') {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!rows || rows.length === 0) {
      container.innerHTML = `
        <div style="text-align:center; padding: 40px; color: var(--on-surface-variant);">
          <span class="material-symbols-outlined" style="font-size:48px; color:var(--outline-variant); margin-bottom:8px;">inbox</span>
          <p style="font-size:15px; font-weight:500;">${emptyMessage}</p>
        </div>
      `;
      return;
    }

    let thHtml = columns.map(c => `<th>${c.label}</th>`).join('');
    let trHtml = rows.map(r => {
      let tdHtml = columns.map(c => {
        let val = typeof c.render === 'function' ? c.render(r) : (r[c.key] !== undefined ? r[c.key] : '-');
        return `<td>${val}</td>`;
      }).join('');
      return `<tr>${tdHtml}</tr>`;
    }).join('');

    container.innerHTML = `
      <div class="table-responsive-wrapper" style="overflow-x:auto; -webkit-overflow-scrolling:touch; width:100%; display:block;">
        <table class="data-table" style="width:100%; min-width:600px;">
          <thead>
            <tr>${thHtml}</tr>
          </thead>
          <tbody>
            ${trHtml}
          </tbody>
        </table>
      </div>
    `;
  }
};
