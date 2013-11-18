$(function(){
  $(".awesome a").click(function(){
    $(".awesome a").animate({opacity: 0}, 400,
      function(){
        $(this).remove();
        $(".awesome").load('/subscribe.html');
      }
    );
    _gaq.push(['_trackEvent', 'Articles', 'Awesome', document.title]);
  });

  $("a").on({
    "mouseenter": function() {
      var color = '#' + Math.random().toString(16).slice(2, 8);
      $(this).css({'color': color, 'border-color': color}) },
    "mouseleave": function() {
      $(this).removeAttr('style') }})

  $(".awesome").delegate('input', 'focusin mouseenter', function() {
    var color = '#' + Math.random().toString(16).slice(2, 8);
    $(this).css({'color': color, 'border-color': color}) })

  $(".awesome").delegate('input', 'focusout mouseleave', function() {
    if (!$(this).is(":focus")) {
      $(this).removeAttr('style') }})
});
