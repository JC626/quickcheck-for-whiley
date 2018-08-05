package quickcheck.generator;

import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * An interface for standardising 
 * a method for generating candidate test values. 
 * 
 * @author Janice Chin
 *
 */
public interface GenerateTest {
	
	/**
	 * Generate parameters to be used as a single test for the function.
	 * @return The parameters for the function.
	 */
	public RValue[] generateParameters();
	
	/**
	 * Check if all the possible combinations
	 * have been generated or not.
	 * @return Whether all combinations have been generated.
	 */
	public boolean exceedSize();
}
