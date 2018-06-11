package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Record;

/**
 * Test the exhaustive test generation
 * for generating all combinations of possible test values.
 * 
 * @author Janice Chin
 *
 */
public class GenerateExhaustiveTest {
	/**
	 * This directory contains the source files for each test case. Every test
	 * corresponds to a file in this directory.
	 */
	private final static String TEST_DIR = "tests";
	
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	private static RValue[][] boolCombinations = {{}, {RValue.True}, {RValue.False}, 
			   										  {RValue.True, RValue.True}, {RValue.True, RValue.False},
			   										  {RValue.False, RValue.True}, {RValue.False, RValue.False}, 
			   										  {RValue.True, RValue.True, RValue.True}, {RValue.True, RValue.True, RValue.False},
			   										  {RValue.True, RValue.False, RValue.True}, {RValue.True, RValue.False, RValue.False},
			   										  {RValue.False, RValue.True, RValue.True}, {RValue.False, RValue.True, RValue.False},
			   										  {RValue.False, RValue.False, RValue.True}, {RValue.False, RValue.False, RValue.False}};

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
		BigInteger lower = BigInteger.valueOf(-10);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
		for(int i=-2; i <= 3; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}

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
		new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 125, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 5, lower, upper);
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
	public void testFunctionMultiBoolParameters() {
		Decl.Variable boolOne = new Decl.Variable(null, new Identifier("firstBool"), Type.Bool);
		Decl.Variable boolTwo = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Decl.Variable boolThree = new Decl.Variable(null, new Identifier("thirdBool"), Type.Bool);

		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolOne, boolTwo, boolThree);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
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
	 * Test when the function has different parameter types,
	 * int and bool
	 */
	@Test
	public void testFunctionDiffParameters1() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Decl.Variable nullParam = new Decl.Variable(null, new Identifier("secNull"), Type.Null);
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("thirdBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, nullParam, boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
		for(int i=-2; i <= 3; i++) {
			for(int j=0; j <= 1; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(3, generatedParameters.length);
				assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
				assertEquals(semantics.Null(), generatedParameters[1]);
				assertEquals(semantics.Bool(j==0), generatedParameters[2]);
			}
		}
		// Switch the parameters around
		parameters = new Tuple<Decl.Variable>(boolParam, intParam);
		func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
		for(int i=0; i <= 1; i++) {
			for(int j=-2; j <= 3; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(2, generatedParameters.length);
				assertEquals(semantics.Bool(i==0), generatedParameters[0]);
				assertEquals(semantics.Int(BigInteger.valueOf(j)), generatedParameters[1]);
			}
		}
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
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 90, lower, upper);
		for(int i=0; i < 3; i++) {
			for(int j=0; j <= 1; j++) {
				for(int k=0; k < boolCombinations.length; k++) {
					RValue[] generatedParameters = testGen.generateParameters();
					assertEquals(3, generatedParameters.length);
					assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
					assertEquals(semantics.Bool(j==0), generatedParameters[1]);
					assertEquals(semantics.Array(boolCombinations[k]), generatedParameters[2]);
				}				
			}
		}
	}

	/**
	 * Test when the function has a boolean array
	 */
	@Test
	public void testFunctionArraySingleBool() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 15, lower, upper);
		for(int i=0; i < boolCombinations.length; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Array(boolCombinations[i]), generatedParameters[0]);
		}
	}
	
	/**
	 * Test when the function has a integer array
	 */
	@Test
	public void testFunctionArraySingleInt() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 40, lower, upper);
		// Empty
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Array(new RValue[0]), generatedParameters[0]);
		// Single
		for(int i=lower.intValue(); i < upper.intValue(); i++) {
			generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			RValue[] expected = {semantics.Int(BigInteger.valueOf(i))};
			assertEquals(semantics.Array(expected), generatedParameters[0]);
		}
		// 2 elements
		for(int i=lower.intValue(); i < upper.intValue(); i++) {
			for(int j=lower.intValue(); j < upper.intValue(); j++) {
				generatedParameters = testGen.generateParameters();
				assertEquals(1, generatedParameters.length);
				RValue[] expected = {semantics.Int(BigInteger.valueOf(i)), semantics.Int(BigInteger.valueOf(j))};
				assertEquals(semantics.Array(expected), generatedParameters[0]);
			}
	
		}
		// 3 elements
		for(int i=lower.intValue(); i < upper.intValue(); i++) {
			for(int j=lower.intValue(); j < upper.intValue(); j++) {
				for(int k=lower.intValue(); k < upper.intValue(); k++) {
					generatedParameters = testGen.generateParameters();
					assertEquals(1, generatedParameters.length);
					RValue[] expected = {semantics.Int(BigInteger.valueOf(i)), semantics.Int(BigInteger.valueOf(j)), semantics.Int(BigInteger.valueOf(k))};
					assertEquals(semantics.Array(expected), generatedParameters[0]);
				}
			}
		}
	}
	
	/**
	 * Test when the function has a byte array	
	 */
	@Test
	public void testArraySingleByte() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("byteArr"), new Type.Array(Type.Byte));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 40, lower, upper);
		// Empty
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Array(new RValue[0]), generatedParameters[0]);
		// Single
		for(int i=0; i < 256; i++) {
			generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			RValue[] expected = {semantics.Byte((byte) i)};
			assertEquals(semantics.Array(expected), generatedParameters[0]);
		}
		// 2 elements
		for(int i=0; i < 256; i++) {
			for(int j=0; j < 256; j++) {
				generatedParameters = testGen.generateParameters();
				assertEquals(1, generatedParameters.length);
				RValue[] expected = {semantics.Byte((byte) i), semantics.Byte((byte) j)};
				assertEquals(semantics.Array(expected), generatedParameters[0]);
			}
	
		}
		// 3 elements
		for(int i=0; i < 256; i++) {
			for(int j=0; j < 256; j++) {
				for(int k=0; k < 256; k++) {
					generatedParameters = testGen.generateParameters();
					assertEquals(1, generatedParameters.length);
					RValue[] expected = {semantics.Byte((byte) i), semantics.Byte((byte) j), semantics.Byte((byte) k)};
					assertEquals(semantics.Array(expected), generatedParameters[0]);
				}
			}
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
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 600, lower, upper);
		
		for(int n=0; n < boolCombinations.length; n++) {
			RValue boolCombo = semantics.Array(boolCombinations[n]);
			// Empty
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(2, generatedParameters.length);
			assertEquals(boolCombo, generatedParameters[0]);
			assertEquals(semantics.Array(new RValue[0]), generatedParameters[1]);
			// Single
			for(int i=lower.intValue(); i < upper.intValue(); i++) {
				generatedParameters = testGen.generateParameters();
				assertEquals(2, generatedParameters.length);
				RValue[] expected = {semantics.Int(BigInteger.valueOf(i))};
				assertEquals(boolCombo, generatedParameters[0]);
				assertEquals(semantics.Array(expected), generatedParameters[1]);
			}
			// 2 elements
			for(int i=lower.intValue(); i < upper.intValue(); i++) {
				for(int j=lower.intValue(); j < upper.intValue(); j++) {
					generatedParameters = testGen.generateParameters();
					assertEquals(2, generatedParameters.length);
					RValue[] expected = {semantics.Int(BigInteger.valueOf(i)), semantics.Int(BigInteger.valueOf(j))};
					assertEquals(boolCombo, generatedParameters[0]);
					assertEquals(semantics.Array(expected), generatedParameters[1]);
				}
		
			}
			// 3 elements
			for(int i=lower.intValue(); i < upper.intValue(); i++) {
				for(int j=lower.intValue(); j < upper.intValue(); j++) {
					for(int k=lower.intValue(); k < upper.intValue(); k++) {
						generatedParameters = testGen.generateParameters();
						assertEquals(2, generatedParameters.length);
						RValue[] expected = {semantics.Int(BigInteger.valueOf(i)), semantics.Int(BigInteger.valueOf(j)), semantics.Int(BigInteger.valueOf(k))};
						assertEquals(boolCombo, generatedParameters[0]);
						assertEquals(semantics.Array(expected), generatedParameters[1]);
					}
				}
			}
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
		
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(2);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 10, lower, upper);
		for(int i=lower.intValue(); i < upper.intValue(); i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
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
		
		BigInteger lower = BigInteger.valueOf(1);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 10, lower, upper);
		for(int i=lower.intValue(); i < upper.intValue(); i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
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
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 90, lower, upper);
		for(int i=1; i < 3; i++) {
			for(int j=0; j <= 1; j++) {
				for(int k=1; k < boolCombinations.length; k++) {
					RValue[] generatedParameters = testGen.generateParameters();
					assertEquals(3, generatedParameters.length);
					assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
					assertEquals(semantics.Bool(j==0), generatedParameters[1]);
					assertEquals(semantics.Array(boolCombinations[k]), generatedParameters[2]);
				}				
			}
		}
	}
	
	/**
	 * Test creating a nominal based on another nominal
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
			GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 16, lower, upper);
			testGen.generateParameters();
			fail("Should not be able to generate parameters that are invalid");
		}
		catch(Error e) {}
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 16, lower, upper);

		lower = BigInteger.valueOf(0);
		upper = BigInteger.valueOf(20);
		testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 16, lower, upper);
		for(int i=1; i < 2; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i*10)), generatedParameters[0]);
		}
	}
	
	/**
	 * Test a record with different field types,
	 * two integer fields.
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
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(6);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 25, lower, upper);
		for(int i=0; i < 6; i++) {
			for(int j=0; j < 6; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(1, generatedParameters.length);
				RValue.Record record = (Record) generatedParameters[0];
				RValue first = record.read(new Identifier("x"));
				assertEquals(semantics.Int(BigInteger.valueOf(i)), first);
				RValue second = record.read(new Identifier("y"));
				assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
			}
		}

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
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(6);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 10, lower, upper);
		for(int i=0; i < 2; i++) {
			for(int j=0; j < 6; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(1, generatedParameters.length);
				RValue.Record record = (Record) generatedParameters[0];
				RValue first = record.read(new Identifier("negate"));
				assertEquals(semantics.Bool(i == 0), first);
				RValue second = record.read(new Identifier("x"));
				assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
			}
		}

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
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 10, lower, upper);
		for(int i=0; i < 3; i++) {
			for(int j=0; j < 3; j++) {
				for(int k=0; k <= 1; k++) {
					for(int m=0; m < 3; m++) {
						RValue[] generatedParameters = testGen.generateParameters();

						assertEquals(2, generatedParameters.length);
						RValue.Record recordPoint = (Record) generatedParameters[0];
						RValue first = recordPoint.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(i)), first);
						RValue second = recordPoint.read(new Identifier("y"));
						assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
						
						RValue.Record recordCounter = (Record) generatedParameters[1];
						RValue third = recordCounter.read(new Identifier("negate"));
						assertEquals(semantics.Bool(k==0), third);
						RValue fourth = recordCounter.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(m)), fourth);
					}
				}
			}
		}
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
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 10, lower, upper);
		for(int i=0; i < 3; i++) {
			for(int j=0; j < 3; j++) {
				for(int k=0; k < 3; k++) {
					for(int m=0; m < 3; m++) {
						RValue[] generatedParameters = testGen.generateParameters();

						assertEquals(2, generatedParameters.length);
						RValue.Record recordPoint = (Record) generatedParameters[0];
						RValue first = recordPoint.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(i)), first);
						RValue second = recordPoint.read(new Identifier("y"));
						assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
						
						RValue.Record recordCell = (Record) generatedParameters[1];
						RValue third = recordCell.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(k)), third);
						RValue fourth = recordCell.read(new Identifier("y"));
						assertEquals(semantics.Int(BigInteger.valueOf(m)), fourth);
					}
				}
			}
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 10, lower, upper);
		for(int i=0; i < 10; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Null(), generatedParameters[0]);
		}
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 15, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Null(), generatedParameters[0]);
		for(int i=-5; i < 5; i++) {
			generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 15, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(true), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(false), generatedParameters[0]);
		
		for(int i=-4; i < 5; i++) {
			generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
	}
	
	/**
	 * Test multiple unions,
	 * one union has boolean and integer type,
	 * the other union has a null and boolean array type.
	 */
	@Test
	public void testMultipleUnion() {
		Decl.Variable unionParam = new Decl.Variable(null, new Identifier("unionParamComplex"), new Type.Union(Type.Null, new Type.Array(Type.Bool)));
		Decl.Variable unionParam2 = new Decl.Variable(null, new Identifier("unionParam"), new Type.Union(Type.Bool, Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(unionParam, unionParam2);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 15, lower, upper);
		
		for(int i=-1; i < boolCombinations.length; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(2, generatedParameters.length);
			if(i == -1) {
				assertEquals(semantics.Null(), generatedParameters[0]);
			}
			else {
				assertEquals(semantics.Array(boolCombinations[i]), generatedParameters[0]);
			}
			
			generatedParameters = testGen.generateParameters();
			assertEquals(2, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[1]);
			
			generatedParameters = testGen.generateParameters();
			assertEquals(2, generatedParameters.length);
			assertEquals(semantics.Bool(false), generatedParameters[1]);
			
			for(int j=-4; j < 5; j++) {
				generatedParameters = testGen.generateParameters();
				assertEquals(2, generatedParameters.length);
				assertEquals(semantics.Int(BigInteger.valueOf(j)), generatedParameters[1]);
			}
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 15, lower, upper);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(true), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Bool(false), generatedParameters[0]);
		
		for(int i=-4; i < 5; i++) {
			generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, baseInterpreter, 256, lower, upper);
		for(int i=0; i < 256; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Byte((byte) i), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(semantics.Byte((byte) 0), generatedParameters[0]);
	}
	
	/**
	 * Test a recursive type
	 * @throws IOException 
	 */
	@Test
	public void testRecursiveType() throws IOException {
		String testName = "recursive_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(2);	
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 40, lower, upper);
		
		RValue.Field[] fields = new RValue.Field[2];
		fields[0] = semantics.Field(new Identifier("data"), semantics.Int(BigInteger.valueOf(0)));
		fields[1] = semantics.Field(new Identifier("n"), semantics.Null());
		RValue.Record expectedRecordOne = semantics.Record(fields);
		fields = new RValue.Field[2];
		fields[0] = semantics.Field(new Identifier("data"), semantics.Int(BigInteger.valueOf(1)));
		fields[1] = semantics.Field(new Identifier("n"), semantics.Null());
		RValue.Record expectedRecordTwo = semantics.Record(fields);

		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(expectedRecordOne, generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertEquals(expectedRecordTwo, generatedParameters[0]);
		
		// 2 elements
		for(int i=0; i < 2; i++) {
			RValue.Record expectedFirst = i == 0 ? expectedRecordOne : expectedRecordTwo;
			for(int j=0; j < 2; j++) {
				generatedParameters = testGen.generateParameters();
				assertEquals(1, generatedParameters.length);
				RValue.Record record = (Record) generatedParameters[0];
				RValue first = record.read(new Identifier("n"));
				assertEquals(expectedFirst, first);
				RValue second = record.read(new Identifier("data"));
				assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
			}
		}
		// 3 elements
		for(int i=0; i < 2; i++) {
			RValue.Record expectedFirst = i == 0 ? expectedRecordOne : expectedRecordTwo;
			for(int j=0; j < 2; j++) {
				for(int k=0; k < 2; k++) {
					generatedParameters = testGen.generateParameters();
					assertEquals(1, generatedParameters.length);
					RValue.Record record = (Record) generatedParameters[0];
					RValue first = record.read(new Identifier("n"));

					RValue.Record innerRecord = (Record) first;
					RValue innerFirst = innerRecord.read(new Identifier("n"));
					assertEquals(expectedFirst, innerFirst);
					RValue innerSecond = innerRecord.read(new Identifier("data"));
					assertEquals(semantics.Int(BigInteger.valueOf(j)), innerSecond);
					
					RValue second = record.read(new Identifier("data"));
					assertEquals(semantics.Int(BigInteger.valueOf(k)), second);
				}
			}
		}
	}
	
}
