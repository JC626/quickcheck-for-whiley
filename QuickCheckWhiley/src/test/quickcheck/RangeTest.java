package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import quickcheck.constraints.IntegerRange;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import quickcheck.generator.type.NominalGenerator;
import test.utils.TestHelper;
import wybs.lang.Build;
import wyc.lang.WhileyFile.Decl;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Test range constraints are applied
 * for nominal types
 * 
 * @author Janice Chin
 *
 */
public class RangeTest {
	/**
	 * This directory contains the source files for each test case. Every test
	 * corresponds to a file in this directory.
	 */
	private final static String TEST_DIR = "tests";
	private static final ConcreteSemantics semantics = new ConcreteSemantics();

	private final static TestHelper helper = new TestHelper(TEST_DIR);
	
	/**
	 * Get the integer range for an integer generator using reflection
	 * @param testGen
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public IntegerRange getIntegerRange(GenerateTest testGen) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		// Get the integer range using reflection
		Field exhGenField = testGen.getClass().getDeclaredField("parameterGenerators");
		exhGenField.setAccessible(true);
		List<Generator> genExhaustive = (List<Generator>) exhGenField.get(testGen);
		assertTrue(genExhaustive.get(0) instanceof NominalGenerator);
		NominalGenerator nomGen = (NominalGenerator) genExhaustive.get(0);
		
		Field genField = nomGen.getClass().getDeclaredField("generator");
		genField.setAccessible(true);
		assertTrue(genField.get(nomGen) instanceof IntegerGenerator);
		IntegerGenerator gen = (IntegerGenerator) genField.get(nomGen);		
		
		// Check the integer range
		Field rangeField = gen.getClass().getDeclaredField("range");
		rangeField.setAccessible(true);
		return (IntegerRange) rangeField.get(gen);
	}
	
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and it has a constraint with an && (and) in it
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeAnd() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_and";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(BigInteger.valueOf(1), range.lowerBound());
		assertEquals(BigInteger.valueOf(10), range.upperBound());
		
		for(int i=1; i < 10; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(1)), generatedParameters[0]);
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and it has a constraint with an || (or) in it
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeOr() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_or";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
	
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(lower, range.lowerBound());
		assertEquals(upper, range.upperBound());
		
		for(int i=-5; i < 0; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		
		for(int i=11; i < 15; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[0]);
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and it has a constraint with an ! (not) in it
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeNot() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_not";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(lower, range.lowerBound());
		assertEquals(BigInteger.valueOf(1), range.upperBound());

		for(int i=-5; i < 1; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[0]);
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and has one where clause with multiple constraints
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeSingleMulti() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_multi_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(BigInteger.valueOf(-5), range.lowerBound());
		assertEquals(BigInteger.valueOf(5), range.upperBound());

		for(int i=-5; i < 5; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(-5)), generatedParameters[0]);
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and has multiple where clauses
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeMulti() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_multi_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(BigInteger.valueOf(1), range.lowerBound());
		assertEquals(BigInteger.valueOf(5), range.upperBound());

		for(int i=1; i < 5; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(1)), generatedParameters[0]);
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * but cannot generate any integers as the upper range is 
	 * smaller or the same as the lower range
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test(expected = Error.class)
	public void testNominalIntRangeInvalid() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_invalid";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(BigInteger.valueOf(1), range.lowerBound());
		assertEquals(BigInteger.valueOf(1), range.upperBound());
	}
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and has a equals clause
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testNominalIntRangeEquals() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_int_equals";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen);
		assertEquals(BigInteger.valueOf(10), range.lowerBound());
		assertEquals(BigInteger.valueOf(11), range.upperBound());

		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(10)), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(10)), generatedParameters[0]);
	}
	
}
