function reverse(int[] xs) -> (int[] ys)
    // size of lists are the same
    ensures all { k in 0..|xs| | xs[k] == ys[|ys|-(k+1)] }
    ensures |xs| == |ys|:
    int i = 0
    int[] zs = xs
    //
    while i<|xs| 
    where i>=0 && |xs|==|zs| 
    where all { k in 0..i | xs[k] == zs[|xs|-(k+1)] } :
        int j = |xs| - (i+1)
        xs[i] = zs[j]
        i = i + 1
    return xs