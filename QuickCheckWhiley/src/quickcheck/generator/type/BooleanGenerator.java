package quickcheck.generator.type;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate boolean values.
 * 
 * e.g. true, false
 * 
 * @author Janice Chin
 *
 */
public final class BooleanGenerator implements Generator {
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
		
	private TestType testType;
	private int count = 1;

	public BooleanGenerator(TestType testType, int numTests) {
		this.testType = testType;
	}

	@Override
	public RValue generate() {
		assert testType == TestType.EXHAUSTIVE;
		count++;
		return semantics.Bool(count % 2 == 0);
	}
	
	@Override
	public RValue generate(int comboNum) {
		return semantics.Bool(comboNum == 0);
	}


	@Override
	public int size() {
		return 2;
	}

	@Override
	public void resetCount() {
		count = 1;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testType == null) ? 0 : testType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanGenerator other = (BooleanGenerator) obj;
		if (testType != other.testType)
			return false;
		return true;
	}

}
