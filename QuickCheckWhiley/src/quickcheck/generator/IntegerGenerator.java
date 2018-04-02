package quickcheck.generator;

import java.math.BigInteger;
import java.util.Random;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;


public final class IntegerGenerator implements Generator{
	
	public static final IntegerGenerator INTEGER_GENERATOR = new IntegerGenerator();
	
	/**
	 * Used for generating integers
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Randomise the integers produced
	 */
	private static Random randomiser = new Random();
	
	@Override
	public RValue generate() {
		// FIXME actually generate a value
//		BigInteger value = new BigInteger(100, randomiser);
		BigInteger value = BigInteger.valueOf(5);
		return semantics.Int(value);
	}
}
