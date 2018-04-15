package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import quickcheck.generator.Generator;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Decl.Variable;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Generate candidate test parameters for a function.
 * The default is random test generation.
 *  
 * @author Janice Chin
 *
 */
public class GenerateTest {
	
	/**
	 * The function/method we want to test
	 */
	private Decl.FunctionOrMethod dec;

	/**
	 *  A list of generators, each corresponding to a parameter in the function/method
	 */
	private List<Generator> parameterGenerators;

	// TODO Need to be able to pass multiple arguments? For limits on Integer generators etc?
	public GenerateTest(FunctionOrMethod dec) {
		super();
		this.dec = dec;
		this.parameterGenerators = new ArrayList<Generator>();
		createGenerators();
	}	
	
	protected void createGenerators() {
		// TODO get the generators for each parameter type
		for(Variable var : this.dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			if(paramType instanceof WhileyFile.Type.Int) {
				this.parameterGenerators.add(new Generator.IntegerGenerator(TestType.RANDOM, BigInteger.valueOf(-1000), BigInteger.valueOf(1000)));
			}
			else if(paramType instanceof WhileyFile.Type.Bool) {
				this.parameterGenerators.add(new Generator.BooleanGenerator(TestType.RANDOM));
			}
//			else if(paramType instanceof WhileyFile.Type.Array) {
//				WhileyFile.Type arrEle = ((WhileyFile.Type.Array) paramType).getElement();
//				// TODO get generator type
//			}
		}
	}
	
	/**
	 * Generate parameters to be used as a single test for the function.
	 * @return The parameters for the function.
	 */
	public RValue[] generateParameters() {
		// Iterate through the generators to generate the parameters
		RValue[] parameters = new RValue[parameterGenerators.size()];
		for(int i=0; i < parameterGenerators.size(); i++) {
			parameters[i] = parameterGenerators.get(i).generate();
		}
		return parameters;
	}
	
	// Getters
	
	/**
	 * Get the function/method used for generating the tests
	 * @return Function/Method
	 */
	public Decl.FunctionOrMethod getDec() {
		return dec;
	}
	
	/**
	 * Get the generators used for generating test data
	 * @return Generators corresponding to each parameter type in the function/method
	 */
	public List<Generator> getParameterGenerators() {
		return parameterGenerators;
	}

}
