/* ═══════════════════════════════════════════════════════════════
   StreamVault – main.js
   ═══════════════════════════════════════════════════════════════ */

// ── Auto-dismiss alerts after 4 seconds ───────────────────────────────────
document.querySelectorAll('.alert').forEach(el => {
  setTimeout(() => {
    el.style.transition = 'opacity .5s';
    el.style.opacity = '0';
    setTimeout(() => el.remove(), 500);
  }, 4000);
});

// ── Password show/hide toggle ─────────────────────────────────────────────
document.querySelectorAll('[data-toggle-pw]').forEach(btn => {
  btn.addEventListener('click', () => {
    const input = btn.previousElementSibling;
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
    btn.textContent = input.type === 'password' ? '👁' : '🙈';
  });
});

// ── Password strength indicator (register page) ───────────────────────────
const pwInput = document.getElementById('password');
const pwStrength = document.getElementById('pw-strength');
if (pwInput && pwStrength) {
  pwInput.addEventListener('input', () => {
    const val = pwInput.value;
    let strength = 0;
    if (val.length >= 8)              strength++;
    if (/[A-Z]/.test(val))            strength++;
    if (/[0-9]/.test(val))            strength++;
    if (/[^A-Za-z0-9]/.test(val))     strength++;
    const labels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
    const colors = ['', '#e74c3c', '#f39c12', '#2ecc71', '#27ae60'];
    pwStrength.textContent = val.length ? labels[strength] : '';
    pwStrength.style.color = colors[strength];
  });
}

// ── Play Now form – auto-fill device type ─────────────────────────────────
const playForm = document.getElementById('play-form');
if (playForm) {
  const devInput = playForm.querySelector('[name="device"]');
  if (devInput) devInput.value = detectDevice();
}

function detectDevice() {
  const ua = navigator.userAgent;
  if (/Mobile|Android|iPhone/i.test(ua)) return 'Mobile';
  if (/Tablet|iPad/i.test(ua))           return 'Tablet';
  return 'Web';
}

// ── Content card click → navigate to detail page ─────────────────────────
document.querySelectorAll('.content-card[data-id]').forEach(card => {
  card.addEventListener('click', () => {
    window.location.href = 'content?id=' + card.dataset.id;
  });
});

// ── Home filter form – prevent empty searches from adding query params ─────
const filterForm = document.getElementById('filter-form');
if (filterForm) {
  filterForm.addEventListener('submit', (e) => {
    // Remove empty fields so URL stays clean
    Array.from(filterForm.elements).forEach(el => {
      if ((el.tagName === 'INPUT' || el.tagName === 'SELECT') && !el.value) {
        el.disabled = true;
      }
    });
  });
}
