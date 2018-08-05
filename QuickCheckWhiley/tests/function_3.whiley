type nat is (int x) where x >= 0

type Fun is (function(int) -> (nat) a)

function map(Fun fn) -> int:
    return fn(2)
