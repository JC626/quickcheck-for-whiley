type nat is (int x) where x > 0
type tens is (nat y) where y % 10 == 0

function isTen(nat a) -> tens
requires true:
    return a
		