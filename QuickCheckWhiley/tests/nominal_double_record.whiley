type nat is (int x) where x > 0
type cell is {nat x, nat y} where x < 5 && y < 8

function withinBounds(cell input, cell bounds) -> bool
requires true:
    return input.x < bounds.x && input.y < bounds.y
		