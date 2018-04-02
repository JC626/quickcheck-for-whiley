function multiple(bool a, bool b) -> (bool r)
ensures r ==> a && b:
    if a && b:
        return true
    return false
