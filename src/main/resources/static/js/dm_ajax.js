document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  const form = document.getElementById('dm-form');
  const chatContainer = document.querySelector('.chat-container');

  function scrollChatBottom() {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }

  if (form) {
    form.addEventListener('submit', function(e) {
      e.preventDefault();

      const textArea = form.querySelector('textarea[name="text"]');
      const text = textArea.value.trim();
      if (!text) return;

      // CSRF対応
      const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
      };
      if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

      // Ajax送信
      fetch(form.getAttribute('action'), {
        method: 'POST',
        headers: headers,
        body: 'text=' + encodeURIComponent(text),
        credentials: 'same-origin'
      })
      .then(r => r.json())
      .then(data => {
        if (data.status === 'ok' && data.message) {
          // 追加HTMLを動的生成してチャットエリア末尾に追加
          const msg = data.message;
          const el = document.createElement('div');
          el.className = "chat-bubble right";
          el.innerHTML = `
            <img src="${msg.avatar}" class="chat-avatar" alt="自分アイコン">
            <div>
              <div class="chat-user-name">${msg.nickname}</div>
              <div class="chat-message">${escapeHtml(msg.text)}</div>
              <div class="chat-date">${msg.sentAt}</div>
            </div>
          `;
          chatContainer.appendChild(el);
          textArea.value = "";
          scrollChatBottom();
        } else if (data.errors) {
          // バリデーションエラー表示 (例: <ul id="dm-errors"></ul>でも可)
          alert(data.errors.join('\n'));
        }
      })
      .catch(e => { alert('送信に失敗しました'); });
    });
  }

  // 画面ロード時に下までスクロール
  scrollChatBottom();

  // HTMLエスケープ
  function escapeHtml(str) {
    return str.replace(/[&<>"']/g, function(match) {
      return ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
      })[match];
    });
  }
});