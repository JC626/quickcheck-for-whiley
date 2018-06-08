package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.BeforeClass;
import org.junit.Test;

import quickcheck.generator.GenerateTest;
import quickcheck.generator.RandomGenerateTest;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.util.AbstractCompilationUnit.*;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Array;
import wyil.interpreter.ConcreteSemantics.RValue.Record;
import wyil.interpreter.Interpreter;

/**
 * Test the random test generation
 * for generating random test data.
 * 
 * @author Janice Chin
 *
 */
public class GenerateRandomTest {
	/**
	 * This directory contains the source files for each test case. Every test
	 * corresponds to a file in this directory.
	 */
	private final static String TEST_DIR = "tests";
	
	private final static TestHelper helper = new TestHelper(TEST_DIR);
	/**
	 * Base interpreter used for the tests that do not require reading from a test file
	 */
	private static Interpreter baseInterpreter;

		
	@BeforeClass
	public static void setupClass() throws IOException {
		Build.Project project = helper.createProject();
		baseInterpreter = new Interpreter(project, System.out);
	}	
	
	
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
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
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
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}
	
	/**
	 * Test when the function has an int parameter with 
	 * an invalid range
	 */
	@Test(expected = Error.class)
	public void testFunctionIntInvalid() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(4);
		BigInteger upper = BigInteger.valueOf(4);
		new RandomGenerateTest(func, baseInterpreter, lower, upper);
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
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
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
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
	}
	
	/**
	 * Test when the function has multiple bool parameters
	 */
	@Test
	public void testFunctionMultiBoolParameters() {
		Decl.Variable boolOne = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Decl.Variable boolTwo = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable boolThree = new Decl.Variable(null, new Identifier("thirdBool"), Type.Bool);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolOne, boolTwo, boolThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
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
		Decl.Variable nullParam = new Decl.Variable(null, new Identifier("thirdNull"), Type.Null);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam, nullParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Null);
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
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Array);
	}
	
	/**
	 * Test when the function has a boolean array
	 */
	@Test
	public void testArraySingleBool() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for(int i=0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Bool);
		}
	}
	
	/**
	 * Test when the function has a integer array
	 */
	@Test
	public void testArraySingleInt() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for(int i=0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Int);
		}
	}
	
	/**
	 * Test when the function has multiple arrays,
	 * a boolean and a integer array
	 */
	@Test
	public void testMultiArray() {
		Decl.Variable boolArrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Decl.Variable intArrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolArrayParam, intArrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
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
			assertTrue(intElements[i] instanceof RValue.Int);
		}
	}
		
	/**
	 * Test when the function with a precondition
	 * for the nominal type on an integer.
	 */
	@Test
	public void testNominal1() throws IOException {
		String testName = "nominal_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}
	
	/**
	 * Test when the function with a postcondition
	 * for the nominal type on an integer.
	 */
	@Test
	public void testNominal2() throws IOException {
		String testName = "nominal_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}
	
	/**
	 * Test when the function has multiple nominal types,
	 * integer, boolean and boolean array.
	 */
	@Test
	public void testMultiNominal() throws IOException {
		String testName = "nominal_multi";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Array);
	}
	
	/**
	 * Test a record with different field types,
	 * two integer fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNominalSame() throws IOException {
		String testName = "nominal_same";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-3);
		BigInteger upper = BigInteger.valueOf(3);
		try {
			GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
			testGen.generateParameters();
			fail("Should not be able to generate parameters that are invalid");
		}
		catch(Error e) {}
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);

		lower = BigInteger.valueOf(0);
		upper = BigInteger.valueOf(20);
		testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}
	
	/**
	 * Test a record with different field types,
	 * boolean and integer.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRecord1() throws IOException {
		String testName = "record_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);	
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Record);
		RValue.Record record = (Record) generatedParameters[0];
		RValue first = record.read(new Identifier("x"));
		assertTrue(first instanceof RValue.Int);
		RValue second = record.read(new Identifier("y"));
		assertTrue(second instanceof RValue.Int);
	}
	
	/**
	 * Test a record with different field types,
	 * boolean and integer.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRecord2() throws IOException {
		String testName = "record_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);	
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Record);
		RValue.Record record = (Record) generatedParameters[0];
		RValue first = record.read(new Identifier("negate"));
		assertTrue(first instanceof RValue.Bool);
		RValue second = record.read(new Identifier("x"));
		assertTrue(second instanceof RValue.Int);
	}
	
	/**
	 * Test having multiple records in the function.
	 * One record has a boolean and integer field.
	 * The other record has two integer fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMultiRecord() throws IOException {
		String testName = "record_multi";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);	
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Record);
		assertTrue(generatedParameters[1] instanceof RValue.Record);
		
		RValue.Record recordPoint = (Record) generatedParameters[0];
		RValue first = recordPoint.read(new Identifier("x"));
		assertTrue(first instanceof RValue.Int);
		RValue second = recordPoint.read(new Identifier("y"));
		assertTrue(second instanceof RValue.Int);
		
		RValue.Record recordCounter = (Record) generatedParameters[1];
		first = recordCounter.read(new Identifier("negate"));
		assertTrue(first instanceof RValue.Bool);
		second = recordCounter.read(new Identifier("x"));
		assertTrue(second instanceof RValue.Int);
	}
	
	/**
	 * Test two records with the same field types
	 * and same field names.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRecordSame() throws IOException {
		String testName = "record_same";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);	
		GenerateTest testGen = new RandomGenerateTest(functions.get(0), interpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[1] instanceof RValue.Record);
		for(int i=0; i < 2; i++) {
			assertTrue(generatedParameters[i] instanceof RValue.Record);
			RValue.Record record = (Record) generatedParameters[i];
			RValue first = record.read(new Identifier("x"));
			assertTrue(first instanceof RValue.Int);
			RValue second = record.read(new Identifier("y"));
			assertTrue(second instanceof RValue.Int);
		}
	}
	
	/**
	 * Test generating null.
	 */
	@Test
	public void testNull() {
		Decl.Variable nullParam = new Decl.Variable(null, new Identifier("nullParam"), Type.Null);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(nullParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Null);
	}
	
	/**
	 * Test having a union for a 
	 * null and integer type.
	 */
	@Test
	public void testUnion1() {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"), new Type.Union(Type.Null, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Null)) {
			fail("Generated parameter from Union should be a Int or Null but was " + generatedParameters[0]);
		}
	}
	
	/**
	 * Test having a union for a
	 * boolean and integer type.
	 */
	@Test
	public void testUnion2() {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"), new Type.Union(Type.Bool, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
	}
	
	/**
	 * Test multiple unions,
	 * one union has boolean and integer type,
	 * the other union has a null and boolean array type.
	 */
	@Test
	public void testMultipleUnion() {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam1"), new Type.Union(Type.Bool, Type.Int));
		Decl.Variable unionParam2 = new Decl.Variable(null, new Identifier("unionParam2"), new Type.Union(Type.Null, new Type.Array(Type.Bool)));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam, unionParam2);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
		if(generatedParameters[1] instanceof RValue.Array) {
			RValue.Array boolArr = (Array) generatedParameters[1];
			RValue[] boolElements = boolArr.getElements();
			for(int i=0; i < boolElements.length; i++) {
				assertTrue(boolElements[i] instanceof RValue.Bool);
			}
		}
		else if (!(generatedParameters[1] instanceof RValue.Null)) {
			fail("Generated parameter from Union should be a Null or Boolean array but was " + generatedParameters[1]);
		}
	}
	
	/**
	 * Test having a union for that has multiple of the same types,
	 * boolean, integer, boolean, integer type.
	 */
	@Test
	public void testUnionSame() {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"), new Type.Union(Type.Bool, Type.Int, Type.Bool, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
	}
	
	/**
	 * Test when the function has 1 byte parameter
	 */
	@Test
	public void testByte() {
		Decl.Variable byteParam = new Decl.Variable(null, new Identifier("firstByte"), Type.Byte);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(byteParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new RandomGenerateTest(func, baseInterpreter, lower, upper);
		for(int i=0; i < 10; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertTrue(generatedParameters[0] instanceof RValue.Byte);
		}
	}
}
