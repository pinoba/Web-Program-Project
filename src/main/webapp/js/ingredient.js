/* ===== 재료로 검색: 자동완성(AJAX) + 재료 태그 ===== */
(function () {
  const MAX = 10;
  const form = document.getElementById('ingForm');
  const input = document.getElementById('ingInput');
  const list = document.getElementById('acList');
  const tagBox = document.getElementById('tagBox');
  const msg = document.getElementById('ingMsg');

  let items = [];       // 현재 자동완성 후보
  let active = -1;      // 키보드로 고른 후보 위치
  let timer = null;
  let lastQuery = '';

  // ----- 자동완성: 입력이 멈추고 0.2초 뒤 서버에 요청 -----
  input.addEventListener('input', () => {
    clearTimeout(timer);
    const q = input.value.trim();
    if (!q) { closeList(); return; }
    timer = setTimeout(() => suggest(q), 200);
  });

  async function suggest(q) {
    lastQuery = q;
    try {
      const data = await getJson('/api/ingredients?q=' + encodeURIComponent(q));
      if (q !== lastQuery) return; // 늦게 도착한 이전 응답 무시
      items = data;
      render(q);
    } catch (e) {
      closeList();
    }
  }

  function render(q) {
    active = -1;
    if (items.length === 0) {
      list.innerHTML = '<li class="ac-empty">일치하는 재료가 없어요 · Enter 로 그대로 추가</li>';
    } else {
      const re = new RegExp('(' + q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'g');
      list.innerHTML = items.map((name, i) =>
        `<li role="option" data-index="${i}">${escapeHtml(name).replace(re, '<mark>$1</mark>')}</li>`
      ).join('');
    }
    list.hidden = false;
    input.setAttribute('aria-expanded', 'true');
  }

  function closeList() {
    list.hidden = true;
    list.innerHTML = '';
    items = [];
    active = -1;
    input.setAttribute('aria-expanded', 'false');
  }

  function highlight() {
    list.querySelectorAll('li[data-index]').forEach((li, i) => li.classList.toggle('active', i === active));
  }

  // 마우스로 후보 선택 (blur 보다 먼저 처리되도록 mousedown)
  list.addEventListener('mousedown', e => {
    const li = e.target.closest('li[data-index]');
    if (!li) return;
    e.preventDefault();
    addTag(items[Number(li.dataset.index)]);
  });

  // 키보드: ↑ ↓ 이동, Enter 추가, Esc 닫기
  input.addEventListener('keydown', e => {
    if (e.key === 'ArrowDown' && items.length) {
      e.preventDefault();
      active = (active + 1) % items.length;
      highlight();
    } else if (e.key === 'ArrowUp' && items.length) {
      e.preventDefault();
      active = (active - 1 + items.length) % items.length;
      highlight();
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const value = active >= 0 ? items[active] : input.value.trim();
      if (value) addTag(value);
      else submitSearch();
    } else if (e.key === 'Escape') {
      closeList();
    }
  });

  input.addEventListener('blur', () => setTimeout(closeList, 100));

  // ----- 재료 태그 -----
  function currentTags() {
    return [...tagBox.querySelectorAll('.tag')].map(t => t.dataset.name);
  }

  function addTag(name) {
    name = name.trim();
    msg.textContent = '';
    if (currentTags().includes(name)) {
      msg.textContent = '이미 추가한 재료예요.';
    } else if (currentTags().length >= MAX) {
      msg.textContent = `재료는 최대 ${MAX}개까지 넣을 수 있어요.`;
    } else {
      const tag = document.createElement('span');
      tag.className = 'tag';
      tag.dataset.name = name;
      tag.innerHTML = `${escapeHtml(name)}<button type="button" class="tag-x" aria-label="${escapeHtml(name)} 제거">×</button>`
                    + `<input type="hidden" name="ing" value="${escapeHtml(name)}">`;
      tagBox.appendChild(tag);
    }
    input.value = '';
    closeList();
    input.focus();
  }

  // × 를 누르면 태그 제거
  tagBox.addEventListener('click', e => {
    if (e.target.classList.contains('tag-x')) {
      e.target.closest('.tag').remove();
      msg.textContent = '';
    }
  });

  // ----- 검색: 입력 중인 글자가 있으면 태그로 넣고 전송 -----
  form.addEventListener('submit', e => {
    e.preventDefault();
    if (input.value.trim()) addTag(input.value);
    submitSearch();
  });

  function submitSearch() {
    if (currentTags().length === 0) {
      msg.textContent = '재료를 하나 이상 추가하세요.';
      input.focus();
      return;
    }
    form.submit();
  }
})();
