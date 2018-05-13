package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import quickcheck.constraints.IntegerRange;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import quickcheck.generator.type.NominalGenerator;
import quickcheck.generator.type.RecordGenerator;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.util.AbstractCompilationUnit.Identifier;
import wyc.lang.WhileyFile.Decl;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Record;

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
	 * Get the integer range for a generator using reflection
	 * @param testGen
	 * @return List of integer ranges 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 */
	@SuppressWarnings("unchecked")
	public List<IntegerRange>  getIntegerRange(GenerateTest testGen) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
		// Get the integer range using reflection
		Field exhGenField = testGen.getClass().getDeclaredField("parameterGenerators");
		exhGenField.setAccessible(true);
		List<Generator> genExhaustive = (List<Generator>) exhGenField.get(testGen);
		assertTrue(genExhaustive.get(0) instanceof NominalGenerator);
		NominalGenerator nomGen = (NominalGenerator) genExhaustive.get(0);
		
		Field genField = nomGen.getClass().getDeclaredField("generator");
		genField.setAccessible(true);
		
		Field rangeField = IntegerGenerator.class.getDeclaredField("range");
		rangeField.setAccessible(true);
		
		List<IntegerRange> ranges = new ArrayList<IntegerRange>();
		if(genField.get(nomGen) instanceof IntegerGenerator) {
			IntegerGenerator gen = (IntegerGenerator) genField.get(nomGen);
			// Check the integer range
			ranges.add((IntegerRange) rangeField.get(gen));
			return ranges;
		}
		else if(genField.get(nomGen) instanceof RecordGenerator) {
			RecordGenerator generator = (RecordGenerator) genField.get(nomGen);
			Method m = generator.getClass().getDeclaredMethod("getIntegerGenerators");
			m.setAccessible(true);
			List<IntegerGenerator> generators = (List<IntegerGenerator>) m.invoke(generator);
			
			for(IntegerGenerator intGen : generators) {
				ranges.add((IntegerRange) rangeField.get(intGen));
			}
			return ranges;
		}
		fail("No integer range could be generated for " + genField.get(nomGen));
		return null;
	}
	
	
	/**
	 * Test when a nominal type wraps an integer,
	 * and it has a constraint with an && (and) in it
	 * @throws IOException
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeAnd() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_and";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeOr() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_or";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
	
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeNot() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_not";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeSingleMulti() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_multi_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeMulti() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_multi_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test(expected = Error.class)
	public void testNominalIntRangeInvalid() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_invalid";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
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
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalIntRangeEquals() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_int_equals";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);
		
		IntegerRange range = getIntegerRange(testGen).get(0);
		assertEquals(BigInteger.valueOf(10), range.lowerBound());
		assertEquals(BigInteger.valueOf(11), range.upperBound());

		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(10)), generatedParameters[0]);
		
		generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(10)), generatedParameters[0]);
	}
	
	/**
	 * Test when the nominal type wraps a record 
	 * which has an integer field.
	 * The record does not have a name.
	 * It has two fields that are both integers.
	 * @throws IOException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testNominalRecordNoName() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String testName = "record_invariant_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);
		
		
		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(1), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(0).upperBound());
		
		assertEquals(BigInteger.valueOf(-5), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(0), ranges.get(1).upperBound());
		
		for(int i=1; i < 10; i++) {
			for(int j=-5; j < 0; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				
				RValue.Record recordPoint = (Record) generatedParameters[0];
				RValue first = recordPoint.read(new Identifier("x"));
				assertEquals(semantics.Int(BigInteger.valueOf(i)), first);
				RValue second = recordPoint.read(new Identifier("y"));
				assertEquals(semantics.Int(BigInteger.valueOf(j)), second);
			}
		}
	}
	
	/**
	 * Test when the nominal type wraps a record 
	 * which has an integer field.
	 * The record has a name.
	 * The record also has 2 different fields, integer and boolean.
	 */
	@Test
	public void testNominalRecordName() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String testName = "record_invariant_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);
		
		
		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(1, ranges.size());
		assertEquals(BigInteger.valueOf(1), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(0).upperBound());
		
		for(int i=1; i < 10; i++) {
			for(int j=0; j < 2; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				
				RValue.Record recordPoint = (Record) generatedParameters[0];
				RValue first = recordPoint.read(new Identifier("x"));
				assertEquals(semantics.Int(BigInteger.valueOf(i)), first);
				RValue second = recordPoint.read(new Identifier("isCell"));
				assertEquals(semantics.Bool(j==0), second);
			}
		}
	}
	
}
