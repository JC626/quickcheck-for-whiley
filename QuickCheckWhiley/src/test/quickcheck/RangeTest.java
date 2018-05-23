package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import quickcheck.constraints.IntegerRange;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import quickcheck.generator.type.ArrayGenerator;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import quickcheck.generator.type.NominalGenerator;
import quickcheck.generator.type.RecordGenerator;
import quickcheck.generator.type.UnionGenerator;
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
	public List<IntegerRange> getIntegerRange(GenerateTest testGen) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		// Get the integer range using reflection
		Field exhGenField = testGen.getClass().getDeclaredField("parameterGenerators");
		exhGenField.setAccessible(true);
		List<Generator> genExhaustive = (List<Generator>) exhGenField.get(testGen);
		List<IntegerRange> ranges = new ArrayList<IntegerRange>();

		for(int i=0; i < genExhaustive.size(); i++) {
			if(genExhaustive.get(i) instanceof NominalGenerator) {
				NominalGenerator nomGen = (NominalGenerator) genExhaustive.get(i);

				Field genField = nomGen.getClass().getDeclaredField("generator");
				genField.setAccessible(true);

				ranges = getIntegerRange((Generator) genField.get(nomGen), ranges);
			}
		}
		assertFalse(ranges.isEmpty());
		return ranges;
	}

	@SuppressWarnings("unchecked")
	public List<IntegerRange> getIntegerRange(Generator gen, List<IntegerRange> ranges) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(gen instanceof NominalGenerator) {
			NominalGenerator nomGen = (NominalGenerator) gen;

			Field genField = nomGen.getClass().getDeclaredField("generator");
			genField.setAccessible(true);
			return getIntegerRange((Generator) genField.get(nomGen), ranges);
		}
		else if(gen instanceof IntegerGenerator) {
			IntegerGenerator intGen = (IntegerGenerator) gen;
			// Check the integer range
			Field rangeField = gen.getClass().getDeclaredField("range");
			rangeField.setAccessible(true);
			ranges.add((IntegerRange) rangeField.get(intGen));
		}
		else if(gen instanceof ArrayGenerator) {
			ArrayGenerator arrayGen = (ArrayGenerator) gen;
			Field rangeField = arrayGen.getClass().getDeclaredField("range");
			rangeField.setAccessible(true);
			ranges.add((IntegerRange) rangeField.get(arrayGen));
		}
		else if(gen instanceof RecordGenerator) {
			RecordGenerator recordGen = (RecordGenerator) gen;
			Field recordGenField = recordGen.getClass().getDeclaredField("generators");
			recordGenField.setAccessible(true);
			List<Generator> generators = (List<Generator>) recordGenField.get(recordGen);
			for(Generator g : generators) {
				getIntegerRange(g, ranges);
			}
		}
		else if(gen instanceof UnionGenerator) {
			UnionGenerator unionGen = (UnionGenerator) gen;
			Field unionGenField = unionGen.getClass().getDeclaredField("generators");
			unionGenField.setAccessible(true);
			List<Generator> generators = (List<Generator>) unionGenField.get(unionGen);
			for(Generator g : generators) {
				getIntegerRange(g, ranges);
			}
		}
		return ranges;
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
	public void testNominalIntRangeAnd() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
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
	 */
	@Test
	public void testNominalIntRangeOr() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
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
	 */
	@Test
	public void testNominalRecordNoName() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
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
	public void testNominalRecordName() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
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

	/**
	 * Test when the nominal type wraps a array
	 * restricted by size.
	 *
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testNominalArraySize() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_array_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(1, ranges.size());
		assertEquals(BigInteger.valueOf(2), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(4), ranges.get(0).upperBound());

		RValue[] generatedParameters;
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
	 * Test when the nominal type wraps a record that contains
	 * an array restricted by size and a constrained integer.
	 *
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testNominalRecordArray() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String testName = "nominal_record_array";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(1), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(2), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(4), ranges.get(1).upperBound());

		for(int count=1; count < 5; count++) {
			RValue[] generatedParameters;
			// 2 elements
			for(int i=0; i < 2; i++) {
				for(int j=0; j < 2; j++) {
					generatedParameters = testGen.generateParameters();
					assertEquals(1, generatedParameters.length);
					RValue.Record recordPoint = (Record) generatedParameters[0];
					RValue first = recordPoint.read(new Identifier("arr"));
					RValue[] expected = {semantics.Bool(i==0), semantics.Bool(j==0)};
					assertEquals(semantics.Array(expected), first);
					RValue second = recordPoint.read(new Identifier("count"));
					assertEquals(semantics.Int(BigInteger.valueOf(count)), second);
				}

			}
			// 3 elements
			for(int i=0; i < 2; i++) {
				for(int j=0; j < 2; j++) {
					for(int k=0; k < 2; k++) {
						generatedParameters = testGen.generateParameters();
						assertEquals(1, generatedParameters.length);
						RValue.Record recordPoint = (Record) generatedParameters[0];
						RValue first = recordPoint.read(new Identifier("arr"));
						RValue[] expected = {semantics.Bool(i==0), semantics.Bool(j==0), semantics.Bool(k==0)};
						assertEquals(semantics.Array(expected), first);
						RValue second = recordPoint.read(new Identifier("count"));
						assertEquals(semantics.Int(BigInteger.valueOf(count)), second);
					}
				}
			}
		}
	}

	/**
	 * Test when a nominal type wraps another nominal type
	 * but does not have any constraints.
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testDoubleNominalNoConstraint() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_double_1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);

		IntegerRange range = getIntegerRange(testGen).get(0);
		assertEquals(BigInteger.valueOf(1), range.lowerBound());
		assertEquals(upper, range.upperBound());

		for(int i=1; i < 10; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(1)), generatedParameters[0]);
	}

	/**
	 * Test when a nominal type wraps another nominal type
	 * that also has an additional constraint.
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testDoubleNominalConstraint() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_double_2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(1), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(1), ranges.get(1).lowerBound());
		assertEquals(upper, ranges.get(1).upperBound());

		for(int i=1; i < 10; i++) {
			for(int j=1; j < 15; j++) {
				RValue[] generatedParameters = testGen.generateParameters();
				assertEquals(semantics.Int(BigInteger.valueOf(i)), generatedParameters[0]);
				assertEquals(semantics.Int(BigInteger.valueOf(j)), generatedParameters[1]);

			}
		}
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(semantics.Int(BigInteger.valueOf(1)), generatedParameters[0]);
		assertEquals(semantics.Int(BigInteger.valueOf(1)), generatedParameters[1]);
	}

	/**
	 * Test when a nominal type wraps another nominal type
	 * that has a constraint that makes it invalid
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test(expected = Error.class)
	public void testDoubleNominalInvalid() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_double_invalid";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);
	}

	/**
	 * Test when a record contains a nominal type with constraints.
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testRecordNominal() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_double_record";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(10);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 20, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(4, ranges.size());
		assertEquals(BigInteger.valueOf(1), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(1), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(8), ranges.get(1).upperBound());

		assertEquals(BigInteger.valueOf(1), ranges.get(2).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(2).upperBound());
		assertEquals(BigInteger.valueOf(1), ranges.get(3).lowerBound());
		assertEquals(BigInteger.valueOf(8), ranges.get(3).upperBound());

		for(int i=1; i < 5; i++) {
			for(int j=1; j < 8; j++) {
				for(int m=1; m < 5; m++) {
					for(int n=1; n < 8; n++) {
						RValue[] generatedParameters = testGen.generateParameters();
						assertEquals(2, generatedParameters.length);

						RValue.Record recordInput = (Record) generatedParameters[0];
						RValue inputFirst = recordInput.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(i)), inputFirst);
						RValue inputSecond = recordInput.read(new Identifier("y"));
						assertEquals(semantics.Int(BigInteger.valueOf(j)), inputSecond);

						RValue.Record recordBounds = (Record) generatedParameters[1];
						RValue boundsFirst = recordBounds.read(new Identifier("x"));
						assertEquals(semantics.Int(BigInteger.valueOf(m)), boundsFirst);
						RValue boundsSecond = recordBounds.read(new Identifier("y"));
						assertEquals(semantics.Int(BigInteger.valueOf(n)), boundsSecond);

					}
				}
			}
		}
	}

	/**
	 * Test when a record wraps another record.
	 * Both records have constraints applied to them
	 */
	@Test
	public void testDoubleRecord() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "record_double";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(0), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(0), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(1).upperBound());

		for(int i=0; i < 10; i++) {
			for(int j=0; j < 2; j++) {
				for(int m=0; m < 5; m++) {
					for(int n=0; n < 2; n++) {
						RValue[] generatedParameters = testGen.generateParameters();
						RValue.Record recordBoard = (Record) generatedParameters[0];
						RValue first = recordBoard.read(new Identifier("width"));
						assertTrue(first instanceof RValue.Record);
						RValue.Record widthRecord = (RValue.Record) first;
						RValue widthValue = widthRecord.read(new Identifier("value"));
						RValue widthPositive = widthRecord.read(new Identifier("positive"));
						assertEquals(semantics.Int(BigInteger.valueOf(i)), widthValue);
						assertEquals(semantics.Bool(j == 0), widthPositive);

						RValue second = recordBoard.read(new Identifier("height"));
						assertTrue(second instanceof RValue.Record);
						RValue.Record heightRecord = (RValue.Record) second;
						RValue heightValue = heightRecord.read(new Identifier("value"));
						RValue heightPositive = heightRecord.read(new Identifier("positive"));
						assertEquals(semantics.Int(BigInteger.valueOf(m)), heightValue);
						assertEquals(semantics.Bool(n == 0), heightPositive);
					}
				}
			}
		}
	}

	/**
	 * Test when a record wraps a union.
	 * The record has a constraint applied to it.
	 */
	@Test
	public void testRecordUnion() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "record_union";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(0), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(-5), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(1).upperBound());

		int i = 0;
		int j = -5;
		boolean isNat = true;
		while(i < 5 && j < 5) {
			for(int m = 0; m < 2; m++) {
				RValue[] generatedParameters = testGen.generateParameters();
				RValue.Record recordBoard = (Record) generatedParameters[0];
				RValue num = recordBoard.read(new Identifier("num"));
				RValue positive = recordBoard.read(new Identifier("positive"));
				if(isNat) {
					assertEquals(semantics.Int(BigInteger.valueOf(i)), num);
				}
				else {
					assertEquals(semantics.Int(BigInteger.valueOf(j)), num);
				}
				assertEquals(semantics.Bool(m==0), positive);
			}
			if(isNat) {
				i++;
			}
			else {
				j++;
			}
			isNat = !isNat;
		}
	}

	/**
	 * Test when a union has a constraint.
	 */
	@Test
	public void testUnion() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "union_constraint";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(0), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(-5), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(10), ranges.get(1).upperBound());
		int i = 0;
		int j = -5;
		boolean isNat = true;
		while(i < 10 && j < 10) {
			RValue[] generatedParameters = testGen.generateParameters();
			RValue value =  generatedParameters[0];
			if(isNat) {
				assertEquals(semantics.Int(BigInteger.valueOf(i)), value);
				i++;
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(j)), value);
				j++;
			}
			isNat = !isNat;
		}
	}

	/**
	 * Test when another nominal wraps a union type.
	 * The nominal applies an additional constraint on the
	 * union.
	 */
	@Test
	public void testNominalUnion() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "nominal_union";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(15);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(0), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(-5), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(5), ranges.get(1).upperBound());
		int i = 0;
		int j = -5;
		boolean isNat = true;
		while(i < 5 && j < 5) {
			RValue[] generatedParameters = testGen.generateParameters();
			RValue value =  generatedParameters[0];
			if(isNat) {
				assertEquals(semantics.Int(BigInteger.valueOf(i)), value);
				i++;
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(j)), value);
				j++;
			}
			isNat = !isNat;
		}
	}

	/**
	 * Test when a union wraps arrays and a
	 * constraint is applied to its size
	 */
	@Test
	public void testUnionArray() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String testName = "union_array";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new Interpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);

		BigInteger lower = BigInteger.valueOf(-5);
		BigInteger upper = BigInteger.valueOf(5);
		GenerateTest testGen = new ExhaustiveGenerateTest(functions.get(0), interpreter, 50, lower, upper);

		List<IntegerRange> ranges = getIntegerRange(testGen);
		assertEquals(2, ranges.size());
		assertEquals(BigInteger.valueOf(0), ranges.get(0).lowerBound());
		assertEquals(BigInteger.valueOf(2), ranges.get(0).upperBound());
		assertEquals(BigInteger.valueOf(0), ranges.get(1).lowerBound());
		assertEquals(BigInteger.valueOf(2), ranges.get(1).upperBound());

		// Check the empty array first
		for(int i=0; i < 2; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			RValue[] expected = new RValue[0];
			assertEquals(semantics.Array(expected), generatedParameters[0]);
		}

		// Go between the union types
		int i = -5;
		int j = 0;
		boolean isInt = true;
		while(i < 5 && j < 2) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			if(isInt) {
				RValue[] expected = new RValue[] {semantics.Int(BigInteger.valueOf(i))};
				assertEquals(semantics.Array(expected), generatedParameters[0]);
				i++;
				if(j >= 2) {
					continue;
				}
			}
			else {
				RValue[] expected = new RValue[] {semantics.Bool(j==0)};
				assertEquals(semantics.Array(expected), generatedParameters[0]);
				j++;
			}
			isInt = !isInt;
		}
	}
}
