package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import quickcheck.RunTest;
import quickcheck.generator.type.ArrayGenerator;
import quickcheck.generator.type.BooleanGenerator;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import quickcheck.util.TestType;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Decl.Variable;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate candidate test parameters exhaustively from a function.
 * This uses a brute force approach by iteratively generating 
 * the candidate test parameters, one combination at a time.
 * As we are not generating all possible parameter combinations at once, 
 * we cannot use a recursive function to do this.
 * 
 * @author Janice Chin
 *
 */
public class ExhaustiveGenerateTest implements GenerateTest{
	/**
	 * The function/method we want to test
	 */
	private Decl.FunctionOrMethod dec;
	
	/**
	 *  A list of generators, each corresponding to a parameter in the function/method
	 */
	private List<Generator> parameterGenerators;
	/**
	 * The last parameters used to create a test
	 */
	private RValue[] parameters;
	/**
	 * Stores the generators used in an iterative manner.
	 */
	private Stack<Generator> stack;
	
	private BigInteger totalCombinations;
	private int numTested;
	private int numTests; // Default number of tests to run
	private boolean allTests;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;

	
	public ExhaustiveGenerateTest(FunctionOrMethod dec, int numTests, BigInteger lowerLimit, BigInteger upperLimit) {
		this.dec = dec;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.parameterGenerators = new ArrayList<Generator>();
		this.numTests = numTests;
		this.totalCombinations = BigInteger.valueOf(1);
		// Get the generators
		for(Variable var : this.dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			Generator gen = getGenerator(paramType);
			totalCombinations.multiply(BigInteger.valueOf(gen.size()));
			this.parameterGenerators.add(gen);
		}
		if(parameterGenerators.isEmpty()) {
			this.totalCombinations = new BigInteger("0");
		}
		this.parameters = new RValue[parameterGenerators.size()];
		this.allTests = totalCombinations.compareTo(BigInteger.valueOf(numTests)) != 1;
		stack = new Stack<Generator>();
	}
	
	
	private Generator getGenerator(WhileyFile.Type paramType) {
		if(paramType instanceof WhileyFile.Type.Int) {
			return new IntegerGenerator(TestType.EXHAUSTIVE, lowerLimit, upperLimit);
		}
		else if(paramType instanceof WhileyFile.Type.Bool) {
			return new BooleanGenerator(TestType.EXHAUSTIVE);
		}
		else if(paramType instanceof WhileyFile.Type.Array) {
			WhileyFile.Type arrEle = ((WhileyFile.Type.Array) paramType).getElement();
			List<Generator> generators = new ArrayList<Generator>();
			for(int i=0; i < RunTest.ARRAY_UPPER_LIMIT; i++) {
				Generator gen = getGenerator(arrEle);
				generators.add(gen);
			}
			return new ArrayGenerator(generators, TestType.EXHAUSTIVE, RunTest.ARRAY_LOWER_LIMIT, RunTest.ARRAY_UPPER_LIMIT);
		}
		assert false;
		return null;
	}

	@Override
	public RValue[] generateParameters() {
		// Brute force generate parameters, iteratively 
		// Keep the previous state
		// Iterate through the generators to generate the parameters
		if(parameters.length == 0){
			return parameters;
		}
		// FIXME (only run for allTests)
		else if(true) {
			// Initialise the first combination used
			if(parameters[0] == null) {
				for(int i=0; i < parameters.length; i++) {
					Generator gen = parameterGenerators.get(i);
					parameters[i] = gen.generate();
					stack.push(gen);
				}
			}
			else{
				assert stack.size() > 0;
				// Get the last generator used
				int genIndex = stack.size() -1;
				Generator gen = stack.peek();
				/*
				 *  If the last generator has reached it's limit 
				 *  (i.e. we cannot move onto the next combination)
				 *  reset and remove the generator.
			     *  Repeat for all previous generators that have reached its limit,
			     *  until we reach a generator which hasn't reached it's limit.
			     *  All generators which have reached its limit will be 
			     *  re-added as they are used for generating the next set of combinations.
				 */
				while(gen.exceedCount() && stack.size() > 1) {
					gen.resetCount();
					stack.pop();
					if(stack.isEmpty()) {
						break;
					}
					gen = stack.peek();
					genIndex--;
				}
				// Remove the current generator as it will be re-added in the for loop
				if(!stack.isEmpty()) {
					stack.pop();
				}
				// Generate the next possible combination, re-adding generators (that have been resetted)
				for(int i=genIndex; i < parameterGenerators.size(); i++) {
					gen = parameterGenerators.get(i);
					RValue newValue = gen.generate();
					parameters[i] = newValue;
					stack.push(gen);					
				}
			}
		}
		// TODO selection of tests if we cannot do all exhaustive combinations?
		
		assert stack.size() > 0;
		numTested++;
		return parameters;
	}
	
	
	
}
