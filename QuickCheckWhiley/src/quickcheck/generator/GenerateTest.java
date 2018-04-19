package quickcheck.generator;

import wyil.interpreter.ConcreteSemantics.RValue;

public interface GenerateTest {
	
	/**
	 * Generate parameters to be used as a single test for the function.
	 * @return The parameters for the function.
	 */
	public RValue[] generateParameters();
}
