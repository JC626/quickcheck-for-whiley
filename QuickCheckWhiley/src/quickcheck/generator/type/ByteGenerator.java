package quickcheck.generator.type;

import java.util.Random;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;


/**
 * Generate byte values, which are between
 * 0 (inclusive) and 256 (exclusive)
 * i.e. between the binary values 0 and 11111111
 *
 * @author Janice Chin
 *
 */
public class ByteGenerator implements Generator{
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/** Randomise values produced */
	private static Random randomiser = new Random();
	
	private TestType testType;
	private int LOWER_LIMIT = 0;
	private int UPPER_LIMIT = 256;

	private int count = 1;
	
	public ByteGenerator(TestType testType) {
		this.testType = testType;
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			byte binary = (byte) (count - 1);
			count++;
			return semantics.Byte(binary);
		}
		else {
			int value = -1;
			do {
			    value = randomiser.nextInt(UPPER_LIMIT);
			} while (((Integer) value).compareTo(UPPER_LIMIT) >= 0 || ((Integer) value).compareTo(LOWER_LIMIT) < 0);
			return semantics.Byte((byte) value);
		}
	}

	@Override
	public int size() {
		return UPPER_LIMIT;
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
