function change (){
  const bannerItems = document.getElementsByClassName("li");
  let currentIndex = 0;

  Array.from(bannerItems).forEach((item, index) => {
    item.style.opacity = index === 0 ? "1" : "0";
    item.style.transition = "opacity 1s ease-in-out"
  });

  function changeSlide() {
    // 現在のスライドを非表示にする
    bannerItems[currentIndex].style.opacity = "0";

    // 次のスライドに切り替える
    currentIndex = (currentIndex + 1) % bannerItems.length;
    bannerItems[currentIndex].style.opacity = "1";
  }

  // 3秒ごとにスライドを切り替える
setInterval(changeSlide, 5000);

};

window.addEventListener('load', change);