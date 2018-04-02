package quickcheck.generator;

import java.math.BigInteger;
import java.util.Random;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public abstract class Generator {
	/**
	 * Define generators here
	 */
	public static final IntegerGenerator INTEGER_GENERATOR = new IntegerGenerator();

	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Randomise values produced
	 */
	private static Random randomiser = new Random();
	
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
		private IntegerGenerator() {
			
		}
		
		@Override
		public RValue generate() {
			// FIXME actually generate a value
//			BigInteger value = new BigInteger(100, randomiser);
			BigInteger value = BigInteger.valueOf(5);
			return Generator.semantics.Int(value);
		}
	}
}
