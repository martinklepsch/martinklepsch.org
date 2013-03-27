$(function(){
  $(".awesome a").click(function(){
    $(".awesome a").animate({opacity: 0}, 400,
      function(){
        $(this).hide();
        $(".awesome .social").fadeIn(400);
      }
    );
    _gaq.push(['_trackEvent', 'Articles', 'Awesome', document.title]);
  });
});
