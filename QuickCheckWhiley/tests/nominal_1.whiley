type nat is (int x) where x > 0

function toNat(int y) -> nat
	requires y > 0:
    return y