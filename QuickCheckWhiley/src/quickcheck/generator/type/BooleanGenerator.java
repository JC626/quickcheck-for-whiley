package quickcheck.generator.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
	
	private List<Boolean> testValues;
	
	private TestType testType;
	private int count = 1;

	public BooleanGenerator(TestType testType, int numTests) {
		this.testType = testType;
		
		// Random inputs use Knuth's Algorithm S
		if(testType == TestType.RANDOM) {
			Random randomiser = new Random(); 
			testValues = new ArrayList<Boolean>();
			int nextVal = 0;
			int selected = 0; 
			while(selected < numTests) {
				double uniform = randomiser.nextDouble();
				if((size() - nextVal)*uniform >= numTests - selected) {
					nextVal++;
				}
				else {
					testValues.add(nextVal == 0);
					nextVal++;
					selected++;
				}
				if(nextVal > 1) {
					nextVal = 0;
				}
			}
			//  Shuffle test values so they are not in order
			Collections.shuffle(testValues);
		}
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			count++;
			return semantics.Bool(count % 2 == 0);
		}
		int index = count - 1;
		count++;
		return semantics.Bool(testValues.get(index));
//		return semantics.Bool(randomiser.nextBoolean());
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
