property nat(int x) where x >= 0

function abs(int x) -> (int y)
requires nat(x)
ensures (x == y) || (x == -y):
	return x