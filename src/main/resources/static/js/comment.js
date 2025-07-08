document.addEventListener("DOMContentLoaded", function() {

  bindCommentFormSubmit(document.getElementById('form-new'));

  function bindCommentFormSubmit(form) {
    if (!form) return;
    form.addEventListener('submit', function(e) {
      e.preventDefault();
      const prototypeId = form.action.match(/prototypes\/(\d+)/)[1];
      const formData = new FormData(form);

      fetch(form.action, {
        method: 'POST',
        body: formData,
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      })
      .then(resp => {
        if (resp.status === 403) {
          alert('権限がありません');
          return '';
        }
        return resp.text();
      })
      .then(html => {
        if (!html) return;

        if (html.includes('class="comment-item"')) {
          
          const parentId = form.querySelector('input[name="parentId"]')?.value;
          if (parentId && parentId !== '' && parentId !== 'null') {
            
            const parentBlock = document.getElementById(`comment-container-${parentId}`);
            if (parentBlock) {
              
              let children = parentBlock.querySelector('.comment-children');
              if (!children) {
                children = document.createElement('div');
                children.className = 'comment-children';
                parentBlock.appendChild(children);
              }
              children.insertAdjacentHTML('beforeend', html);
            }
            form.remove();
          } else {
            
            document.querySelector('.comments').insertAdjacentHTML('beforeend', html);

            const mainCommentForm = document.getElementById('form-new');
            if (mainCommentForm) mainCommentForm.reset();
          
            fetch(`/prototypes/${prototypeId}/comment-form-fragment`, {
              headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(resp2 => resp2.text())
            .then(formHtml => {
              document.getElementById('comment-form-container').innerHTML = formHtml;
              bindCommentFormSubmit(document.getElementById('form-new'));
            });
          }
          
        } else {
          
          const tmp = document.createElement('div');
          tmp.innerHTML = html;
          const newForm = tmp.querySelector('form');
          form.replaceWith(newForm);
          bindCommentFormSubmit(newForm);
        }
      })
      .catch(err => {
        alert('コメントの送信でエラーが発生しました');
        console.error(err);
      });
    });
  }
    
  document.querySelector('.comments').addEventListener('click', function(e) {
    if (e.target.classList.contains('comment-reply')) {
      document.querySelectorAll('form[id^="reply-form-"]').forEach(f => f.remove());

      const commentId = e.target.getAttribute('data-comment-id');
      const prototypeId = e.target.getAttribute('data-prototype-id');
      
      // if (document.getElementById(`reply-form-${commentId}`)) return;

      fetch(`/prototypes/${prototypeId}/comment-reply-form-fragment/${commentId}`, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      })
      .then(resp => resp.text())
      .then(html => {
        const temp = document.createElement('div');
        temp.innerHTML = html;
        const form = temp.querySelector('form');
        form.id = `reply-form-${commentId}`;
        
        let parentInput = form.querySelector('input[name="parentId"]');
        if (parentInput) {
          parentInput.value = commentId;
        } else {
          parentInput = document.createElement('input');
          parentInput.type = 'hidden';
          parentInput.name = 'parentId';
          parentInput.value = commentId;
          form.appendChild(parentInput);
        }
        
        form.querySelector('textarea')?.focus();

        let block = document.getElementById(`comment-container-${commentId}`);
        let replyFormSpace = block.querySelector('.reply-form-space');
        if (!replyFormSpace) {
          alert('reply-form-space for commentId='+commentId + " not found");
          return;
        }
        replyFormSpace.innerHTML = '';
        replyFormSpace.appendChild(form);

        bindCommentFormSubmit(form);
      });
    }
  });

  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  document.querySelector('.comments').addEventListener('click', function(e) {
    
    // if (e.target.classList.contains('comment-edit')) {
    //   e.preventDefault();
    //   const commentId = e.target.getAttribute('data-comment-id');
    //   fetch(`/comment/${commentId}/edit-form`, {
    //     headers: { [csrfHeader]: csrfToken }
    //   })
    //   .then(resp => resp.text())
    //   .then(html => {
    //     document.getElementById(`comment-container-${commentId}`).innerHTML = html;
        
    //     bindCommentFormSubmit(document.getElementById(`form-${commentId}`));
    //   });
    // }

    if (e.target.classList.contains('comment-edit')) {
      e.preventDefault();
      const commentId = e.target.getAttribute('data-comment-id');
      fetch(`/comment/${commentId}/edit-form`, {
        headers: { [csrfHeader]: csrfToken }
      })
      .then(resp => resp.text())
      .then(html => {
        const container = document.getElementById(`comment-container-${commentId}`);
        if (!container) return;
        const commentItem = container.querySelector('.comment-item');
        const temp = document.createElement('div');
        temp.innerHTML = html;
        const form = temp.querySelector('form');
        if (commentItem && form) {
          commentItem.replaceWith(form);
          bindCommentFormSubmit(form);
        }
      });
    }

    if (e.target.classList.contains('reply-cancel-btn')) {
      e.preventDefault();
      const form = e.target.closest("form");
      const replyFormSpace = form && form.parentElement;
      if (replyFormSpace && replyFormSpace.classList.contains('reply-form-space')) {
        replyFormSpace.innerHTML = "";
      }
    }
  
    // if (e.target.classList.contains('comment-edit-cancel')) {
    //   e.preventDefault();
      
    //   const commentId = e.target.id.replace('cancel-', '');
    //   fetch(`/comment/${commentId}/item`)
    //   .then(resp => resp.text())
    //   .then(html => {
    //     document.getElementById(`comment-container-${commentId}`).innerHTML = html;
    //   });
    // }

    if (e.target.classList.contains('comment-edit-cancel')) {
      e.preventDefault();
      const commentId = e.target.id.replace('cancel-', '');
      fetch(`/comment/${commentId}/item`)
      .then(resp => resp.text())
      .then(html => {
        const container = document.getElementById(`comment-container-${commentId}`);
        if (!container) return;
        const formEl = container.querySelector('form');
        const temp = document.createElement('div');
        temp.innerHTML = html;
        const commentItem = temp.querySelector('.comment-item');
        if (formEl && commentItem) {
          formEl.replaceWith(commentItem);
        }
      });
    }    

    if (e.target.classList.contains('comment-delete')) {
      const commentId = e.target.getAttribute('data-comment-id');
      const prototypeId = e.target.getAttribute('data-prototype-id');
      if (confirm('本当に削除しますか？')) {
        fetch(`/prototype/${prototypeId}/comments/${commentId}/delete`, {
          method: 'POST',
          headers: {
            [csrfHeader]: csrfToken,
            'X-Requested-With': 'XMLHttpRequest'
          }
        })
        .then(resp => resp.json())
        .then(json => {
          if (json.success) {
            
            const container = document.getElementById(`comment-container-${commentId}`);
            if (container) container.remove();
          } else {
            alert('削除に失敗しました: ' + (json.error || ''));
          }
        });
      }
    }
  });

 
  document.addEventListener('submit', function(e) {
    if (e.target.matches('form[id^="form-"]')) {
      const form = e.target;
      const submitLabel = form.querySelector('input[type="submit"]').value;
      
      // if (submitLabel === 'UPDATE') {
      //   e.preventDefault();
      //   const commentId = form.id.replace('form-', '');
      //   const formData = new FormData(form);
      //   fetch(`/comment/${commentId}/edit`, {
      //     method: 'POST',
      //     headers: { [csrfHeader]: csrfToken },
      //     body: formData
      //   })
      //   .then(resp => {
      //     if (resp.status == 403) {
      //       alert('編集権限がありません');
      //       fetch(`/comment/${commentId}/item`)
      //         .then(r => r.text())
      //         .then(html => {
      //           document.getElementById(`comment-container-${commentId}`).innerHTML = html;
      //         });
      //       throw new Error('編集権限がありません');
      //     }
      //     return resp.text();
      //   })
      //   .then(html => {
      //     document.getElementById(`comment-container-${commentId}`).innerHTML = html;
      //   })
      // .catch(err => {
      //   if (err.message !== '編集権限がありません') {
      //     alert('コメント編集処理時にエラーが発生しました');
      //     console.error(err);
      //   }
      // });

      if (submitLabel === 'UPDATE') {
        e.preventDefault();
        const commentId = form.id.replace('form-', '');
        const formData = new FormData(form);
        fetch(`/comment/${commentId}/edit`, {
          method: 'POST',
          headers: { [csrfHeader]: csrfToken },
          body: formData
        })
        .then(resp => {
          if (resp.status == 403) {
            alert('編集権限がありません');
            fetch(`/comment/${commentId}/item`)
              .then(r => r.text())
              .then(html => {
                // 下面一样：只替换掉表单为普通评论item
                const container = document.getElementById(`comment-container-${commentId}`);
                if (!container) return;
                const formEl = container.querySelector('form');
                const temp = document.createElement('div');
                temp.innerHTML = html;
                const commentItem = temp.querySelector('.comment-item');
                if (formEl && commentItem) {
                  formEl.replaceWith(commentItem);
                }
              });
            throw new Error('編集権限がありません');
          }
          return resp.text();
        })
        .then(html => {
          // 只替换掉表单为普通评论item
          const container = document.getElementById(`comment-container-${commentId}`);
          if (!container) return;
          const formEl = container.querySelector('form');
          const temp = document.createElement('div');
          temp.innerHTML = html;
          const commentItem = temp.querySelector('.comment-item');
          if (formEl && commentItem) {
            formEl.replaceWith(commentItem);
          }
        })
        .catch(err => {
          if (err.message !== '編集権限がありません') {
            alert('コメント編集処理時にエラーが発生しました');
            console.error(err);
          }
        });
      }      
    }
  

  window.showAllReplies = function(btn) {
    var hidden = btn.nextElementSibling;
    if (hidden) {
      hidden.style.display = "";
      btn.style.display = "none";
    }
  };

});

