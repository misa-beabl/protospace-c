document.addEventListener("DOMContentLoaded", function() {
  const form = document.getElementById('form');
  if (!form) return;
  
  form.addEventListener('submit', function(e) {
    e.preventDefault();
    const prototypeId = form.action.match(/prototypes\/(\d+)/)[1];
    const url = `/prototypes/${prototypeId}/comment`;
    const formData = new FormData(form);
    
    fetch(url, {
      method: 'POST',
      body: formData,
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      }
    })
      .then(resp => {
        if (!resp.ok) throw new Error('Network error!');
        return resp.text();
      })
      .then(html => {
        const commentsBox = document.querySelector('.comments');
        commentsBox.insertAdjacentHTML('beforeend', html);
        form.reset();
      })
      .catch(err => {
        alert('コメントの送信でエラーが発生しました');
        console.error(err);
      });
  });
});

document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  document.querySelectorAll('.comments').forEach(commentList => {
    commentList.addEventListener('click', function(e) {
      if (e.target && e.target.classList.contains('comment-delete')) {
        e.preventDefault();

        const form = e.target.closest('.delete-comment-form');
        const commentId = form.getAttribute('data-comment-id');
        const prototypeId = form.getAttribute('data-prototype-id');

        if (!confirm('本当に削除しますか？')) return;

        fetch(`/prototype/${prototypeId}/comments/${commentId}/delete`, {
          method: 'POST',
          headers: {
            'X-Requested-With': 'XMLHttpRequest',
            [csrfHeader]: csrfToken 
          }
        })
        .then(resp => {
          if (!resp.ok) throw new Error("ユーザー検証にエラーが発生します");
          return resp.json();
        })
        .then(res => {
          if (res.success) {
            const elem = document.getElementById(`comment-${commentId}`);
            if (elem) elem.remove();
          } else {
            alert('コメント削除失敗: ' + (res.error || 'サーバーエラー'));
          }
        })
        .catch(err => {
          alert('コメント削除処理中にエラーが発生しました');
          console.error(err);
        });
      }
    });
  });
});

