window.addEventListener('DOMContentLoaded', function() {
  var chat = document.querySelector('.chat-container');
  if (chat) {
    chat.scrollTop = chat.scrollHeight;
  }
});