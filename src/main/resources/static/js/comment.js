// function comment (){
//   const send = document.getElementById("send");
//   send.addEventListener("click", () => {
//     const form = document.getElementById("form");
//     const formData = new FormData(form);
//     const XHR = new XMLHttpRequest();
//     XHR.open("POST", "/prototypes/${prototypeId}/comment", true);
//     XHR.responseType = "json";
//     XHR.send(formData);
//   });
// };

// window.addEventListener('load', comment);
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