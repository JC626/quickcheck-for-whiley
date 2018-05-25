type nat is (int x) where x >= 0
type number is (nat | int b) where b < 10
type anotherNum is (number a) where a < 5

function addOne(anotherNum n) -> int
    requires true:
    return n+1
