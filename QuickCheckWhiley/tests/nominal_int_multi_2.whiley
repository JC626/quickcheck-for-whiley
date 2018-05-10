type nat is (int x) 
where x > 0 
where x < 5

function toNat(nat y) -> nat
	requires true:
    return y