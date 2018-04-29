type counter is {bool negate, int x}
type point is {int x, int y}

function addOne(point a, counter b) -> (point r, counter q)
requires true:
	return {x: a.x+1, y: a.y+1}, {negate: b.negate, x: b.x + 1}