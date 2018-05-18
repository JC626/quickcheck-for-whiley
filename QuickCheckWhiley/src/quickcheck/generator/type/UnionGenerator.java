package quickcheck.generator.type;

import java.util.List;
import java.util.Random;

import quickcheck.constraints.RangeHelper;
import quickcheck.util.TestType;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Expr;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate values for the union type.
 * Since a union can be any of a number of types,
 * multiple generators for each type is required.
 *
 * e.g. (int|bool)
 * would require a IntegerGenerator and BooleanGenerator
 * to be able to generate integer and boolean values.
 * Possible values generated could be 1, true, 6, false.
 *
 * @author Janice Chin
 *
 */
public final class UnionGenerator implements Generator {
	/** Randomise values produced */
	private static Random randomiser = new Random();

	/** Generators corresponding to each type in the union*/
	private List<Generator> generators;
	/**Index of the next generator used for generating values (exhaustive)*/
	private int currentIndex;

	private TestType testType;

	private int size = 0;
	private int count = 1;

	public UnionGenerator(List<Generator> generators, TestType testType) {
		this.generators = generators;
		this.testType = testType;
		currentIndex = 0;
		calculateSize();
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
			if(exceedCount()) {
				resetCount();
			}
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

	/**
	 * Check the ranges on the invariants against the generators.
	 * @param invariants The invariants to check against the generator on the nominal type
	 * @param interpreter The interpreter used
	 */
	public void checkInvariantRange(Tuple<Expr> invariants, Interpreter interpreter, String name) {
		// TODO pass the invariants down
		if (invariants.size() > 0 && generators.size() > 0) {
			for(Generator gen : generators) {
				if(gen instanceof ArrayGenerator || gen instanceof IntegerGenerator) {
					// TODO do normal
					RangeHelper.checkInvariantRange(gen, new Identifier(name), invariants, interpreter);
				}
				else if(gen instanceof NominalGenerator) {
					// TODO check
					NominalGenerator nomGen = (NominalGenerator) gen;
					nomGen.checkInvariantRange(invariants, new Identifier(name));
				}
				else if(gen instanceof RecordGenerator) {
					// TODO check
					RecordGenerator recordGen = (RecordGenerator) gen;
					recordGen.checkInvariantRange(invariants, interpreter, name);
				}
				else if(gen instanceof UnionGenerator) {
					// TODO check
					UnionGenerator unionGen = (UnionGenerator) gen;
					unionGen.checkInvariantRange(invariants, interpreter, name);
				}
			}
			calculateSize();
		}
	}

	private void calculateSize() {
		// Calculate the size
		this.size = 0;
		for(int i=0; i < generators.size(); i++) {
			size += generators.get(i).size();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generators == null) ? 0 : generators.hashCode());
		result = prime * result + size;
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
		UnionGenerator other = (UnionGenerator) obj;
		if (generators == null) {
			if (other.generators != null)
				return false;
		} else if (!generators.equals(other.generators))
			return false;
		if (size != other.size)
			return false;
		if (testType != other.testType)
			return false;
		return true;
	}



}
