type point is {int x, int y}
type cell is {int x, int y}

function inCell(point a, cell b) -> bool
	requires a.x >= 0 && a.y >= 0:
	return a.x == b.x && a.y == b.y