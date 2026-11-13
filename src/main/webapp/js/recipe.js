/* ===== 음식 상세: 즐겨찾기 (새로고침 없이) ===== */
(function () {
  const favBtn = document.getElementById('favBtn');

  favBtn.addEventListener('click', async () => {
    favBtn.disabled = true;
    try {
      const data = await postForm('/api/favorite', { recipeId: favBtn.dataset.recipeId });
      if (!data) return; // 비로그인 → 로그인 화면으로 이동 중
      setFavorite(data.favorited);
      toast(data.favorited ? '즐겨찾기에 저장했어요' : '즐겨찾기를 해제했어요');
    } catch (e) {
      toast(e.message);
    } finally {
      favBtn.disabled = false;
    }
  });

  function setFavorite(on) {
    favBtn.classList.toggle('is-on', on);
    favBtn.setAttribute('aria-pressed', on);
    favBtn.querySelector('.ico').textContent = on ? '♥' : '♡';
    favBtn.querySelector('.txt').textContent = on ? '즐겨찾기됨' : '즐겨찾기';
  }
})();
