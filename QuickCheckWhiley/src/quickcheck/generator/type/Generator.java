package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * A standard class for all Generators to inherit.
 * 
 * @author Janice Chin
 *
 */

public interface Generator{
		
	public RValue generate();
	
	/**
	 * Get the number of unique values that could be generated 
	 */
	public int size();
	
	public void resetCount();
	
	public boolean exceedCount();
		
}
