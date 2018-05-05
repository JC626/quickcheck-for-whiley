package quickcheck.constraints;

import java.math.BigInteger;

import wycc.util.Pair;

/**
 * <p>
 * Represents the set of consecutive integers between a lower and upper bound
 * (inclusive). For example, the range <code>[1,3]</code> represents the set
 * <code>{1,2,3}</code>.
 * </p>
 * 
 * <p>
 * Integer ranges also support the notion of infinity. Thus, for example, we can
 * express the set of natural numbers as the integer range <code>[0,inf]</code>.
 * Finally, in the case that the lower bound is greater than the upper bound
 * then we have the empty set.
 * </p>
 * 
 * @author David J. Pearce
 *
 */
public class IntegerRange implements Range {
	/**
	 * Represents the lower bound for this range. This maybe null, in which case
	 * it represents negative infinity.
	 */
	private final BigInteger lowerBound;
	/**
	 * Represents the upper bound for this range. This maybe null, in which case
	 * it represents positive infinity.
	 */
	private final BigInteger upperBound;
	
	/**
	 * Construct an integer range from a lower and upper bound, either of which
	 * may be null. A null lower bound signals negative infinity, whilst a null
	 * upper bound signals positive infinity.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 */
	public IntegerRange(BigInteger lowerBound, BigInteger upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	/**
	 * Construct an integer range from a finite lower and upper bound.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 */
	public IntegerRange(long lowerBound, long upperBound) {
		this.lowerBound = BigInteger.valueOf(lowerBound);
		this.upperBound = BigInteger.valueOf(upperBound);
	}
	
	/**
	 * Represents the top element in the lattice of integer ranges. This is the
	 * range which contains all possible integer values.
	 */
	public static final IntegerRange TOP = new IntegerRange(null,null);
	
	/**
	 * Represents the top element in the lattice of integer ranges. This is the
	 * range which contains all possible integer values.
	 */
	public static final IntegerRange BOTTOM = new IntegerRange(BigInteger.ONE,BigInteger.ZERO);

	// =============================================
	// Accessors
	// =============================================

	public BigInteger lowerBound() {
		return lowerBound;
	}
	
	public BigInteger upperBound() {
		return upperBound;
	}
	
	// =============================================
	// Arithmetic Operators
	// =============================================
	
	public IntegerRange add(IntegerRange other) {		
		BigInteger lb = null;
		BigInteger ub = null;
		if(lowerBound != null && other.lowerBound != null) {
			lb = lowerBound.add(other.lowerBound);
		}
		if(upperBound != null && other.upperBound != null) {
			ub = upperBound.add(other.upperBound);
		}
		return new IntegerRange(lb,ub);
	}
	
	public IntegerRange subtract(IntegerRange other) {
		BigInteger lb = null;
		BigInteger ub = null;
		if(lowerBound != null && other.upperBound != null) {
			lb = lowerBound.subtract(other.upperBound);
		}
		if(upperBound != null && other.lowerBound != null) {
			ub = upperBound.subtract(other.lowerBound);
		}
		return new IntegerRange(lb,ub);
	}
	
	public IntegerRange multiply(IntegerRange other) {
		return null;
	}
	
	public IntegerRange divide(IntegerRange other) {
		return null;
	}
	
	// =============================================
	// Comparators
	// =============================================

	public Pair<IntegerRange,IntegerRange> equals(IntegerRange other) {
		IntegerRange r = this.intersection(other);
		return new Pair<IntegerRange,IntegerRange>(r,r);
	}
	
	public Pair<IntegerRange,IntegerRange> notEquals(IntegerRange other) {
		return new Pair<IntegerRange,IntegerRange>(this.difference(other),other.difference(this));
	}
	
	public Pair<IntegerRange, IntegerRange> lessThan(IntegerRange other) {
		IntegerRange lb = new IntegerRange(lowerBound, min(upperBound, true,
				subtract(other.upperBound,BigInteger.ONE), true));
		IntegerRange ub = new IntegerRange(max(add(lowerBound,BigInteger.ONE),
				false, other.lowerBound, false), other.upperBound);
		return new Pair<IntegerRange, IntegerRange>(lb, ub);
	}
	
	public Pair<IntegerRange,IntegerRange> lessThanOrEquals(IntegerRange other) {
		IntegerRange lb = new IntegerRange(lowerBound,min(upperBound,true,other.upperBound,true));
		IntegerRange ub = new IntegerRange(max(lowerBound,false,other.lowerBound,false),other.upperBound);
		return new Pair<IntegerRange,IntegerRange>(lb,ub);
	}
	
	public Pair<IntegerRange,IntegerRange> greaterThan(IntegerRange other) {
		return swap(other.lessThan(this));
	}
	
	public Pair<IntegerRange,IntegerRange> greaterThanOrEquals(IntegerRange other) {
		return swap(other.lessThanOrEquals(this));
	}
	
	// =============================================
	// Lattice Operators
	// =============================================

	public IntegerRange union(IntegerRange other) {
		return new IntegerRange(
				min(lowerBound, false, other.lowerBound, false), max(
						upperBound, true, other.upperBound, true));
	}
	
	public IntegerRange intersection(IntegerRange other) {
		return new IntegerRange(
				max(lowerBound, false, other.lowerBound, false), min(
						upperBound, true, other.upperBound, true));
	}
	
	public IntegerRange difference(IntegerRange other) {
		return null;
	}

	// =============================================
	// Helper methods
	// =============================================

	/**
	 * Check whether this range completely contains another range.
	 * 
	 * @param r
	 * @return
	 */
	public boolean contains(IntegerRange ir) {
		int l = lowerBound.compareTo(ir.lowerBound);
		int r = upperBound.compareTo(ir.upperBound);
		return l <= 0 && r >= 0;
	}
	
	public String toString() {
		String lb = lowerBound == null ? "-inf" : lowerBound.toString();
		String ub = upperBound == null ? "+inf" : upperBound.toString();
		return "int[" + lb + "," + ub + "]";
	}
		
	/**
	 * Compute the minimum of two integer ranges, whilst assuming that null
	 * represents either negative or positive infinity.
	 * 
	 * @param x
	 * @param xSign
	 *            The sign for the x operand, with false indicated negative.
	 * @param y
	 * @param ySign
	 *            The sign for the y operand, with false indicated negative.
	 * @return
	 */
	private static BigInteger min(BigInteger x, boolean xSign, BigInteger y, boolean ySign) {
		// First, handle infinity
		if(x == null) {
			if(xSign) {
				// x represents positive infinity, which is the greatest element
				return y;
			} else {
				// x represents negative infinity, which is the least element
				return null;
			}
		} else if(y == null) {
			if(ySign) {
				// y represents positive infinity, which is the greatest element
				return x;
			} else {
				// y represents negative infinity, which is the least element
				return null;
			}
		} 
		// Ok, definitely not an infinity
		return x.min(y);
	}
	
	/**
	 * Compute the maximum of two integer ranges, whilst assuming that null
	 * represents either negative or positive infinity.
	 * 
	 * @param x
	 * @param xSign
	 *            The sign for the x operand, with false indicated negative.
	 * @param y
	 * @param ySign
	 *            The sign for the y operand, with false indicated negative.
	 * @return
	 */
	private static BigInteger max(BigInteger x, boolean xSign, BigInteger y, boolean ySign) {
		// First, handle infinity
		if(x == null) {
			if(xSign) {
				// x represents positive infinity, which is the greatest element
				return null;
			} else {
				// x represents negative infinity, which is the least element
				return y;
			}
		} else if(y == null) {
			if(ySign) {
				// y represents positive infinity, which is the greatest element
				return null;
			} else {
				// y represents negative infinity, which is the least element
				return x;
			}
		} 
		// Ok, definitely not an infinity
		return x.max(y);
	}
	
	/**
	 * Add two big integers, of which either maybe infinity.
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private BigInteger add(BigInteger lhs, BigInteger rhs) {
		if(lhs == null || rhs == null)  {
			return null;
		} else {
			return lhs.add(rhs);
		}
	}
	
	/**
	 * Subtract two big integers, of which either maybe infinity.
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private BigInteger subtract(BigInteger lhs, BigInteger rhs) {
		if(lhs == null || rhs == null)  {
			return null;
		} else {
			return lhs.subtract(rhs);
		}
	}
	
	private Pair<IntegerRange,IntegerRange> swap(Pair<IntegerRange,IntegerRange> p) {
		return new Pair<IntegerRange,IntegerRange>(p.second(),p.first());
	}
}
