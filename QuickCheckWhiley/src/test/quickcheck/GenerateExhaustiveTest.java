package test.quickcheck;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Test the exhaustive test generation
 * for generating all combinations of possible input data.
 * 
 * @author Janice Chin
 *
 */
public class GenerateExhaustiveTest {
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	/**
	 * Test when the function has no parameters
	 */
	@Test
	public void testFunctionNoParameters() {
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>();
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 10, lower, upper);
		assertArrayEquals(new RValue[0], testGen.generateParameters());
	}
	
	/**
	 * Test when the function has 1 int parameter
	 */
	@Test
	public void testFunctionIntParameter() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 10, lower, upper);
		for(int i=-2; i <= 3; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}

	}
	
	/**
	 * Test when the function has multiple int parameters
	 */
	@Test
	public void testFunctionMultiIntParameters() {
		Decl.Variable intOne = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable intTwo = new Decl.Variable(null, new Identifier("secInt"), Type.Int);
		Decl.Variable intThree = new Decl.Variable(null, new Identifier("thirdInt"), Type.Int);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intOne, intTwo, intThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 18, lower, upper);
		for(int i=-2; i <= 3; i++) {
			for(int j=-2; j <= 3; j++) {
				for(int k=-2; k <= 3; k++) {
					RValue[] generatedParameters = testGen.generateParameters();
					assertEquals(3, generatedParameters.length);
					assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
					assertEquals(semantics.Int(BigInteger.valueOf(j)), generatedParameters[1]);
					assertEquals(semantics.Int(BigInteger.valueOf(k)), generatedParameters[2]);
				}
			}
		}
	}
	
	/**
	 * Test when the function has 1 bool parameter
	 */
	@Test
	public void testFunctionBoolParameter() {
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 5, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(true), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(false), generatedParameters[0]);
	}
	
	/**
	 * Test when the function has multiple bool parameters
	 */
	@Test
	public void testFunctionMultipleBoolParameters() {
		Decl.Variable boolOne = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Decl.Variable boolTwo = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable boolThree = new Decl.Variable(null, new Identifier("thirdBool"), Type.Bool);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolOne, boolTwo, boolThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 10, lower, upper);
		for(int i=0; i <= 1; i++) {
			for(int j=0; j <= 1; j++) {
				for(int k=0; k <= 1; k++) {
					RValue[] generatedParameters = testGen.generateParameters();
					assertEquals(3, generatedParameters.length);
					assertEquals(semantics.Bool(i==0), generatedParameters[0]);
					assertEquals(semantics.Bool(j==0), generatedParameters[1]);
					assertEquals(semantics.Bool(k==0), generatedParameters[2]);
				}
			}
		}
	}
	
	/**
	 * Test when the function has different parameter types
	 */
	@Test
	public void testFunctionDiffParameters() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, 10, lower, upper);
		for(int i=-2; i <= 3; i++) {
			for(int j=0; j <= 1; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(2, generatedParameters.length);
				assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
				assertEquals(semantics.Bool(j==0), generatedParameters[1]);
			}
		}
		// Switch the parameters around
		parameters = new Tuple<Decl.Variable>(boolParam, intParam);
		func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		testGen = new ExhaustiveGenerateTest(func, 10, lower, upper);
		for(int i=0; i <= 1; i++) {
			for(int j=-2; j <= 3; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(2, generatedParameters.length);
				assertEquals(semantics.Bool(i==0), generatedParameters[0]);
				assertEquals(semantics.Int(BigInteger.valueOf(j)), generatedParameters[1]);
			}
		}

	}
	
}
