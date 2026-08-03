// TICKET-ADV100 — theme toggle persisted to localStorage with zero-FOUC paint.
(function () {
  const stored = localStorage.getItem('reconx-theme') || 'light';
  document.documentElement.setAttribute('data-theme', stored);

  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('theme-toggle');
    if (!btn) return;

    const applyTheme = (next) => {
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('reconx-theme', next);
      btn.setAttribute('aria-pressed', String(next === 'dark'));
    };

    applyTheme(document.documentElement.getAttribute('data-theme') || 'light');
    btn.addEventListener('click', () => {
      const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      applyTheme(next);
    });
  });
})();
