document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  // いいねリストの再描画関数
  function renderLikedList(prototypes) {
    const list = document.getElementById('liked-list');
    if (!list) return;
    list.innerHTML = '';
    if (!prototypes || prototypes.length === 0) {
      list.innerHTML = '<p>まだいいねしていません。</p>';
      return;
    }
    prototypes.forEach(prototype => {
      const el = document.createElement('div');
      el.className = 'card prototype_card';
      el.innerHTML = `
        <a href="/prototype/${prototype.id}">
          <img class="card__img" src="${prototype.image}">
        </a>
        <div class="card__body">
          <a class="card__title" href="/prototype/${prototype.id}">${prototype.name}</a>
          <p class="card__summary">${prototype.slogan || ''}</p>
          <div class="card__like">
            <button
              type="button"
              class="like-btn liked"
              data-prototype-id="${prototype.id}"
              data-action="/prototype/${prototype.id}/like-from-u-detail"
            >
              <img src="/images/like_btn_liked.png" alt="いいね" class="heart-icon" />
              <span class="like-count">${prototype.likeCount}</span>
            </button>
          </div>
          <a class="card__user" href="/users/${prototype.user.id}">by ${prototype.user.nickname}</a>
        </div>
      `;
      list.appendChild(el);
    });
    setLikeBtnEvent('#liked-list .like-btn');
  }

  // 引数でセレクタをコントロールできるようにする
  function setLikeBtnEvent(selector = '.like-btn') {
    document.querySelectorAll(selector).forEach(function(btn) {
      // 二重イベント防止
      btn.replaceWith(btn.cloneNode(true));
    });
    document.querySelectorAll(selector).forEach(function(btn) {
      btn.addEventListener('click', function (e) {
        // どのボタンでも「POSTしてからリスト再取得」
        const endpoint = btn.getAttribute('data-action') ||
          `/prototype/${btn.getAttribute('data-prototype-id')}/like-from-index`;

        fetch(endpoint, {
          method: 'POST',
          headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
          },
          credentials: 'same-origin'
        })
        .then(response => response.json())
        .then(data => {
          // ボタン自身のUI更新
          btn.classList.toggle('liked', data.liked);
          const img = btn.querySelector('img');
          img.src = data.liked ? '/images/like_btn_liked.png' : '/images/like_btn.png';
          btn.querySelector('.like-count').textContent = data.likeCount;

          // 2. 同じprototypeIdの他ボタンもUIを同期
          const prototypeId = btn.getAttribute('data-prototype-id');
          document.querySelectorAll(`.like-btn[data-prototype-id="${prototypeId}"]`).forEach(otherBtn => {
            // ボタン自身以外も同じ値で更新
            otherBtn.classList.toggle('liked', data.liked);
            otherBtn.querySelector('img').src =
              data.liked ? '/images/like_btn_liked.png' : '/images/like_btn.png';
            otherBtn.querySelector('.like-count').textContent = data.likeCount;
          });
          
          // 追加 いいねリストも更新
          fetch('/prototype/liked-prototypes', {
            method: 'GET',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
          })
          .then(res => res.json())
          .then(renderLikedList);
        })
        .catch(e => alert('通信に失敗しました'));
      });
    });
  }

  setLikeBtnEvent(); // 初回ページ全体
});