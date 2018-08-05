type point is {int x, int y}
type Fun is (function() -> (point) a)

function map(Fun fn) -> point:
    return fn()
