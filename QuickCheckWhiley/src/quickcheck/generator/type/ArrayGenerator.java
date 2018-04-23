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
	private int currentCombinations;
	private TestType testType;
	private int lowerLimit;
	private int upperLimit;
	private List<Generator> generators;
	private RValue[] parameters;
	
	public ArrayGenerator(List<Generator> generators, TestType testType, int lower, int upper) {
		this.generators = generators;
		this.testType = testType;
		this.lowerLimit = lower;
		this.upperLimit = upper;
		this.currentCombinations = 0;
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
				int size = 1;
				if(parameters != null) {
					if(currentCombinations >= Math.pow(generators.get(0).size(), parameters.length)) {
						size = parameters.length + 1;
						currentCombinations = 0;
					}
				}
				if(parameters == null || parameters.length < size) {
					parameters = new RValue[size];
					for(int i=0; i < parameters.length; i++) {
						parameters[i] = generators.get(i).generate();
					}
				}
				else {
					for(int i=parameters.length - 1; i >= 0 ; i--) {
						Generator gen = generators.get(i);
						if(!gen.exceedCount()) {
							parameters[i] = gen.generate();
							break;
						}
						else {
							parameters[i] = gen.generate();
						}
					}
				}
				count++;
				currentCombinations++;
				return semantics.Array(parameters);
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
		currentCombinations = 0;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}
}
