package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public class NullGenerator implements Generator{
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
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
