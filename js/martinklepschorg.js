window.onload = function(){
	var paper = new Raphael("pseudologo", 160, 100);
	var shape = paper.circle(80, 50, 30);
		shape.attr("stroke", "none");
		shape.attr("fill", Raphael.hsl(117, 53, 76));
    shape.attr("cursor", "pointer")
		shape.hover(
        function()  { shape.animate({fill: Raphael.hsl(297, 53, 76)}, 250) },
        function () { shape.animate({fill: Raphael.hsl(117, 53, 76)}, 250) });
    shape.click(function() { location = "/"; this.style.curser = 'pointer'; })
}
