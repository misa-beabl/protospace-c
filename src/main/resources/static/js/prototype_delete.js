document.addEventListener('DOMContentLoaded', function () {
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  
  let prototypeId = null;

  document.querySelectorAll('.delete-btn').forEach(btn => {
    btn.addEventListener('click', function () {
      prototypeId = this.getAttribute('data-prototype-id') 
        || this.getAttribute('data-action')?.split('/')[2];
      document.getElementById('delete-modal').style.display = 'block';
      document.getElementById('modal-mask').style.display = 'block';
    });
  });

  document.getElementById('confirm-delete').addEventListener('click', function () {
    if (!prototypeId) return;
    fetch(`/prototype/${prototypeId}/delete`, {
      method: 'POST',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        [csrfHeader]: csrfToken
      }
    })
    .then(resp => resp.json())
    .then(res => {
      if (res.success && res.redirectUrl) {
        window.location.href = res.redirectUrl;
      } else {
        alert("删除失败：" + (res.error || "未知错误"));
      }
    })
    .catch(err => alert("删除异常：" + err))
    .finally(() => {
      document.getElementById('delete-modal').style.display = 'none';
      document.getElementById('modal-mask').style.display = 'none';
      prototypeId = null;
    });
  });

  document.getElementById('cancel-delete').addEventListener('click', function () {
    document.getElementById('delete-modal').style.display = 'none';
    document.getElementById('modal-mask').style.display = 'none';
    prototypeId = null;
  })
});
