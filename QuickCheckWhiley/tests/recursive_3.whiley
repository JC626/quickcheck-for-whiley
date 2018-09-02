type node is int | {node n, int data}

function foo(node a) -> (int ans)
    ensures true:
    if a is int:
        return a
    return a.data