package quickcheck.generator.type;

import java.util.Random;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public final class BooleanGenerator implements Generator {
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Randomise values produced
	 */
	private static Random randomiser = new Random();
	
	private int count = 1;
	private TestType testType;

	public BooleanGenerator(TestType testType) {
		this.testType = testType;
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			if(count == 1) {
				count++;
				return semantics.Bool(true);
			}
			count++;
			return semantics.Bool(false);
		}
		return semantics.Bool(randomiser.nextBoolean());
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

}
