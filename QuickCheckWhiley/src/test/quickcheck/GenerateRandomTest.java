package test.quickcheck;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import quickcheck.generator.RandomGenerateTest;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Test the GenerateTest class
 * @author Janice Chin
 *
 */
public class GenerateRandomTest {

	/**
	 * Test when the function has no parameters
	 */
	@Test
	public void testFunctionNoParameters() {
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>();
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
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
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
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
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Int);
		assertTrue(generatedParameters[2] instanceof RValue.Int);
	}
	
	/**
	 * Test when the function has 1 bool parameter
	 */
	@Test
	public void testFunctionBoolParameter() {
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
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
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Bool);

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
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		RandomGenerateTest testGen = new RandomGenerateTest(func, generatorArgs);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
	}
}
