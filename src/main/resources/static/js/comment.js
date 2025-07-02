document.addEventListener("DOMContentLoaded", function() {
  bindCommentFormSubmit();

  function bindCommentFormSubmit() {
    const form = document.getElementById('form-new');
    if (!form) return;
    form.addEventListener('submit', function(e) {
      e.preventDefault();

      const prototypeId = form.action.match(/prototypes\/(\d+)/)[1];
      const formData = new FormData(form);

      fetch(`/prototypes/${prototypeId}/comment`, {
        method: 'POST',
        body: formData,
        headers: { 'X-Requested-With': 'XMLHttpRequest' } // optional
      })
      .then(resp => resp.text())
      .then(html => {
        // 判断插入片段类型
        if (html.includes('class="comment-item"')) {
          // 成功：评论插入、重建空form
          document.querySelector('.comments').insertAdjacentHTML('beforeend', html);

          // 可选：拉取最新空表单片段
          fetch(`/prototypes/${prototypeId}/comment-form-fragment`, { headers: {'X-Requested-With': 'XMLHttpRequest'} })
            .then(resp => resp.text())
            .then(formHtml => {
              document.getElementById('comment-form-container').innerHTML = formHtml;
              bindCommentFormSubmit(); // 绑定新form事件
            });

        } else {
          // 校验失败：直接替换整个form区域
          document.getElementById('comment-form-container').innerHTML = html;
          bindCommentFormSubmit(); // 绑定新form事件
        }
      })
      .catch(err => {
        alert('コメントの送信でエラーが発生しました');
        console.error(err);
      });
    });
  }
});

document.addEventListener('DOMContentLoaded', function() {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  // 编辑和取消事件（委托在 .comments 区）
  document.querySelectorAll('.comments').forEach(commentList => {
    commentList.addEventListener('click', function(e) {

      // 点击编辑按钮，加载编辑form
      if (e.target && e.target.classList.contains('comment-edit')) {
        e.preventDefault();
        const commentId = e.target.getAttribute('data-comment-id');
        if (!commentId || commentId === 'null') {
          alert('コメントIDを取得できません。');
          return;
        }
        fetch(`/comment/${commentId}/edit-form`, {
          headers: { [csrfHeader]: csrfToken }
        })
        .then(resp => resp.text())
        .then(html => {
          document.getElementById(`comment-container-${commentId}`).innerHTML = html;
        });
      }

      // 点击取消，恢复成只读item
      if (e.target && e.target.classList.contains('comment-edit-cancel')) {
        e.preventDefault();
        const commentId = e.target.id.replace('cancel-', '');
        fetch(`/comment/${commentId}/item`)
          .then(resp => resp.text())
          .then(html => {
            document.getElementById(`comment-container-${commentId}`).innerHTML = html;
          });
      }
    });
  });

  // 编辑表单提交（全局事件，兼容表单被替换）
  document.addEventListener('submit', function(e) {
    if (e.target && e.target.matches('form[id^="form-"]')) {
      const submitLabel = e.target.querySelector('input[type="submit"]').value;
      if (submitLabel === 'UPDATE') {
        e.preventDefault();
        const form = e.target;
        const commentId = form.id.replace('form-', '');
        const formData = new FormData(form);
        fetch(`/comment/${commentId}/edit`, {
          method: 'POST',
          headers: { [csrfHeader]: csrfToken },
          body: formData
        })
        .then(resp => {
          // 处理权限403弹框
          if (resp.status === 403) {
            alert('編集権限がありません');
            // 恢复为只读项
            fetch(`/comment/${commentId}/item`)
              .then(r => r.text())
              .then(html => {
                document.getElementById(`comment-container-${commentId}`).innerHTML = html;
              });
            throw new Error('編集権限がありません');
          }
          return resp.text();
        })
        .then(html => {
          // 直接更新编辑区内容，无论出错还是成功
          document.getElementById(`comment-container-${commentId}`).innerHTML = html;
        })
        .catch(err => {
          if (err.message !== '編集権限がありません') {
            alert('コメント編集処理時にエラーが発生しました');
            console.error(err);
          }
        });
      }
    }
  });
});