type Fun is (function() -> (int, bool) a)

function map(Fun fn) -> (int r, bool t):
    return fn()