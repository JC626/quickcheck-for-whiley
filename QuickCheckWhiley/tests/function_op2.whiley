function foo(int a) -> (int r)
requires a >= 0
ensures r == a * 2:
    int ans = sum(a, a)
    return ans
    
function sum(int a, int b) -> (int r)
ensures r == a + b:
    return a + b
