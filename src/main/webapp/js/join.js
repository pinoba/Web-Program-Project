/* ===== 회원가입: 아이디 중복 확인(AJAX) + 입력값 검사 ===== */
(function () {
  const form = document.getElementById('joinForm');
  const userId = document.getElementById('userId');
  const idMsg = document.getElementById('idMsg');
  const joinMsg = document.getElementById('joinMsg');
  let checkedId = null; // 중복 확인을 통과한 아이디

  document.getElementById('idCheckBtn').addEventListener('click', async () => {
    const id = userId.value.trim();
    checkedId = null;
    if (!/^[A-Za-z0-9]{4,20}$/.test(id)) {
      showIdMsg('아이디는 영문·숫자 4~20자로 입력하세요.', false);
      return;
    }
    try {
      const data = await getJson('/api/idcheck?userId=' + encodeURIComponent(id));
      showIdMsg(data.message, data.available);
      if (data.available) checkedId = id;
    } catch (e) {
      showIdMsg(e.message, false);
    }
  });

  // 아이디를 바꾸면 다시 확인해야 함
  userId.addEventListener('input', () => {
    checkedId = null;
    idMsg.textContent = '';
  });

  form.addEventListener('submit', e => {
    const pw = document.getElementById('password').value;
    const pw2 = document.getElementById('password2').value;
    const nick = document.getElementById('nickname').value.trim();
    let error = '';

    if (!userId.value.trim() || !pw || !pw2 || !nick) error = '빈 칸을 모두 입력하세요.';
    else if (checkedId !== userId.value.trim()) error = '아이디 중복확인을 해 주세요.';
    else if (pw.length < 6) error = '비밀번호는 6자 이상 입력하세요.';
    else if (pw !== pw2) error = '비밀번호 확인이 일치하지 않습니다.';

    if (error) {
      e.preventDefault();
      joinMsg.textContent = error;
    }
  });

  function showIdMsg(text, ok) {
    idMsg.textContent = text;
    idMsg.className = 'form-msg ' + (ok ? 'ok' : 'err');
  }
})();
