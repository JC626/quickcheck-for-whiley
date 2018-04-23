// Returns true or false depending if the length is even or odd 
function arraySize(int[] xs, bool even) -> (bool r)
    ensures (r && !even) || (!r && even)  ==> |xs| % 2 == 1
    ensures (r && even) || (!r && !even) ==> |xs| % 2 == 0:
    if even:
        return |xs| % 2 == 0
    else:
        return |xs| % 2 == 1