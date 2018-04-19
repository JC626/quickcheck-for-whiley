package quickcheck.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	private BigInteger lowerLimit;
	private BigInteger upperLimit;
	
	public RandomGenerateTest(FunctionOrMethod dec, BigInteger lowerLimit, BigInteger upperLimit) {
		super();
		this.dec = dec;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.parameterGenerators = new ArrayList<Generator>();
		createGenerators();
	}	
	
	private void createGenerators() {
		// TODO get the generators for each parameter type
		for(Variable var : this.dec.getParameters()) {
			WhileyFile.Type paramType = var.getType();
			if(paramType instanceof WhileyFile.Type.Int) {
				this.parameterGenerators.add(new IntegerGenerator(TestType.RANDOM, lowerLimit, upperLimit));
			}
			else if(paramType instanceof WhileyFile.Type.Bool) {
				this.parameterGenerators.add(new BooleanGenerator(TestType.RANDOM));
			}
//			else if(paramType instanceof WhileyFile.Type.Array) {
//				WhileyFile.Type arrEle = ((WhileyFile.Type.Array) paramType).getElement();
//				// TODO get generator type
//			}
		}
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
