function mid(int x,int y, int z) -> (int r)
requires x != y && y != z && x != z
ensures r == x ==> (z < r && r< y) || (y < r && r< z)
ensures r == y ==> (z < y && y< x) || (x < y && y< z)
ensures r == z ==> (y < z && z< x) || (x < z && z< y):
    if x > y && y > z:
        return y
    else if z > y && y > x:
        return y
    else if x > z && z > y :
        return z
    else if y > z && z > x:
        return z
    return x