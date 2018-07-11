package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import quickcheck.RunTest;
import quickcheck.exception.IntegerRangeException;
import quickcheck.generator.type.*;
import quickcheck.util.TestType;
import wybs.lang.NameResolver.ResolutionError;
import wybs.util.AbstractCompilationUnit.Name;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
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
	
	/** A list of generators, each corresponding to a parameter in the function/method */
	private List<Generator> parameterGenerators;
	
	private Interpreter interpreter;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;
	
    private int numTests;
	private Map<Name, Integer> recursiveType = new HashMap<Name, Integer>();
		
    
    // Allows variables for the generators to be passed in instead of 
    public RandomGenerateTest(Tuple<Decl.Variable> valuesToGenerate, Interpreter interpreter, int numTests, BigInteger lowerLimit, BigInteger upperLimit) throws IntegerRangeException {
		super();
        this.numTests = numTests;
		this.interpreter = interpreter;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.parameterGenerators = new ArrayList<Generator>();		
		// Get the generators
		for(Variable var : valuesToGenerate) {
			WhileyFile.Type paramType = var.getType();
			this.parameterGenerators.add(getGenerator(paramType));
		}
	}	

	/**
	 * Get the generator based on a type
	 * @param paramType The type of a parameter for a function/method
	 * @return The generator that corresponds to the parameter's type
	 * @throws IntegerRangeException 
	 */
	private Generator getGenerator(WhileyFile.Type paramType) throws IntegerRangeException {
		if(paramType instanceof WhileyFile.Type.Int) {
			return new IntegerGenerator(TestType.RANDOM, numTests, lowerLimit, upperLimit);
		}
		else if(paramType instanceof WhileyFile.Type.Bool) {
			return new BooleanGenerator(TestType.RANDOM, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Byte) {
			return new ByteGenerator(TestType.RANDOM, numTests);
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
			return new ArrayGenerator(generators, TestType.RANDOM, numTests, RunTest.ARRAY_LOWER_LIMIT, RunTest.ARRAY_UPPER_LIMIT);
		}
		else if(paramType instanceof WhileyFile.Type.Nominal) {
			// Nominal generator takes another generator
			WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) paramType;
			try {
				Decl.Type decl = interpreter.getTypeSystem().resolveExactly(nom.getName(), Decl.Type.class);
				Decl.Variable var = decl.getVariableDeclaration();
				Name name = nom.getName();
				recursiveType.put(name, recursiveType.getOrDefault(name, -1) + 1);
				if(recursiveType.get(name) > RunTest.RECURSIVE_LIMIT) {
					return new NullGenerator();
				}
				Generator gen = getGenerator(var.getType());
				if(recursiveType.get(name) == -1) {
					recursiveType.remove(name);
				}
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
			return new RecordGenerator(generators, fields, TestType.RANDOM, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Union) {
			boolean limitReached = false;
			WhileyFile.Type.Union union = (WhileyFile.Type.Union) paramType;
			// Decided not to use a Set as there are few generators
			// and it is highly unlikely a user would have a very large union
			List<Generator> generators = new ArrayList<Generator>();
			for(int i=0; i < union.size(); i++) {
				WhileyFile.Type unionFieldType = union.get(i);
				if(unionFieldType instanceof WhileyFile.Type.Nominal) {
					WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) unionFieldType;
					if(recursiveType.getOrDefault(nom.getName(), 0) >= RunTest.RECURSIVE_LIMIT) {
						limitReached = true;
						// No longer be able to generate the nominal type
						break;
					}
				}
				// Check nominals in the record (and in nested records), are/are not recursive types
				else if(unionFieldType instanceof WhileyFile.Type.Record) {
					Stack<WhileyFile.Type.Record> stack = new Stack<WhileyFile.Type.Record>();
					stack.push((WhileyFile.Type.Record) unionFieldType);
					while(!stack.isEmpty()) {
						WhileyFile.Type.Record record = stack.pop();
						Tuple<Decl.Variable> tuple = record.getFields();
						for(Decl.Variable var : tuple) {
							if(var.getType() instanceof WhileyFile.Type.Nominal) {
								WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) var.getType();
								if(recursiveType.getOrDefault(nom.getName(), 0) >= RunTest.RECURSIVE_LIMIT) {
									limitReached = true;
									// No longer be able to generate the nominal type
									break;
								}
							}
							else if(var.getType() instanceof WhileyFile.Type.Nominal) {
								stack.push((WhileyFile.Type.Record) var.getType());
							}
						}
					}
				}
				if(!limitReached) {
					Generator gen = getGenerator(unionFieldType);
					if(!generators.contains(gen)) {
						generators.add(gen);
					}
				}
			}
			return new UnionGenerator(generators, TestType.RANDOM, numTests);
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
