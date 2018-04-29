package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public class NullGenerator implements Generator{
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	public NullGenerator() {
		super();
	}

	@Override
	public RValue generate() {
		return semantics.Null();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void resetCount() {
		
	}

	@Override
	public boolean exceedCount() {
		return true;
	}

}
