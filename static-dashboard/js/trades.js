(function () {
  const table = document.getElementById('trades-table');
  const tbody = document.getElementById('trades-tbody');
  if (!table || !tbody) return;

  let rows = [];

  function renderRows() {
    tbody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.tradeRef || ''}</td>
        <td>${row.symbol || ''}</td>
        <td>${row.quantity ?? ''}</td>
        <td>${row.price ?? ''}</td>
        <td>${row.status || ''}</td>
      </tr>
    `).join('');
  }

  table.querySelectorAll('thead th').forEach((th) => {
    th.addEventListener('click', (event) => {
      if (event.target.classList.contains('resize-handle')) return;

      const col = th.dataset.col;
      const type = th.dataset.type || 'string';
      const nextDir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';

      table.querySelectorAll('thead th').forEach((header) => header.removeAttribute('aria-sort'));
      th.setAttribute('aria-sort', nextDir);

      const multiplier = nextDir === 'ascending' ? 1 : -1;
      rows.sort((a, b) => {
        const first = a[col];
        const second = b[col];
        if (type === 'number') {
          return (Number(first) - Number(second)) * multiplier;
        }
        return String(first).localeCompare(String(second)) * multiplier;
      });

      renderRows();
    });
  });

  table.querySelectorAll('.resize-handle').forEach((handle) => {
    handle.addEventListener('mousedown', (event) => {
      event.preventDefault();
      const th = handle.closest('th');
      if (!th) return;

      const startX = event.clientX;
      const startWidth = th.offsetWidth;

      const onMove = (moveEvent) => {
        th.style.width = `${startWidth + moveEvent.clientX - startX}px`;
      };
      const onUp = () => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      };

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    });
  });

  fetch('/api/v1/trades?size=200')
    .then((response) => response.json())
    .then((data) => {
      rows = Array.isArray(data) ? data : data.content || [];
      renderRows();
    })
    .catch(() => {
      rows = [
        { tradeRef: 'demo-001', symbol: 'AAPL', quantity: 100, price: 175.2, status: 'MATCHED' },
        { tradeRef: 'demo-002', symbol: 'MSFT', quantity: 250, price: 410.6, status: 'BREAK' },
      ];
      renderRows();
    });
})();
