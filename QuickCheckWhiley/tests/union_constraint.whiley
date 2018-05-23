type nat is (int x) where x >= 0
type number is (nat | int b) where b < 10

function addOne(number n) -> int
    requires true:
    return n+1
