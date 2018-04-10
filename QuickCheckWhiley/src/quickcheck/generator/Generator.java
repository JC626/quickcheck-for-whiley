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
	 * Define generators here
	 */
	public static final IntegerGenerator INTEGER_GENERATOR = new IntegerGenerator();
	public static final BooleanGenerator BOOLEAN_GENERATOR = new BooleanGenerator();

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
	public abstract RValue generate();
	
	/**
	 * Generate random integer values.
	 * This uses the singleton pattern, therefore only one generator
	 * is created.
	 * 
	 * @author Janice Chin
	 *
	 */
	private static final class IntegerGenerator extends Generator{
		// FIXME 
		private BigInteger LOWER_LIMIT = new BigInteger("-1000");
		private BigInteger UPPER_LIMIT = new BigInteger("1000");
		private IntegerGenerator() {
			
		}
		
		@Override
		public RValue generate() {
			BigInteger value;
			boolean negateValue = LOWER_LIMIT.compareTo(new BigInteger("0")) < 0;
			do {
			    value = new BigInteger(UPPER_LIMIT.bitLength(), randomiser);
				if(negateValue && !randomiser.nextBoolean()) {
					value = value.negate();
				}
			} while (value.compareTo(UPPER_LIMIT) >= 0 && value.compareTo(LOWER_LIMIT) < 0);
			return Generator.semantics.Int(value);
		}
	}
	
	/**
	 * Generate random boolean values.
	 * This uses the singleton pattern, therefore only one generator
	 * is created.
	 * 
	 * @author Janice Chin
	 *
	 */
	private static final class BooleanGenerator extends Generator{		
		private BooleanGenerator() {
			
		}
		
		@Override
		public RValue generate() {
			return Generator.semantics.Bool(randomiser.nextBoolean());
		}
	}
	
}
