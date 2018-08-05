type Fun is (function() -> (null|int) a)

function map(Fun fn) -> null|int:
    return fn()
