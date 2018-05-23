type nat is (int x) where x >= 0
type number is (nat | int b) where b < 10
type point is {number num, bool positive} where num < 5

function getPointNum(point p) -> int
    requires true:
    return p.num
