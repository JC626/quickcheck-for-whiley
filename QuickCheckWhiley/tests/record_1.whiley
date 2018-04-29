type point is {int x, int y}


function addOne(point pt) -> point
	requires pt.x > 0 && pt.y > 0:
	return {x:pt.x + 1, y:pt.y + 1}