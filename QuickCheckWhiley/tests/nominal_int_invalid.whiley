type nat is (int x) where x > 0 && x < 0

function toNat(nat y) -> nat
	requires true:
    return y