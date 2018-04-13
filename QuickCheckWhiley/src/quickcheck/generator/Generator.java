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
	
	/**
	 * Generate a randomised value for it's specified type
	 * @return A randomised value
	 */
	// FIXME add parameter with options in generate (for arrays, other generators used etc)
	public abstract RValue generate();
	
	/**
	 * Generate random integer values.
	 * 
	 * @author Janice Chin
	 *
	 */
	public static final class IntegerGenerator extends Generator{
		private BigInteger lowerLimit;
		private BigInteger upperLimit;
		public IntegerGenerator(String lower, String upper) {
			this.lowerLimit = new BigInteger(lower);
			this.upperLimit = new BigInteger(upper);
		}
		
		@Override
		public RValue generate() {
			BigInteger value;
			boolean negateValue = lowerLimit.compareTo(new BigInteger("0")) < 0;
			do {
			    value = new BigInteger(upperLimit.bitLength(), randomiser);
				if(negateValue && !randomiser.nextBoolean()) {
					value = value.negate();
				}
			} while (value.compareTo(upperLimit) >= 0 && value.compareTo(lowerLimit) < 0);
			return Generator.semantics.Int(value);
		}
	}
	
	/**
	 * Generate random boolean values.
	 *
	 * @author Janice Chin
	 *
	 */
	public static final class BooleanGenerator extends Generator{		
		public BooleanGenerator() {
			
		}
		
		@Override
		public RValue generate() {
			return Generator.semantics.Bool(randomiser.nextBoolean());
		}
	}
	
}
