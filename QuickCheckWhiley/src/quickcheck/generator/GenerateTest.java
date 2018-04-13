package quickcheck.generator;

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

	public GenerateTest(FunctionOrMethod dec) {
		super();
		this.dec = dec;
		this.parameterGenerators = new ArrayList<Generator>();
		// TODO get the generators for each parameter type
		for(Variable var : dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			if(paramType instanceof WhileyFile.Type.Int) {
				this.parameterGenerators.add(new Generator.IntegerGenerator("-1000", "1000"));
			}
			else if(paramType instanceof WhileyFile.Type.Bool) {
				this.parameterGenerators.add(new Generator.BooleanGenerator());
			}
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
}
