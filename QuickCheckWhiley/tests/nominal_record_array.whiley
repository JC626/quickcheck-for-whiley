type counter is {int count, bool[] arr} where |arr| > 1 && count > 0


function checkCounter(counter a) -> bool
	requires true:
	return true