function foo(int a) -> (int r)
requires a >= 0:
    int b = square(a)
    return b + 1
    
function square(int x) -> (int r)
requires x >= 0
ensures r == x * x:
    return x * x
