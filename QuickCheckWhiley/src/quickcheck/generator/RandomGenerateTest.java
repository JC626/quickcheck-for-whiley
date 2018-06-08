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
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter;

/**
 * Generate candidate test parameters for a function randomly.
 *  
 * @author Janice Chin
 *
 */
public class RandomGenerateTest implements GenerateTest{
	
	/** The function/method we want to test */
	private Decl.FunctionOrMethod dec;
	
	/** A list of generators, each corresponding to a parameter in the function/method */
	private List<Generator> parameterGenerators;
	
	private Interpreter interpreter;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;
	
    private int numTests;
		
    public RandomGenerateTest(FunctionOrMethod dec, Interpreter interpreter, int numTests, BigInteger lowerLimit, BigInteger upperLimit) {
		super();
		this.dec = dec;
        this.numTests = numTests;
		this.interpreter = interpreter;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.parameterGenerators = new ArrayList<Generator>();		
		// Get the generators
		for(Variable var : this.dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			this.parameterGenerators.add(getGenerator(paramType));
		}
	}	

	/**
	 * Get the generator based on a type
	 * @param paramType The type of a parameter for a function/method
	 * @return The generator that corresponds to the parameter's type
	 */
	private Generator getGenerator(WhileyFile.Type paramType) {
		if(paramType instanceof WhileyFile.Type.Int) {
			return new IntegerGenerator(TestType.RANDOM, numTests, lowerLimit, upperLimit);
		}
		else if(paramType instanceof WhileyFile.Type.Bool) {
			return new BooleanGenerator(TestType.RANDOM);
		}
		else if(paramType instanceof WhileyFile.Type.Byte) {
			return new ByteGenerator(TestType.RANDOM);
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
			return new ArrayGenerator(generators, TestType.RANDOM, RunTest.ARRAY_LOWER_LIMIT, RunTest.ARRAY_UPPER_LIMIT);
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
			return new RecordGenerator(generators, fields, TestType.EXHAUSTIVE);
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
			return new UnionGenerator(generators, TestType.RANDOM);
		}
		assert false;
		return null;
	}
	
	@Override
	public RValue[] generateParameters() {
		// Iterate through the generators to generate the parameters
		RValue[] parameters = new RValue[parameterGenerators.size()];
		for(int i=0; i < parameterGenerators.size(); i++) {
			parameters[i] = parameterGenerators.get(i).generate();
		}
		return parameters;
	}
	
}
