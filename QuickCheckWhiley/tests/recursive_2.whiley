type node is null | {node n, int data}

function foo(node a) -> (int ans)
    ensures true:
    if a is null:
        return 0
    return a.data