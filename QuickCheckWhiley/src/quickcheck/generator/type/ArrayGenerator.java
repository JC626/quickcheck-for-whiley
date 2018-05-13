package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import quickcheck.constraints.IntegerRange;
import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate an array based on the type of the generator.
 * The size of the array generated is limited based on 
 * the lower and upper limit.
 * 
 * e.g. int[] 
 * would require a IntegerGenerator 
 * and could return [4, 6].
 * 
 * @author Janice Chin
 *
 */
public class ArrayGenerator implements Generator{
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/** Randomise values produced */
	private static Random randomiser = new Random();
	
	private TestType testType;
	/** Generators corresponding to each array element */
	private List<Generator> generators;
	/** Current array elements generated */
	private RValue[] arrElements;
	
	/** Lower limit (inclusive) and upper limit (exclusive) for the size of the array generated */
	private IntegerRange range;
	
	private int size = 0;
	/** Number of combinations completed so far for the current size of the array */
	private int currentCombinations;
	private int count = 1;

	public ArrayGenerator(List<Generator> generators, TestType testType, int lower, int upper) {
		this.generators = generators;
		this.testType = testType;
		this.range = new IntegerRange(lower, upper + 1);
		this.currentCombinations = 0;
		checkValidRange();
		calculateSize();
	}
	
	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			// Empty array
			if(count == 1 && range.lowerBound().equals(BigInteger.valueOf(0))) {
				count++;
				return semantics.Array(new RValue[0]);
			}
			else {
				int size = range.lowerBound().intValue() > 0 ? range.lowerBound().intValue() : 1;
				// Get the size of the array
				if(arrElements != null) {
					if(currentCombinations >= Math.pow(generators.get(0).size(), arrElements.length)) {
						size = arrElements.length + 1;
						currentCombinations = 0;
					}
				}
				// Resetting as it is a new array size
				if(arrElements == null || arrElements.length < size) {
					arrElements = new RValue[size];
					assert size < range.upperBound().intValue();
					for(int i=0; i < arrElements.length; i++) {
						Generator gen = generators.get(i);
						gen.resetCount();
						arrElements[i] = gen.generate();
					}
				}
				else {
					// Generate the array elements backwards 
					for(int i=arrElements.length - 1; i >= 0 ; i--) {
						Generator gen = generators.get(i);
						if(!gen.exceedCount()) {
							arrElements[i] = gen.generate();
							break;
						}
						else {
							gen.resetCount();
							arrElements[i] = gen.generate();
						}
					}
				}
				count++;
				currentCombinations++;
				return semantics.Array(arrElements);
			}
		}
		else {
			int size = randomiser.nextInt(range.upperBound().intValue() - range.lowerBound().intValue() + 1) + range.lowerBound().intValue();
			RValue[] array = new RValue[size];
			for(int i=0; i < array.length; i++) {
				array[i] = generators.get(0).generate();
			}
			count++;
			return semantics.Array(array);
		}
	}
	
	private void checkValidRange() {
		// Throw an error if the range is bigger than the other
		if(range.lowerBound().compareTo(range.upperBound()) >= 0) {
			throw new Error("Upper integer limit is less than or equal to the lower integer limit");
		}
	}
	
	/**
	 * Intersect the range of this generator with
	 * another generator if it hasn't generated any values yet.
	 * 
	 * @param other An integer range to intersect with
	 */
	public void joinRange(IntegerRange other) {
		assert count == 1;
		this.range = range.intersection(other);
		checkValidRange();
		calculateSize();
		System.out.println(this.range);
	}
	
	private void calculateSize(){
		// Calculate size
		int start = range.lowerBound().intValue();
		if(start == 0) {
			this.size = 1;
			start++;
		}
		else {
			this.size = 0;
		}
		int generatorRange = generators.get(0).size();
		for(int i=start; i < range.upperBound().intValue(); i++) {
			this.size += Math.pow(generatorRange, i);
		}
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public void resetCount() {
		count = 1;
		currentCombinations = 0;
		this.arrElements = null;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generators == null) ? 0 : generators.hashCode());
		result = prime * result + range.lowerBound().intValue();
		result = prime * result + size;
		result = prime * result + ((testType == null) ? 0 : testType.hashCode());
		result = prime * result + range.upperBound().intValue();
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
		ArrayGenerator other = (ArrayGenerator) obj;
		if (generators == null) {
			if (other.generators != null)
				return false;
		} else if (!generators.equals(other.generators))
			return false;
		if (range != other.range)
			return false;
		if (size != other.size)
			return false;
		if (testType != other.testType)
			return false;
		return true;
	}
}
