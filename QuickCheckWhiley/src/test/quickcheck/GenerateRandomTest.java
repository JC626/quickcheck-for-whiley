package test.quickcheck;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import quickcheck.generator.GenerateTest;
import quickcheck.generator.RandomGenerateTest;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Array;

/**
 * Test the random test generation
 * for generating random test data.
 * 
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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Bool);

	}
	
	/**
	 * Test when the function has different parameter types,
	 * int and bool
	 */
	@Test
	public void testFunctionDiffParameters1() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
	}
	
	/**
	 * Test when the function has different parameter types,
	 * int, bool and array
	 */
	@Test
	public void testFunctionDiffParameters2() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam, arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Array);
	}
	
	@Test
	public void testArraySingleBool() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for(int i=0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Bool);
		}
	}
	
	@Test
	public void testArraySingleInt() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for(int i=0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Int);
		}
	}
	
	@Test
	public void testMultiArray() {
		Decl.Variable boolArrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Decl.Variable intArrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolArrayParam, intArrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		assertTrue(generatedParameters[1] instanceof RValue.Array);
		RValue.Array boolArr = (Array) generatedParameters[0];
		RValue[] boolElements = boolArr.getElements();
		for(int i=0; i < boolElements.length; i++) {
			assertTrue(boolElements[i] instanceof RValue.Bool);
		}
		
		RValue.Array intArr = (Array) generatedParameters[1];
		RValue[] intElements = intArr.getElements();
		for(int i=0; i < intElements.length; i++) {
			assertTrue(boolElements[i] instanceof RValue.Bool);
		}
	}
}
