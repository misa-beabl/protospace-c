document.addEventListener('DOMContentLoaded', function() {
  const fileInput = document.getElementById('avatar');
  const previewImg = document.getElementById('preview-avatar');
  if(fileInput && previewImg) {
    fileInput.addEventListener('change', function(e) {
      const file = e.target.files[0];
      if (file) {
        previewImg.style.display = "block";
        previewImg.src = URL.createObjectURL(file);
      } else {
        previewImg.src = "";
        previewImg.style.display = "none";
      }
    });
  }
});
