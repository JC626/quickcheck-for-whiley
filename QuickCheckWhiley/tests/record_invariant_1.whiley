type point is {int x, int y} where x > 0 && y < 0


function addOne(point pt) -> point
	requires true:
	return {x:pt.x + 1, y:pt.y + 1}