package quickcheck.generator.type;

import wyil.interpreter.ConcreteSemantics.RValue;

public class NominalGenerator implements Generator{
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
