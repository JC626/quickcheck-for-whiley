type nat is (int x) where x > 0
type bool2 is (bool y)
type boolArr is (bool[] z) where |z| > 1


function f(nat a, bool2 b, boolArr c) -> bool
requires true:
    return a < 10 && b
		