document.addEventListener('DOMContentLoaded', function() {
const openDrawerBtn = document.getElementById('openDrawerBtn');
const closeDrawerBtn = document.getElementById('closeDrawerBtn');
const drawerOverlay = document.getElementById('drawerOverlay');
const genreDrawerNav = document.getElementById('genreDrawerNav'); // nav要素に変更


// ドロワーを開く関数
function openDrawer() {
    drawerOverlay.classList.add('is-open');
    genreDrawerNav.classList.add('is-open');
    document.body.style.overflow = 'hidden'; // ページ本体のスクロールを無効化
}

// ドロワーを閉じる関数
function closeDrawer() {
    drawerOverlay.classList.remove('is-open');
    genreDrawerNav.classList.remove('is-open');
    document.body.style.overflow = ''; // ページ本体のスクロールを有効化
}

// 開くボタンのイベントリスナー
if (openDrawerBtn) {
    openDrawerBtn.addEventListener('click', openDrawer);
}

// 閉じるボタンのイベントリスナー
if (closeDrawerBtn) {
    closeDrawerBtn.addEventListener('click', closeDrawer);
}

// オーバーレイのイベントリスナー
if (drawerOverlay) {
    drawerOverlay.addEventListener('click', closeDrawer);
}

// ジャンルフォームの送信時にもドロワーを閉じる
const genreFilterForm = document.getElementById('genreFilterForm');
if (genreFilterForm) {
    genreFilterForm.addEventListener('submit', function(event) {
        // フォームが送信される直前にドロワーを閉じる
        closeDrawer();
        // フォームの通常の送信処理は続行させる（preventDefaultはしない）
    });
}
});