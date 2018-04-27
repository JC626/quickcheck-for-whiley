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
import quickcheck.generator.type.NominalGenerator;
import quickcheck.util.TestType;
import wybs.lang.NameResolver.ResolutionError;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Decl.Variable;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.type.TypeSystem;

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
	
	private BigInteger totalCombinations;
	private int numTested;
	private int numTests; // Default number of tests to run
	private boolean allTests;
	
	private TypeSystem typeSystem;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;

	
	public ExhaustiveGenerateTest(FunctionOrMethod dec, TypeSystem typeSystem, int numTests, BigInteger lowerLimit, BigInteger upperLimit) {
		this.dec = dec;
		this.typeSystem = typeSystem;
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
		else if(paramType instanceof WhileyFile.Type.Nominal) {
			// Nominal generator takes another generator
			WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) paramType;
			try {
				Decl.Type decl = typeSystem.resolveExactly(nom.getName(), Decl.Type.class);
				Decl.Variable var = decl.getVariableDeclaration();
				Generator gen = getGenerator(var.getType());
				return new NominalGenerator(gen);
			} catch (ResolutionError e) {
				// TODO What to do with resolution error?
				e.printStackTrace();
				assert false;
			}
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
				}
			}
			else{
	            /*
	             *  Generate the array elements backwards.
	             *  If the last generator has reached it's upper limit 
	             *  (i.e. we cannot move onto the next combination) then reset the generator.
	             *  Repeat for all previous generators that have reached its limit,
	             *  until we reach a generator which hasn't reached it's limit.
	             */
				for(int i=parameters.length - 1; i >= 0 ; i--) {
					Generator gen = parameterGenerators.get(i);
					if(!gen.exceedCount()) {
						parameters[i] = gen.generate();
						break;
					}
					else {
						gen.resetCount();
						parameters[i] = gen.generate();
					}
				}
			}
		}
		// TODO selection of tests if we cannot do all exhaustive combinations?
		numTested++;
		return parameters;
	}
	
	
	
}