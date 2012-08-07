function getCircleToPath(x, y, r) {
  var s = "M";
  s = s + "" + (x) + "," + (y-r) + "A"+r+","+r+",0,1,1,"+(x-0.1)+","+(y-r)+"z";
  return s;
}

function getArrowPath(x, y, width) {
  var startx = x - width/4
      starty = y + width/2
  var s = "M"
  s = s + startx + "," + starty + "l-" + width/4 + ",0 " + width/2 + "," + -width/2 + " "
  s = s + width/2 + "," + width/2 + " " + -width/4 + ",0 0," + width/2
  s = s + -width/2 + ",0 0," + -width/2 + "z"
  return s;
}

window.onload = function(){
  var paper = new Raphael("pseudologo", 220, 60),
      // shape = paper.circle(80, 140, 50),
      circlepath = getCircleToPath(110, 30, 30)
      secArrow   = getArrowPath(110, 35, 20)
      rectpath   = "M180,30l0,30 -140,0 0,-30z",
      groovy = { stroke: "none", fill: Raphael.hsl(117, 53, 76), cursor: "pointer" },
      arrow = { fill: "white", stroke: "none", opacity: 0 }

    // var rect   = paper.path(path2).attr(dashed)

    // shape.attr(groovy)
    // shape.hover(
    //   function()  { shape.animate({fill: Raphael.hsl(297, 53, 76)}, 250) },
    //   function () { shape.animate({fill: Raphael.hsl(117, 53, 76)}, 250) });

    var el = paper.path(circlepath).attr(groovy),
        elattrs = [{path: rectpath}, {path: circlepath}],
        now = 1;

    var uparrow = paper.path(secArrow).attr(arrow),
        uparrowattrs = [{opacity: 0.5}, {opacity: 0}],
        othernow = 1;

    var barVisible = false

    function showCircle(){
      uparrow.stop().animate(uparrowattrs[+(othernow = !othernow)], 50)
      el.stop().animate(elattrs[+(now = !now)], 300)
      $('#pseudologo').css({'position': '', 'margin-top': '90px'})
      $('nav ul').css({'margin-top': '30px'})
      el.click(function() { location = "/"; this.style.curser = 'pointer'; })
    }
    function showBar(){
      el.stop().animate(elattrs[+(now = !now)], 300)
      uparrow.stop().animate(uparrowattrs[+(othernow = !othernow)], 50)
      $('#pseudologo').css({'position': 'fixed', 'margin-top': '-30px'})
      $('nav ul').css({'margin-top': '186px'})
      el.click(function() {
        var destination = $('body').offset().top;
        $("html:not(:animated),body:not(:animated)").animate({ scrollTop: destination-20}, 500 );
        return false;
      })
    }

    $('#pseudologo').waypoint({
      handler: function(event, direction) {
        if (direction === "down"){
          showBar()
          console.log('#pseudologo triggered waypoint in downwards direction')
        }
        event.stopPropagation()
      },
      offset: 30,
    })

    $('nav').waypoint({
      handler: function(event, direction) {
        if (direction === "up") {
          showCircle()
          console.log('<nav> triggered waypoint in upwards direction')
        }
      },
      offset: -157,
    });
}
