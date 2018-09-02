package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate a null value.
 * 
 * @author Janice Chin
 *
 */
public class NullGenerator implements Generator{
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	/** Check if the null value has been generated yet */
	private boolean generated = false;
	
	public NullGenerator() {
		super();
	}

	@Override
	public RValue generate() {
		generated = true;
		return semantics.Null();
	}
	
	@Override
	public RValue generate(int comboNum) {
		return semantics.Null();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void resetCount() {
		generated = false;
	}

	@Override
	public boolean exceedCount() {
		return generated;
	}

}
