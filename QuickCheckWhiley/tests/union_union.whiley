type nat is (int x) where x >= 0
type number is (nat | int b) where b < 2
type anotherNum is (number | int num) where num < 5

function addOne(anotherNum n) -> int
    requires true:
    return n+1
