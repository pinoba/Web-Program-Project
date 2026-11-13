/* ===== 관리자: API 적재 실행 + 진행 상황 폴링 ===== */
(function () {
  const btn = document.getElementById('loadBtn');
  let timer = null;

  btn.addEventListener('click', async () => {
    if (!confirmLoad()) return;
    btn.disabled = true;
    try {
      const data = await postForm('/admin/load', {});
      toast(data.message);
    } catch (e) {
      toast(e.message); // 이미 실행 중이어도 진행 상황은 계속 보여준다
    }
    poll();
  });

  function confirmLoad() {
    return window.confirm('식약처 API 에서 레시피를 받아 DB 에 저장합니다. 진행할까요?');
  }

  async function poll() {
    clearTimeout(timer);
    try {
      const s = await getJson('/admin/status');
      render(s);
      if (s.running) {
        timer = setTimeout(poll, 500);
      } else {
        btn.disabled = false;
        if (s.done) toast(s.message);
      }
    } catch (e) {
      btn.disabled = false;
    }
  }

  function render(s) {
    const pct = s.total > 0 ? Math.round(s.processed / s.total * 100) : 0;
    document.getElementById('progFill').style.width = pct + '%';
    document.getElementById('progMsg').textContent = s.message;
    document.getElementById('dbCount').textContent = s.recipeCount.toLocaleString();
    document.getElementById('stTotal').textContent = s.total.toLocaleString();
    document.getElementById('stInserted').textContent = s.inserted.toLocaleString();
    document.getElementById('stUpdated').textContent = s.updated.toLocaleString();
    document.getElementById('stFailed').textContent = s.failed.toLocaleString();
    document.getElementById('stMismatch').textContent = s.mismatch.toLocaleString();
  }

  // 화면을 다시 열었을 때 적재가 진행 중이면 이어서 표시
  if (btn.disabled) poll();
})();
