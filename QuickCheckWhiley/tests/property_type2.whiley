property valid(int x) where x >= 0

type number is ({int data} n) where valid(n.data)

function getNum(number n) -> (int y):
	return n.data
	