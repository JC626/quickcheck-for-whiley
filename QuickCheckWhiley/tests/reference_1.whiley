method foo(&int x_ptr) -> (int r)
requires true:
    int tmp = *x_ptr
    return tmp
