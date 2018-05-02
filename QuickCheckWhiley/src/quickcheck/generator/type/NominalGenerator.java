package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate values for a nominal type.
 * Since a nominal type renames an existing type, 
 * it takes another generator to generate values 
 * for the existing type.
 * 
 * e.g. type nat is int where x > 0
 * Would require an IntegerGenerator 
 * and could return a value of 20.
 * 
 * @author Janice Chin
 *
 */
public class NominalGenerator implements Generator{
	/**Generator for the type that is renamed.*/
	private Generator generator;

	public NominalGenerator(Generator generator) {
		super();
		this.generator = generator;
	}

	@Override
	public RValue generate() {
		return generator.generate();
	}
	
	@Override
	public int size() {
		return generator.size();
	}

	@Override
	public void resetCount() {
		generator.resetCount();
	}

	@Override
	public boolean exceedCount() {
		return generator.exceedCount();
	}

}
