type Fun is (function() -> (int) a)

function map(Fun fn) -> int:
    return fn()
