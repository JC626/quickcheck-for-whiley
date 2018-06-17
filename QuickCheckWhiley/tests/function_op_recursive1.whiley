function fact(int a) -> (int r)
requires a >= 1
ensures r == 1 || r == a * fact(a-1):
	if a == 1:
	    return 1
	else:
	    return a * fact(a - 1)
