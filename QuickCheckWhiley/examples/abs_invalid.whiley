function abs(int x) -> (int r)
// Return value cannot be negative
ensures r >= 0
// Return value is either x or its negation
ensures r == x || r == -x:
    return x