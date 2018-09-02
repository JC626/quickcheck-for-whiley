package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
	
	/**Combinations for random test generation*/
	private List<Integer> testCombos;
	
	private Interpreter interpreter;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;

	
	private BigInteger totalCombinations;
	private int numTested = 1;
    private int numTests;
    
	/** All the user created types that are recursive structures */
	private Map<Name, Integer> recursiveType = new HashMap<Name, Integer>();
	/** All the user created types that are recursive array structures */
	private Map<Name, Integer> recursiveArray = new HashMap<Name, Integer>();

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
		// Number of combinations
		if(parameterGenerators.isEmpty()) {
			this.totalCombinations = BigInteger.valueOf(0);
		}
		else {
			int size = 1;
			for(Generator gen : parameterGenerators) {
				size *= gen.size();
			}
			this.totalCombinations = BigInteger.valueOf(size);
		}
		testCombos = new ArrayList<Integer>();
		if(numTests >= totalCombinations.intValue()) {
			for(int i=0; i < totalCombinations.intValue(); i++) {
				testCombos.add(i);
			}
		}
		else {
			// FIXME this takes too long!
			// Random inputs use Knuth's Algorithm S
			Random randomiser = new Random(); 
			int nextCombo = 0;
			int selected = 0; 
			while(selected < numTests) {
				double uniform = randomiser.nextDouble();
				if((this.totalCombinations.intValue() - nextCombo)*uniform >= numTests - selected) {
					nextCombo++;
				}
				else {
					testCombos.add(nextCombo);
					nextCombo++;
					selected++;
				}
				if(nextCombo >= this.totalCombinations.intValue()) {
					nextCombo = 0;
				}
			}
			// Shuffle test values so they are not in order
			Collections.shuffle(testCombos);
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
			Name nomName = null;
			if(arrEle instanceof WhileyFile.Type.Nominal) {
				nomName  = ((WhileyFile.Type.Nominal) arrEle).getName();
				recursiveArray.put(nomName, recursiveType.getOrDefault(nomName, 0) + 1);
			}
			for(int i=0; i < RunTest.ARRAY_UPPER_LIMIT; i++) {
				Generator gen = getGenerator(arrEle);
				generators.add(gen);
			}
			if(nomName != null) {
				recursiveArray.put(nomName, recursiveType.getOrDefault(nomName, 0) - 1);
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
				recursiveType.put(name, recursiveType.getOrDefault(name, 0) + 1);
//				if(recursiveType.get(name) > RunTest.RECURSIVE_LIMIT) {
//					return new NullGenerator();
//				}
				Generator gen = getGenerator(var.getType());
				recursiveType.put(name, recursiveType.get(name) - 1);
				if(recursiveType.get(name) == 0) {
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
					if(recursiveType.containsKey(nom.getName()) && recursiveType.get(nom.getName()) > RunTest.RECURSIVE_LIMIT) {
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
								if(recursiveType.containsKey(nom.getName()) && recursiveType.get(nom.getName()) > RunTest.RECURSIVE_LIMIT) {
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
				} else if(unionFieldType instanceof WhileyFile.Type.Array) {
					WhileyFile.Type.Array arr = (WhileyFile.Type.Array) unionFieldType;
					if(arr.getElement() instanceof WhileyFile.Type.Nominal) {
						WhileyFile.Type.Nominal nom = (WhileyFile.Type.Nominal) arr.getElement();
						Name nomName = nom.getName();
						if(recursiveArray.containsKey(nomName) && recursiveArray.get(nomName) > RunTest.RECURSIVE_ARRAY_LIMIT) {
							limitReached = true;
							// No longer be able to generate the nominal type
							break;	
						}
						else if(recursiveType.containsKey(nomName) && recursiveType.get(nomName) > RunTest.RECURSIVE_LIMIT) {
							limitReached = true;
							// No longer be able to generate the nominal type
							break;
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
			if(generators.size() == 1) {
				return generators.get(0);
			}
			return new UnionGenerator(generators, TestType.RANDOM, numTests);
		}
		else if(paramType instanceof WhileyFile.Type.Reference) {
			WhileyFile.Type.Reference ref = (WhileyFile.Type.Reference) paramType;
			Generator gen = getGenerator(ref.getElement());
			return new ReferenceGenerator(gen);
		}
		else if(paramType instanceof WhileyFile.Type.Function) {
			WhileyFile.Type.Function func = (WhileyFile.Type.Function) paramType;
			List<Generator> generators = new ArrayList<Generator>();
			for(WhileyFile.Type type : func.getReturns()) {
				generators.add(getGenerator(type));
			}
			return new FunctionGenerator(generators, func, interpreter, TestType.RANDOM, numTests);
		}
		assert false;
		return null;
	}
	
	@Override
	public RValue[] generateParameters() {
//        RValue[] parameters = new RValue[parameterGenerators.size()];
//		for(int i=0; i < parameterGenerators.size(); i++) {
//            parameters[i] = parameterGenerators.get(i).generate();
//        }
//        return parameters;
		if(exceedSize()) {
 			Random randomiser = new Random(); 
			int nextCombo = 0;
			int selected = 0; 
			while(true) {
				double uniform = randomiser.nextDouble();
				if((this.totalCombinations.intValue() - nextCombo)*uniform >= 1 - selected) {
					nextCombo++;
				}
				else {
					return generateCombination(nextCombo);
				}
				if(nextCombo >= this.totalCombinations.intValue()) {
					nextCombo = 0;
				}
			}
 		}
		else {
			int index = numTested - 1;
			numTested++;
			return generateCombination(testCombos.get(index));
		}
	}
	
	private RValue[] generateCombination(int comboNum) {
		RValue[] parameters = new RValue[parameterGenerators.size()];
		int leftover = comboNum;
		for(int i=0; i < parameters.length ; i++) {
			int divNum = 1;
			for(int j = i+1; j < parameters.length; j++ ) {
				Generator gen = parameterGenerators.get(j);
				divNum *= gen.size();
			}
			if(i+1 >= parameters.length) {
				divNum = 0;
			}
			int num = leftover;
			// Note: Num is always rounded down
			if(divNum != 0) {
				num /= divNum;
			}
			Generator gen = parameterGenerators.get(i);
			parameters[i] = gen.generate(num);
			leftover -= num * divNum;
		}
		return parameters;
	}

	@Override
	public boolean exceedSize() {
		return numTested > totalCombinations.intValue();
	}
	
}
