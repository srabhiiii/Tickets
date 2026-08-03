// TICKET-ADV104 / ADV105 — EventSource live feed with prepend + slide-in animation.
class TradeFeed {
  constructor(feedEl, statusEl) {
    this.feedEl = feedEl;
    this.statusEl = statusEl;
    this.sse = null;
    this.demoEvents = [
      { tradeRef: 'EQU-20260603-0001', symbol: 'SAP.DE', qty: 1000, price: 125.5, status: 'MATCHED' },
      { tradeRef: 'FX-20260603-0001', symbol: 'EUR/USD', qty: 1_000_000, price: 1.0852, status: 'PENDING' },
      { tradeRef: 'EQU-20260603-0002', symbol: 'AAPL', qty: 500, price: 178.2, status: 'BREAK' },
    ];
  }

  escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  formatQty(value) {
    return new Intl.NumberFormat('en-US').format(Number(value) || 0);
  }

  formatPrice(value) {
    return new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 4 }).format(Number(value) || 0);
  }

  setStatus(text, variant) {
    if (!this.statusEl) return;
    this.statusEl.textContent = text;
    this.statusEl.className = 'connection-badge';
    if (variant) {
      this.statusEl.classList.add(`connection-badge--${variant}`);
    }
  }

  prependTradeRow(trade) {
    if (!this.feedEl) return;

    const status = String(trade.status || 'PENDING').toUpperCase();
    const statusModifier = status === 'MATCHED'
      ? 'trade-card--matched'
      : status === 'BREAK' || status === 'UNMATCHED'
        ? 'trade-card--break'
        : '';

    const row = document.createElement('article');
    row.className = ['trade-card', statusModifier, 'trade-card--new'].filter(Boolean).join(' ');
    row.innerHTML = `
      <div class="trade-card__header">
        <strong>${this.escapeHtml(trade.tradeRef || 'UNKNOWN')}</strong>
        <span>${this.escapeHtml(status)}</span>
      </div>
      <div class="trade-card__body">
        <span>${this.escapeHtml(trade.symbol || '—')}</span>
        <span>Qty ${this.escapeHtml(this.formatQty(trade.qty || 0))}</span>
        <span>Price ${this.escapeHtml(this.formatPrice(trade.price || 0))}</span>
      </div>`;

    this.feedEl.prepend(row);
    while (this.feedEl.children.length > 50) {
      this.feedEl.lastElementChild.remove();
    }

    window.setTimeout(() => row.classList.remove('trade-card--new'), 500);
  }

  renderDemoEvents() {
    this.demoEvents.forEach((event, index) => {
      window.setTimeout(() => this.prependTradeRow(event), 500 * index);
    });
  }

  connect() {
    this.renderDemoEvents();

    if (typeof window.EventSource === 'undefined') {
      this.setStatus('Demo feed', 'demo');
      return;
    }

    this.setStatus('Connecting…', 'connecting');
    this.sse = new window.EventSource('/api/v1/trades/stream');
    this.sse.onopen = () => this.setStatus('Live', 'live');
    this.sse.onmessage = (event) => {
      try {
        const trade = JSON.parse(event.data);
        this.prependTradeRow(trade);
      } catch (error) {
        console.error('Unable to parse trade payload', error);
      }
    };
    this.sse.onerror = () => this.setStatus('Reconnecting…', 'reconnecting');
    window.addEventListener('beforeunload', () => this.sse?.close());
  }
}

(function () {
  const feed = document.getElementById('trade-feed');
  const status = document.getElementById('sse-status');
  if (!feed) return;

  const tradeFeed = new TradeFeed(feed, status);
  tradeFeed.connect();
})();
