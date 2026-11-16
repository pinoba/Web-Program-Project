/* ===== 밥심 공통 JS ===== */
const CTX = document.body.dataset.ctx || '';

/** 로그인이 필요한 AJAX 요청이 401 이면 로그인 화면으로 이동 */
function goLogin() {
  location.href = CTX + '/login?redirect=' + encodeURIComponent(location.pathname + location.search);
}

/** POST (form 형식) → JSON */
async function postForm(url, data) {
  const res = await fetch(CTX + url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
    body: new URLSearchParams(data)
  });
  if (res.status === 401) {
    goLogin();
    return null;
  }
  const json = await res.json();
  if (!res.ok || json.ok === false) {
    throw new Error(json.message || '요청을 처리하지 못했습니다.');
  }
  return json;
}

/** GET → JSON */
async function getJson(url) {
  const res = await fetch(CTX + url, { headers: { 'Accept': 'application/json' } });
  if (!res.ok) throw new Error('요청을 처리하지 못했습니다.');
  return res.json();
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, ch => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[ch]));
}

/** 화면 아래 잠깐 뜨는 알림 */
let toastTimer;
function toast(message) {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = message;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 2000);
}

