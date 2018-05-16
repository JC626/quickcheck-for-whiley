type nat is (int x) where x > 0
type nat2 is (nat y) where y < 1

function toInt(nat2 a) -> int
requires true:
    return a
		