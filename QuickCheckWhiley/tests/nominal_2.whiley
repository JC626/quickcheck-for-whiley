type nat is (int x) where x > 0

function square(nat y) -> (nat r)
ensures r == y*y:
    return y*y