package quickcheck.generator.type;

import java.util.List;
import java.util.Random;

import quickcheck.util.TestType;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate values for the union type
 * e.g. (int|bool)
 * would generate integer and boolean values.
 * 
 * @author Janice Chin
 *
 */
public final class UnionGenerator implements Generator {
	/**
	 * Randomise values produced
	 */
	private static Random randomiser = new Random();
	
	private int count = 1;
	private int size = 0;
	private TestType testType;
	private List<Generator> generators;
	private int currentIndex;

	public UnionGenerator(List<Generator> generators, TestType testType) {
		this.generators = generators;
		this.testType = testType;
		currentIndex = 0;
		this.size = 0;
		for(int i=0; i < generators.size(); i++) {
			size += generators.get(i).size();
		}
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			// Iterate through the different generators
			if(currentIndex >= generators.size()) {
				currentIndex = 0;
			}
			// Generate all possible values, skip generators that have already finished generating values
			Generator currentGen = generators.get(currentIndex);
			while(currentGen.exceedCount()) {
				currentIndex++;
				if(currentIndex >= generators.size()) {
					currentIndex = 0;
				}
				currentGen = generators.get(currentIndex);
			}
			currentIndex++;
			count++;
			return currentGen.generate();
		}
		else {
			// Pick a random generator to generate tests
			int index = randomiser.nextInt(generators.size());
			return generators.get(index).generate();
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void resetCount() {
		count = 1;
		for(Generator gen : generators) {
			gen.resetCount();
		}
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}

}
