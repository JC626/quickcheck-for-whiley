package quickcheck.generator.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	private TestType testType;
	private int LOWER_LIMIT = 0;
	private int UPPER_LIMIT = 256;

	private int count = 1;
	
	private List<Byte> testValues;
	
	public ByteGenerator(TestType testType, int numTests) {
		this.testType = testType;
		
		// Random inputs use Knuth's Algorithm S
		if(testType == TestType.RANDOM) {
			Random randomiser = new Random();
			testValues = new ArrayList<Byte>();
			int nextVal = LOWER_LIMIT;
			int selected = 0; 
			while(selected < numTests) {
				double uniform = randomiser.nextDouble();
				if((size() - nextVal)*uniform >= numTests - selected) {
					nextVal++;
				}
				else {
					testValues.add((byte) nextVal);
					nextVal++;
					selected++;
				}
				if(nextVal >= UPPER_LIMIT) {
					nextVal = LOWER_LIMIT;
				}
			}
			//  Shuffle test values so they are not in order
			Collections.shuffle(testValues);
		}
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			byte binary = (byte) (count - 1);
			count++;
			return semantics.Byte(binary);
		}
 		else if(count >= testValues.size()) {
 			Random randomiser = new Random(); 
			int nextCombo = 0;
			int selected = 0; 
			while(true) {
				double uniform = randomiser.nextDouble();
				if((size() - nextCombo)*uniform >= 1 - selected) {
					nextCombo++;
				}
				else {
					return generateCombination(nextCombo);
				}
				if(nextCombo >= size()) {
					nextCombo = 0;
				}
			}
 		}
		else {
			int index = count -1;
			count++;
			return semantics.Byte(testValues.get(index));
//			int value = -1;
//			do {
//			    value = randomiser.nextInt(UPPER_LIMIT);
//			} while (((Integer) value).compareTo(UPPER_LIMIT) >= 0 || ((Integer) value).compareTo(LOWER_LIMIT) < 0);
//			return semantics.Byte((byte) value);
		}
	}
	
	@Override
	public RValue generateCombination(int comboNum) {
		return semantics.Byte((byte) comboNum);
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
