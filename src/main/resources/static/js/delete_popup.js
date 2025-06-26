let deleteActionUrl = null;

document.addEventListener('DOMContentLoaded', function () {
    // 绑定每个删除按钮事件
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            deleteActionUrl = this.getAttribute('data-action');
            document.getElementById('modal-mask').style.display = 'block';
            document.getElementById('delete-modal').style.display = 'block';
        });
    });

    document.getElementById('cancel-delete').onclick = function () {
        document.getElementById('modal-mask').style.display = 'none';
        document.getElementById('delete-modal').style.display = 'none';
        deleteActionUrl = null;
    };

    document.getElementById('confirm-delete').onclick = function () {
        // 构造表单并POST，模拟原来的同步行为
        if (deleteActionUrl) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = deleteActionUrl;
            document.body.appendChild(form);
            form.submit();
        }
    };
});
