document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  document.querySelectorAll('.like-btn').forEach(function(btn) {
    btn.addEventListener('click', function (e) {
      const prototypeId = btn.getAttribute('data-prototype-id');
      fetch(`/prototype/${prototypeId}/like-from-index`, {
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
        // ボタンの色・クラス切替
        btn.classList.toggle('liked', data.liked);

        // 画像切替
        const img = btn.querySelector('img');
        img.src = data.liked ? '/images/like_btn_liked.png' : '/images/like_btn.png';

        // いいね数切替
        btn.querySelector('.like-count').textContent = data.likeCount;
      })
      .catch(e => alert('通信に失敗しました'));
    });
  });
});
