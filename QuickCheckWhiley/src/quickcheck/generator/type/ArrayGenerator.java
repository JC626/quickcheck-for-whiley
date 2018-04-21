package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

public class ArrayGenerator implements Generator{
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
	private int lowerLimit;
	private int upperLimit;
	private List<Generator> generators;
	
	public ArrayGenerator(List<Generator> generators, TestType testType, int lower, int upper) {
		this.generators = generators;
		this.testType = testType;
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}
	
	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			// Empty array
			if(count == 1) {
				count++;
				return semantics.Array(new RValue[0]);
			}
			else {
				int size = 1 + (count / generators.get(0).size());
				RValue[] array = new RValue[size];
				for(int i=0; i < array.length; i++) {
					array[i] = generators.get(i).generate();
				}
				count++;
				return semantics.Array(array);
			}
		}
		else {
			int size = randomiser.nextInt(upperLimit - lowerLimit + 1) + lowerLimit;
			RValue[] array = new RValue[size];
			for(int i=0; i < array.length; i++) {
				array[i] = generators.get(0).generate();
			}
			count++;
			return semantics.Array(array);
		}
	}

	@Override
	public int size() {
		int size = 1;
		int generatorRange = generators.get(0).size();
		for(int i=1; i <= upperLimit; i++) {
			size += Math.pow(generatorRange, i);
		}
		return size;
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
