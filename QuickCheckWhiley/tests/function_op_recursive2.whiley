function sum(int a) -> (int r)
requires a >= 1
ensures (r == 1 && a == 1) || r == a + sum(a-1):
    if a == 1:
        return 1
    return a + sum(a - 1)