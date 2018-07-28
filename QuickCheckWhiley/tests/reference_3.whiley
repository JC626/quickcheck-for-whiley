method foo(&int x_ptr, &bool pos_ptr) -> (int r):
    int tmp = *x_ptr
    bool isPositive = *pos_ptr
    if tmp < 0 && isPositive:
        return -tmp
    else if tmp >=0 && !isPositive:
        return -tmp
    else:
        return tmp