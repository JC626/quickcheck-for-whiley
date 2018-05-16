type nat is (int x) where x > 0
type ones is (nat y) where y < 10

function multiply(ones a, nat b) -> int
requires true:
    return a * b
		