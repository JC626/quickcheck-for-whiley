type nat is (int x) where x > 0
type bool2 is (bool y)
type numArr is (int [] z) where |z| > 1


function f(nat a, bool2 b, numArr c) -> bool
requires true:
    return a < 10 && b
		