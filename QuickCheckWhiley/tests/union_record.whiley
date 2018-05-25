type dimensions is {int width, int height} where 0 < width && width < 5
    where 0 < height && height < 9
type board is dimensions | int

function getArea(board b) -> int
    requires true:
    if b is dimensions:
        return b.width * b.height
    else if b is int:
        return b*b
    return -1
