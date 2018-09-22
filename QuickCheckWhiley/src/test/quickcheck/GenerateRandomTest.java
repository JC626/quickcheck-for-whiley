package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import quickcheck.QCInterpreter;
import quickcheck.exception.IntegerRangeException;
import quickcheck.generator.GenerateTest;
import quickcheck.generator.RandomGenerateTest;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.AbstractCompilationUnit.*;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Array;
import wyil.interpreter.ConcreteSemantics.RValue.Record;
import wyil.interpreter.ConcreteSemantics.RValue.Reference;
import wyil.interpreter.Interpreter.CallStack;
import wyil.interpreter.Interpreter;

/**
 * Test the random test generation for generating random test data.
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
	 * Base interpreter used for the tests that do not require reading from a test
	 * file
	 */
	private static Interpreter baseInterpreter;

	@BeforeClass
	public static void setupClass() throws IOException {
		Build.Project project = helper.createProject();
		baseInterpreter = new QCInterpreter(project, System.out);
	}

	/**
	 * Test when the function has no parameters
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionNoParameters() throws IntegerRangeException {
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>();
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		Map<String, Object> generatorArgs = new HashMap<String, Object>();
		generatorArgs.put("upperLimit", "10");
		generatorArgs.put("lowerLimit", "-10");
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		assertArrayEquals(new RValue[0], testGen.generateParameters());
	}

	/**
	 * Test when the function has 1 int parameter
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionIntParameter() throws IntegerRangeException {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}

	/**
	 * Test when the function has an int parameter with an invalid range
	 * 
	 * @throws IntegerRangeException
	 */
	@Test(expected = IntegerRangeException.class)
	public void testFunctionIntInvalid() throws IntegerRangeException {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(4);
		BigInteger upper = BigInteger.valueOf(4);
		new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
	}

	/**
	 * Test when the function has multiple int parameters
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionMultiIntParameters() throws IntegerRangeException {
		Decl.Variable intOne = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable intTwo = new Decl.Variable(null, new Identifier("secInt"), Type.Int);
		Decl.Variable intThree = new Decl.Variable(null, new Identifier("thirdInt"), Type.Int);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intOne, intTwo, intThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Int);
		assertTrue(generatedParameters[2] instanceof RValue.Int);
	}

	/**
	 * Test when the function has 1 bool parameter
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionBoolParameter() throws IntegerRangeException {
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
	}

	/**
	 * Test when the function has multiple bool parameters
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionMultiBoolParameters() throws IntegerRangeException {
		Decl.Variable boolOne = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Decl.Variable boolTwo = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable boolThree = new Decl.Variable(null, new Identifier("thirdBool"), Type.Bool);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolOne, boolTwo, boolThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Bool);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Bool);

	}

	/**
	 * Test when the function has different parameter types, int and bool
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionDiffParameters1() throws IntegerRangeException {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable nullParam = new Decl.Variable(null, new Identifier("thirdNull"), Type.Null);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam, nullParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Null);
	}

	/**
	 * Test when the function has different parameter types, int, bool and array
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunctionDiffParameters2() throws IntegerRangeException {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam, arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Array);
	}

	/**
	 * Test when the function has a boolean array
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testArraySingleBool() throws IntegerRangeException {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for (int i = 0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Bool);
		}
	}

	/**
	 * Test when the function has a integer array
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testArraySingleInt() throws IntegerRangeException {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for (int i = 0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Int);
		}
	}

	/**
	 * Test when the function has a byte array
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testArraySingleByte() throws IntegerRangeException {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("byteArr"), new Type.Array(Type.Byte));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 1, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		RValue.Array arr = (Array) generatedParameters[0];
		RValue[] elements = arr.getElements();
		for (int i = 0; i < elements.length; i++) {
			assertTrue(elements[i] instanceof RValue.Byte);
		}
	}

	/**
	 * Test when the function has multiple arrays, a boolean and a integer array
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testMultiArray() throws IntegerRangeException {
		Decl.Variable boolArrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Decl.Variable intArrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolArrayParam, intArrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Array);
		assertTrue(generatedParameters[1] instanceof RValue.Array);
		RValue.Array boolArr = (Array) generatedParameters[0];
		RValue[] boolElements = boolArr.getElements();
		for (int i = 0; i < boolElements.length; i++) {
			assertTrue(boolElements[i] instanceof RValue.Bool);
		}

		RValue.Array intArr = (Array) generatedParameters[1];
		RValue[] intElements = intArr.getElements();
		for (int i = 0; i < intElements.length; i++) {
			assertTrue(intElements[i] instanceof RValue.Int);
		}
	}

	/**
	 * Test when the function with a precondition for the nominal type on an
	 * integer.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testNominal1() throws IOException, IntegerRangeException {
		String testName = "nominal_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}

	/**
	 * Test when the function with a postcondition for the nominal type on an
	 * integer.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testNominal2() throws IOException, IntegerRangeException {
		String testName = "nominal_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}

	/**
	 * Test when the function has multiple nominal types, integer, boolean and
	 * boolean array.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testMultiNominal() throws IOException, IntegerRangeException {
		String testName = "nominal_multi";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(3, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
		assertTrue(generatedParameters[1] instanceof RValue.Bool);
		assertTrue(generatedParameters[2] instanceof RValue.Array);
	}

	/**
	 * Test a record with different field types, two integer fields.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testNominalSame() throws IOException, IntegerRangeException {
		String testName = "nominal_same";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(11);
		try {
			GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower,
					upper);
			testGen.generateParameters();
			fail("Should not be able to generate parameters that are invalid");
		} catch (Error e) {
		}
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);

		lower = BigInteger.valueOf(0);
		upper = BigInteger.valueOf(20);
		testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}

	/**
	 * Test a record with different field types, boolean and integer.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testRecord1() throws IOException, IntegerRangeException {
		String testName = "record_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
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
	 * Test a record with different field types, boolean and integer.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testRecord2() throws IOException, IntegerRangeException {
		String testName = "record_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
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
	 * Test having multiple records in the function. One record has a boolean and
	 * integer field. The other record has two integer fields.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testMultiRecord() throws IOException, IntegerRangeException {
		String testName = "record_multi";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
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
	 * Test two records with the same field types and same field names.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testRecordSame() throws IOException, IntegerRangeException {
		String testName = "record_same";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[1] instanceof RValue.Record);
		for (int i = 0; i < 2; i++) {
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
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testNull() throws IntegerRangeException {
		Decl.Variable nullParam = new Decl.Variable(null, new Identifier("nullParam"), Type.Null);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(nullParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Null);
	}

	/**
	 * Test having a union for a null and integer type.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testUnion1() throws IntegerRangeException {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"),
				new Type.Union(Type.Null, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Null)) {
			fail("Generated parameter from Union should be a Int or Null but was " + generatedParameters[0]);
		}
	}

	/**
	 * Test having a union for a boolean and integer type.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testUnion2() throws IntegerRangeException {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"),
				new Type.Union(Type.Bool, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
	}

	/**
	 * Test multiple unions, one union has boolean and integer type, the other union
	 * has a null and boolean array type.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testMultipleUnion() throws IntegerRangeException {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam1"),
				new Type.Union(Type.Bool, Type.Int));
		Decl.Variable unionParam2 = new Decl.Variable(null, new Identifier("unionParam2"),
				new Type.Union(Type.Null, new Type.Array(Type.Bool)));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam, unionParam2);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
		if (generatedParameters[1] instanceof RValue.Array) {
			RValue.Array boolArr = (Array) generatedParameters[1];
			RValue[] boolElements = boolArr.getElements();
			for (int i = 0; i < boolElements.length; i++) {
				assertTrue(boolElements[i] instanceof RValue.Bool);
			}
		} else if (!(generatedParameters[1] instanceof RValue.Null)) {
			fail("Generated parameter from Union should be a Null or Boolean array but was " + generatedParameters[1]);
		}
	}

	/**
	 * Test having a union for that has multiple of the same types, boolean,
	 * integer, boolean, integer type.
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testUnionSame() throws IntegerRangeException {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParam"),
				new Type.Union(Type.Bool, Type.Int, Type.Bool, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if (!(generatedParameters[0] instanceof RValue.Int || generatedParameters[0] instanceof RValue.Bool)) {
			fail("Generated parameter from Union should be a Int or Bool but was " + generatedParameters[0]);
		}
	}

	/**
	 * Test when the function has 1 byte parameter
	 * 
	 * @throws IntegerRangeException
	 */
	@Test
	public void testByte() throws IntegerRangeException {
		Decl.Variable byteParam = new Decl.Variable(null, new Identifier("firstByte"), Type.Byte);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(byteParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), baseInterpreter, 10, lower, upper);
		for (int i = 0; i < 10; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertTrue(generatedParameters[0] instanceof RValue.Byte);
		}
	}

	/**
	 * Test a recursive type
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testRecursiveType1() throws IOException, IntegerRangeException {
		String testName = "recursive_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);

		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Record);
		RValue.Record record = (RValue.Record) generatedParameters[0];
		RValue first = record.read(new Identifier("data"));
		assertTrue(first instanceof RValue.Int);
		RValue second = record.read(new Identifier("n"));
		assertTrue(second instanceof RValue.Null || second instanceof RValue.Record);
	}

	/**
	 * Test a recursive type
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testRecursiveType2() throws IOException, IntegerRangeException {
		String testName = "recursive_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 10, lower, upper);

		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		if(!(generatedParameters[0] instanceof RValue.Null)) {
			assertTrue(generatedParameters[0] instanceof RValue.Record);
			RValue.Record record = (RValue.Record) generatedParameters[0];
			RValue first = record.read(new Identifier("data"));
			assertTrue(first instanceof RValue.Int);
			RValue second = record.read(new Identifier("n"));
			assertTrue(second instanceof RValue.Null || second instanceof RValue.Record);
		}
	}

	/**
	 * Test when there is only one reference generated
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testReference1() throws IOException, IntegerRangeException {
		String testName = "reference_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 25, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Reference);
		RValue.Reference refOne = (Reference) generatedParameters[0];
		assertTrue(refOne.deref().read() instanceof RValue.Int);
	}

	/**
	 * Test when there are two references generated which both have the same type
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testReference2() throws IOException, IntegerRangeException {
		String testName = "reference_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 25, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Reference);
		assertTrue(generatedParameters[1] instanceof RValue.Reference);
		RValue.Reference refOne = (Reference) generatedParameters[0];
		RValue.Reference refTwo = (Reference) generatedParameters[1];
		assertTrue(refOne.deref().read() instanceof RValue.Int);
		assertTrue((refTwo.deref().read() instanceof RValue.Int));
	}

	/**
	 * Test when there are two references generated which have different types.
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testReference3() throws IOException, IntegerRangeException {
		String testName = "reference_3";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new RandomGenerateTest(functions.get(0).getParameters(), interpreter, 25, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(2, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Reference);
		assertTrue(generatedParameters[1] instanceof RValue.Reference);
		RValue.Reference refOne = (Reference) generatedParameters[0];
		RValue.Reference refTwo = (Reference) generatedParameters[1];
		assertTrue(refOne.deref().read() instanceof RValue.Int);
		assertTrue((refTwo.deref().read() instanceof RValue.Bool));
	}

	/**
	 * Test when a function returns an int
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunction1() throws IOException, IntegerRangeException {
		String testName = "function_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), interpreter, 25, lower, upper);

		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		CallStack frame = interpreter.new CallStack();
		RValue[] paramValues = testGen.generateParameters();
		frame.putLocal(paramName, paramValues[0]);

		RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
		assertTrue(returns[0] instanceof RValue.Int);
	}

	/**
	 * Test when a function returns two types, an int and a boolean
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunction2() throws IOException, IntegerRangeException {
		String testName = "function_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), interpreter, 25, lower, upper);

		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		CallStack frame = interpreter.new CallStack();
		RValue[] paramValues = testGen.generateParameters();
		frame.putLocal(paramName, paramValues[0]);

		RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
		assertTrue(returns[0] instanceof RValue.Int);
		assertTrue(returns[1] instanceof RValue.Bool);
	}

	/**
	 * Test when a function returns a nominal with a constraint
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunction3() throws IOException, IntegerRangeException {
		String testName = "function_3";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), interpreter, 25, lower, upper);

		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();

		CallStack frame = interpreter.new CallStack();
		RValue[] paramValues = testGen.generateParameters();
		frame.putLocal(paramName, paramValues[0]);

		RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
		assertTrue(returns[0] instanceof RValue.Int);

		RValue.Int val = (RValue.Int) returns[0];

		boolean constraintApplied = false;
		for (int i = -5; i < 5; i++) {
			if (val.intValue() == i) {
				constraintApplied = true;
			}
		}
		assertTrue(constraintApplied);
	}

	/**
	 * Test when a function returns a record
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunction4() throws IOException, IntegerRangeException {
		String testName = "function_4";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), interpreter, 25, lower, upper);

		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		CallStack frame = interpreter.new CallStack();
		RValue[] paramValues = testGen.generateParameters();
		frame.putLocal(paramName, paramValues[0]);
		RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);

		RValue.Record record = (Record) returns[0];
		RValue first = record.read(new Identifier("x"));
		RValue second = record.read(new Identifier("y"));
		assertTrue(first instanceof RValue.Int);
		assertTrue(second instanceof RValue.Int);
	}

	/**
	 * Test when a function returns a union
	 * 
	 * @throws IOException
	 * @throws IntegerRangeException
	 */
	@Test
	public void testFunction5() throws IOException, IntegerRangeException {
		String testName = "function_5";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new RandomGenerateTest(func.getParameters(), interpreter, 25, lower, upper);

		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		CallStack frame = interpreter.new CallStack();
		RValue[] paramValues = testGen.generateParameters();
		frame.putLocal(paramName, paramValues[0]);

		RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
		if (!(returns[0] instanceof RValue.Null || returns[0] instanceof RValue.Int)) {
			fail("Did not generate correct type for the union: " + returns[0]); 
		}
	}
}
