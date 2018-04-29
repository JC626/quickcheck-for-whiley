type counter is {bool negate, int x}

function addCount(counter a) -> counter
requires true:
	return {negate: a.negate, x: a.x + 1}