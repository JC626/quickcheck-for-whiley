package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import quickcheck.RunTest;
import quickcheck.generator.type.ArrayGenerator;
import quickcheck.generator.type.BooleanGenerator;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import quickcheck.generator.type.NominalGenerator;
import quickcheck.util.TestType;
import wybs.lang.NameResolver.ResolutionError;
import wybs.util.AbstractCompilationUnit.Name;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Decl.Variable;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.type.TypeSystem;

/**
 * Generate candidate test parameters for a function.
 * The default is random test generation.
 *  
 * @author Janice Chin
 *
 */
public class RandomGenerateTest implements GenerateTest{
	
	/**
	 * The function/method we want to test
	 */
	private Decl.FunctionOrMethod dec;
	
	/**
	 *  A list of generators, each corresponding to a parameter in the function/method
	 */
	private List<Generator> parameterGenerators;
	
	private TypeSystem typeSystem;
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;
		
	public RandomGenerateTest(FunctionOrMethod dec, TypeSystem typeSystem, BigInteger lowerLimit, BigInteger upperLimit) {
		super();
		this.dec = dec;
		this.typeSystem = typeSystem;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.parameterGenerators = new ArrayList<Generator>();
		// Get the generators
		for(Variable var : this.dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			this.parameterGenerators.add(getGenerator(paramType));
		}
	}	

	private Generator getGenerator(WhileyFile.Type paramType) {
		if(paramType instanceof WhileyFile.Type.Int) {
			return new IntegerGenerator(TestType.RANDOM, lowerLimit, upperLimit);
		}
		else if(paramType instanceof WhileyFile.Type.Bool) {
			return new BooleanGenerator(TestType.RANDOM);
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
		// Iterate through the generators to generate the parameters
		RValue[] parameters = new RValue[parameterGenerators.size()];
		for(int i=0; i < parameterGenerators.size(); i++) {
			parameters[i] = parameterGenerators.get(i).generate();
		}
		return parameters;
	}
	
}
