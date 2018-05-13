type point is ({int x, bool isCell} p) where p.x > 0 


function addOne(point pt) -> point
	requires true:
	return {x:pt.x + 1, isCell: pt.isCell}