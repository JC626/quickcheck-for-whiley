package quickcheck.generator;

import wyil.interpreter.ConcreteSemantics.RValue;

public interface Generator {
	public RValue generate();
}
