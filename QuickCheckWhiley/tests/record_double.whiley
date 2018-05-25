type board is {flipPoint width, flipPoint height} where width.value < 10 && height.value < 5
type flipPoint is {int value, bool positive} where value >= 0

function validBoard(board b) -> bool
	requires true:
	return b.width.positive && b.height.positive
