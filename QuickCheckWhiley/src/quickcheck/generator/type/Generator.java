package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * A standard inteface for the generators of
 * different types to inherit.
 * 
 * @author Janice Chin
 *
 */

public interface Generator{
		
	/**
	 * Generate a test value for a type
	 * @return A test value
	 */
	public RValue generate();
	
	/**
	 * Generate a test value for a type based on the specific combination
	 */
	public RValue generateCombination(int comboNum);
	
	/**
	 * Get the number of unique values that could be generated.
	 * @return The number of unique values/combinations that can be generated.
	 */
	public int size();
	
	/**
	 * Reset the counting for the generator.
	 * Used in exhaustive testing when all possible values has been generated.
	 */
	public void resetCount();
	
	/**
	 * Check if all possible values have been generated by the generator.
	 * @return If all possible values have been generated.
	 */
	public boolean exceedCount();
		
}
