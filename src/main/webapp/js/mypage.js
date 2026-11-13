/* ===== 마이페이지: 즐겨찾기 해제 (새로고침 없이 목록에서 제거) ===== */
(function () {
  const grid = document.getElementById('favGrid');

  grid.addEventListener('click', async e => {
    const btn = e.target.closest('.fav-remove');
    if (!btn) return;
    const item = btn.closest('.fav-item');
    btn.disabled = true;
    try {
      const data = await postForm('/api/favorite', { recipeId: item.dataset.recipeId, action: 'remove' });
      if (!data) return;
      item.classList.add('removing');
      setTimeout(() => {
        item.remove();
        const count = grid.querySelectorAll('.fav-item').length;
        document.getElementById('favCount').textContent = count;
        document.getElementById('favEmpty').hidden = count > 0;
      }, 200);
      toast('즐겨찾기를 해제했어요');
    } catch (err) {
      toast(err.message);
      btn.disabled = false;
    }
  });
})();
