package quickcheck.generator;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import wyc.lang.WhileyFile;
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
public class ExhaustiveGenerateTest extends GenerateTest{
	private BigInteger totalCombinations;
	private int numTested;
	private int numTests; // Default number of tests to run
	private boolean allTests;
	/**
	 * The last parameters used to create a test
	 */
	private RValue[] parameters;
	/**
	 * Stores the generators used in an iterative manner.
	 */
	private Stack<Generator> stack;
	
	
	
	public ExhaustiveGenerateTest(FunctionOrMethod dec, Map<String, Object> keywordArgs) {
		super(dec, keywordArgs);
		this.numTests = 10;
		this.allTests = totalCombinations.compareTo(BigInteger.valueOf(numTests)) != 1;
		stack = new Stack<Generator>();	
	}

	
	public ExhaustiveGenerateTest(FunctionOrMethod dec, Map<String, Object> keywordArgs, int numTests) {
		super(dec, keywordArgs);
		this.numTests = numTests;
		this.allTests = totalCombinations.compareTo(BigInteger.valueOf(numTests)) != 1;
		stack = new Stack<Generator>();
	}

	@Override
	protected void createGenerators() {
		// TODO get the generators for each parameter type
		BigInteger numCombinations = BigInteger.valueOf(1);
		List<Generator> parameterGenerators = getParameterGenerators();
		for(Variable var : getDec().getParameters()) {
			WhileyFile.Type paramType = var.getType();
			if(paramType instanceof WhileyFile.Type.Int) {
				parameterGenerators.add(new Generator.IntegerGenerator(TestType.EXHAUSTIVE, BigInteger.valueOf(-5), BigInteger.valueOf(5)));
				numCombinations.multiply(BigInteger.valueOf(2000));
			}
			else if(paramType instanceof WhileyFile.Type.Bool) {
				parameterGenerators.add(new Generator.BooleanGenerator(TestType.EXHAUSTIVE));
				numCombinations.multiply(BigInteger.valueOf(2));
			}
		}
		if(parameterGenerators.isEmpty()) {
			this.totalCombinations = new BigInteger("0");
		}
		else {
			this.totalCombinations = numCombinations;
		}
		parameters = new RValue[parameterGenerators.size()];
	}

	@Override
	public RValue[] generateParameters() {
		// Brute force generate parameters, iteratively 
		// Keep the previous state
		List<Generator> parameterGenerators = getParameterGenerators();
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
