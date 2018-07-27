package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate a reference value.
 * 
 * @author Janice Chin
 *
 */
public class ReferenceGenerator implements Generator{
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**Generator for the type referenced.*/
	private Generator generator;
	
	// TODO static list of existing cells for the type
	// TODO How is size calculated with aliasing?

	
	// Generator generator
	public ReferenceGenerator(Generator generator) {
		this.generator = generator;
		// TODO need to generate random value? 
		// as initial value is technically, a "random" value
	}
	
	@Override
	public RValue generate() {
		return semantics.Reference(semantics.Cell(generator.generate()));
	}
	
	@Override
	public RValue generateCombination(int comboNum) {
		return semantics.Reference(semantics.Cell(generator.generateCombination(comboNum)));
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
