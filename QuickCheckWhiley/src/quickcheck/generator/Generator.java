package quickcheck.generator;

import java.math.BigInteger;
import java.util.Random;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * A standard class for all Generators to inherit.
 * Allows access to an instance of each generator 
 * as all Generator implementations should be hidden.
 * 
 * @author Janice Chin
 *
 */
public abstract class Generator {
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Randomise values produced
	 */
	private static Random randomiser = new Random();
	
	private int count = 1;
	
	/**
	 * Generate a value for the specified type
	 * @return A value
	 */
	// FIXME add parameter with options in generate (for arrays, other generators used etc)
	// TODO some type of modifier for the generate function -> To be able to specify range and such
	public abstract RValue generate();
	
	
	/**
	 * Get the number of unique values that could be generated 
	 * @return The number of unique values or -1 if there is an indefinite number
	 */
	public int range() {
		return -1;
	};
	
	public void resetCount() {
		count = 1;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public int getCount() {
		return count;
	}
	
	public boolean exceedCount() {
		return this.range() < count;
	}
	
	/**
	 * Generate integer values
	 * between the lower limit (inclusive) and the upper limit (exclusive)
	 * 
	 * @author Janice Chin
	 *
	 */
	public static final class IntegerGenerator extends Generator{
		private TestType testType;
		private BigInteger lowerLimit;
		private BigInteger upperLimit;
		
		public IntegerGenerator(TestType testType, BigInteger lower, BigInteger upper) {
			this.testType = testType;
			this.lowerLimit = lower;
			this.upperLimit = upper;
		}
		
		@Override
		public RValue generate() {
			BigInteger value;
			if(testType == TestType.EXHAUSTIVE) {
				value = lowerLimit.add(BigInteger.valueOf(getCount()-1));
				if(value.compareTo(upperLimit) >= 0) {
					resetCount();
					value = lowerLimit.add(BigInteger.valueOf(getCount()-1));
				}
				incrementCount();
				return Generator.semantics.Int(value);
			}
			else {
				boolean negateValue = lowerLimit.compareTo(new BigInteger("0")) < 0;
				do {
				    value = new BigInteger(upperLimit.bitLength(), randomiser);
					if(negateValue && !randomiser.nextBoolean()) {
						value = value.negate();
					}
				} while (value.compareTo(upperLimit) >= 0 || value.compareTo(lowerLimit) < 0);
				return Generator.semantics.Int(value);
			}
		}
		
		@Override 
		public int range() {
			return upperLimit.subtract(lowerLimit).intValue();
		}
	}
	
	/**
	 * Generate boolean values.
	 *
	 * @author Janice Chin
	 *
	 */
	public static final class BooleanGenerator extends Generator{		
		private TestType testType;
		
		public BooleanGenerator(TestType testType) {
			this.testType = testType;
		}
		
		@Override
		public RValue generate() {
			if(testType == TestType.EXHAUSTIVE) {
				if(getCount() == 1) {
					incrementCount();
					return Generator.semantics.Bool(true);
				}
				incrementCount();
				return Generator.semantics.Bool(false);
			}
			return Generator.semantics.Bool(randomiser.nextBoolean());
		}
		@Override
		public int range() {
			return 2;
		}
	}
	
}
