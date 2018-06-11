type list is {node n, int data }
type node is null | list

function foo(list a) -> (int ans)
    ensures true:
    return a.data