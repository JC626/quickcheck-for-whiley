function sameArraySize(int[] xs, bool[] ys) -> (bool r)
    ensures r <==> |xs| == |ys|:
		return |xs| == |ys|