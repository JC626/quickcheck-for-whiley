package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.Random;

import quickcheck.constraints.IntegerRange;
import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate integer values.
 * The integer generated is between the
 * lower limit (inclusive) and the upper limit (exclusive).
 * 
 * e.g. -100, 0, 120, 5
 * 
 * @author Janice Chin
 *
 */
public final class IntegerGenerator implements Generator {
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/** Randomise values produced */
	private static Random randomiser = new Random();
	
	private TestType testType;
	
	/** Lower limit (inclusive) for the integer generated */
	/** Upper limit (exclusive) for the integer generated */

	private IntegerRange range;

	private int size;
	private int count = 1;

	public IntegerGenerator(TestType testType, BigInteger lower, BigInteger upper) {
		this.testType = testType;
		this.range = new IntegerRange(lower, upper);
		this.size = upper.subtract(lower).intValue();
		checkValidRange();
	}
	
	@Override
	public RValue generate() {
		BigInteger value;
		if(testType == TestType.EXHAUSTIVE) {
			value = range.lowerBound().add(BigInteger.valueOf(count-1));
			if(value.compareTo(range.upperBound()) >= 0) {
				resetCount();
				value = range.lowerBound().add(BigInteger.valueOf(count-1));
			}
			count++;
			return semantics.Int(value);
		}
		else {
			boolean negateValue = range.lowerBound().compareTo(new BigInteger("0")) < 0;
			do {
			    value = new BigInteger(range.upperBound().bitLength(), randomiser);
				if(negateValue && !randomiser.nextBoolean()) {
					value = value.negate();
				}
			} while (value.compareTo(range.upperBound()) >= 0 || value.compareTo(range.lowerBound()) < 0);
			return semantics.Int(value);
		}
	}
	
	private void checkValidRange() {
		// Throw an error if the range is bigger than the other
		if(range.lowerBound().compareTo(range.upperBound()) >= 0) {
			throw new Error("Upper integer limit is less than or equal to the lower integer limit");
		}
	}
	
	/**
	 * Intersect the range of this generator with
	 * another generator if it hasn't generated any values yet.
	 * 
	 * @param other An integer range to intersect with
	 */
	public void joinRange(IntegerRange other) {
		assert count == 1;
		this.range = range.intersection(other);
		checkValidRange();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void resetCount() {
		count = 1;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((range.lowerBound() == null) ? 0 : range.lowerBound().hashCode());
		result = prime * result + size;
		result = prime * result + ((testType == null) ? 0 : testType.hashCode());
		result = prime * result + ((range.upperBound() == null) ? 0 : range.upperBound().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerGenerator other = (IntegerGenerator) obj;
		if (range.lowerBound() == null) {
			if (other.range.lowerBound() != null)
				return false;
		} else if (!range.lowerBound().equals(other.range.lowerBound()))
			return false;
		if (size != other.size)
			return false;
		if (testType != other.testType)
			return false;
		if (range.upperBound() == null) {
			if (other.range.upperBound()  != null)
				return false;
		} else if (!range.upperBound() .equals(other.range.upperBound()))
			return false;
		return true;
	}
}
