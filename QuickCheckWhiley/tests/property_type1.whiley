property valid(bool b) where b == true

type number is ({int data, bool isPositive} n) where valid(n.isPositive)

function getNum(number n) -> (int y):
	return n.data
	