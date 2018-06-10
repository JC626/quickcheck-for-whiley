package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import quickcheck.RunTest;
import quickcheck.generator.type.*;
import quickcheck.util.TestType;
import wybs.lang.NameResolver.ResolutionError;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Decl.Variable;
import wyil.interpreter.Interpreter;
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
	
	private BigInteger totalCombinations;
	private int numTested;
	private int numTests; // Default number of tests to run
	private boolean allTests;
	
	private Interpreter interpreter;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;

	
	public ExhaustiveGenerateTest(FunctionOrMethod dec, Interpreter interpreter, int numTests, BigInteger lowerLimit, BigInteger upperLimit) {
		this.dec = dec;
		this.interpreter = interpreter;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.numTests = numTests;
		this.parameterGenerators = new ArrayList<Generator>();
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
	
	/**
	 * Get the generator based on a type
	 * @param paramType The type of a parameter for a function/method
	 * @return The generator that corresponds to the parameter's type
	 */
	private Generator getGenerator(WhileyFile.Type paramType) {
		if(paramType instanceof WhileyFile.Type.Int) {
			return new IntegerGenerator(TestType.EXHAUSTIVE, numTests, lowerLimit, upperLimit);
		}
		else if(paramType instanceof WhileyFile.Type.Bool) {
			return new BooleanGenerator(TestType.EXHAUSTIVE, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Byte) {
			return new ByteGenerator(TestType.EXHAUSTIVE, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Null) {
			return new NullGenerator();
		}
		else if(paramType instanceof WhileyFile.Type.Array) {
			WhileyFile.Type arrEle = ((WhileyFile.Type.Array) paramType).getElement();
			List<Generator> generators = new ArrayList<Generator>();
			for(int i=0; i < RunTest.ARRAY_UPPER_LIMIT; i++) {
				Generator gen = getGenerator(arrEle);
				generators.add(gen);
			}
			return new ArrayGenerator(generators, TestType.EXHAUSTIVE, numTests, RunTest.ARRAY_LOWER_LIMIT, RunTest.ARRAY_UPPER_LIMIT);
		}
		else if(paramType instanceof WhileyFile.Type.Nominal) {
			// Nominal generator takes another generator
			WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) paramType;
			try {
				Decl.Type decl = interpreter.getTypeSystem().resolveExactly(nom.getName(), Decl.Type.class);
				Decl.Variable var = decl.getVariableDeclaration();
				Generator gen = getGenerator(var.getType());
				return new NominalGenerator(gen, interpreter, decl);
			} catch (ResolutionError e) {
				// TODO What to do with resolution error?
				e.printStackTrace();
				assert false;
			}
		}
		else if(paramType instanceof WhileyFile.Type.Record) {
			WhileyFile.Type.Record record = (WhileyFile.Type.Record) paramType;
			Tuple<Decl.Variable> tuple = record.getFields();
			List<Generator> generators = new ArrayList<Generator>();
			List<Decl.Variable> fields = new ArrayList<Decl.Variable>();
			for(Decl.Variable var : tuple) {
				Generator gen = getGenerator(var.getType());
				fields.add(var);
				generators.add(gen);
			}
			return new RecordGenerator(generators, fields, TestType.EXHAUSTIVE, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Union) {
			WhileyFile.Type.Union union = (WhileyFile.Type.Union) paramType;
			// Decided not to use a Set as there are few generators
			// and it is highly unlikely a user would have a very large union
			List<Generator> generators = new ArrayList<Generator>();
			for(int i=0; i < union.size(); i++) {
				Generator gen = getGenerator(union.get(i));
				if(!generators.contains(gen)) {
					generators.add(gen);
				}
			}
			return new UnionGenerator(generators, TestType.EXHAUSTIVE, numTests);
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
