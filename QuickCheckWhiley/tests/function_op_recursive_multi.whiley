function sum(int a) -> (int r)
requires a >= 1
ensures (r == 1 && a == 1) || r == a + other(a-1):
    if a == 1:
        return 1
    return a + other(a - 1)
	
function other(int b) -> (int a)
requires b >= 1
ensures a == sum(b):
	return sum(b)