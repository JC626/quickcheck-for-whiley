package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
    private int numTests;
    private int numTested;
    
    private List<BigInteger> testValues;

    public IntegerGenerator(TestType testType, int numTests, BigInteger lower, BigInteger upper) {
		this.testType = testType;
        this.numTests = numTests;
		this.range = new IntegerRange(lower, upper);
		checkValidRange();
		calculateSize();

		// Random inputs use Knuth's Algorithm S
		if(testType == TestType.RANDOM) {
			testValues = new ArrayList<BigInteger>();
			BigInteger count = range.lowerBound();
			int selected = 0; 
			while(selected < numTests) {
				double uniform = randomiser.nextDouble();
				if((size - count.intValue())*uniform >= numTests - selected) {
					count = count.add(BigInteger.valueOf(1));
				}
				else {
					testValues.add(count);
					count = count.add(BigInteger.valueOf(1));
					selected++;
				}
				if(count.compareTo(range.upperBound()) >= 0) {
					count = lower;
				}
			}
			//  Shuffle test values so they are not in order
			Collections.shuffle(testValues);
		}
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
			int index = count - 1;
			count++;
			return semantics.Int(testValues.get(index));
//			boolean negateValue = range.lowerBound().compareTo(new BigInteger("0")) < 0;
//			do {
//			    value = new BigInteger(range.upperBound().bitLength(), randomiser);
//				if(negateValue && !randomiser.nextBoolean()) {
//					value = value.negate();
//				}
//			} while (value.compareTo(range.upperBound()) >= 0 || value.compareTo(range.lowerBound()) < 0);
//			return semantics.Int(value);
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
		calculateSize();
	}
	
	private void calculateSize() {
		this.size = range.upperBound().subtract(range.lowerBound()).intValue();
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
