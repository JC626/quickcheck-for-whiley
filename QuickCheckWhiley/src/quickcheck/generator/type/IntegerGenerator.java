package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.Random;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public final class IntegerGenerator implements Generator {
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Randomise values produced
	 */
	private static Random randomiser = new Random();
	
	private int count = 1;
	private TestType testType;
	private BigInteger lowerLimit; // Lower limit is inclusive
	private BigInteger upperLimit; // Upper limit is exclusive
	
	public IntegerGenerator(TestType testType, BigInteger lower, BigInteger upper) {
		this.testType = testType;
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}
	
	@Override
	public RValue generate() {
		BigInteger value;
		if(testType == TestType.EXHAUSTIVE) {
			value = lowerLimit.add(BigInteger.valueOf(count-1));
			if(value.compareTo(upperLimit) >= 0) {
				resetCount();
				value = lowerLimit.add(BigInteger.valueOf(count-1));
			}
			count++;
			return semantics.Int(value);
		}
		else {
			boolean negateValue = lowerLimit.compareTo(new BigInteger("0")) < 0;
			do {
			    value = new BigInteger(upperLimit.bitLength(), randomiser);
				if(negateValue && !randomiser.nextBoolean()) {
					value = value.negate();
				}
			} while (value.compareTo(upperLimit) >= 0 || value.compareTo(lowerLimit) < 0);
			return semantics.Int(value);
		}
	}

	@Override
	public int size() {
		return upperLimit.subtract(lowerLimit).intValue();
	}

	@Override
	public void resetCount() {
		count = 1;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}	

}
