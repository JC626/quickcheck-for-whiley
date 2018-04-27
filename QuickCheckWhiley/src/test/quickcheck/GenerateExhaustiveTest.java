package test.quickcheck;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import wybs.lang.Build;
import wybs.util.StdProject;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.DirectoryRoot;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.type.TypeSystem;

/**
 * Test the exhaustive test generation
 * for generating all combinations of possible input data.
 * 
 * @author Janice Chin
 *
 */
public class GenerateExhaustiveTest {
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	private static RValue[][] boolCombinations = {{}, {RValue.True}, {RValue.False}, 
			   										  {RValue.True, RValue.True}, {RValue.True, RValue.False},
			   										  {RValue.False, RValue.True}, {RValue.False, RValue.False}, 
			   										  {RValue.True, RValue.True, RValue.True}, {RValue.True, RValue.True, RValue.False},
			   										  {RValue.True, RValue.False, RValue.True}, {RValue.True, RValue.False, RValue.False},
			   										  {RValue.False, RValue.True, RValue.True}, {RValue.False, RValue.True, RValue.False},
			   										  {RValue.False, RValue.False, RValue.True}, {RValue.False, RValue.False, RValue.False}};

	private static TypeSystem typeSystem;
	
	@BeforeClass
	public static void setupClass() throws IOException {
		// The content registry maps file name extensions to their Content.Type.
		Content.Registry registry = new wyc.Activator.Registry();
		// The directory root specified where to look for Whiley / WyIL files.
		DirectoryRoot root = new DirectoryRoot("test", registry);
		ArrayList<Path.Root> roots = new ArrayList<>();
		roots.add(root);
		// Finally, create the project itself
		Build.Project project = new StdProject(roots);
		typeSystem = new TypeSystem(project);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 10, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 10, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 18, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 5, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 10, lower, upper);
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
		Decl.Variable boolParam = new Decl.Variable(null, new Identifier("secBool"), Type.Bool);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam, boolParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(-2);
		BigInteger upper = BigInteger.valueOf(4);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 10, lower, upper);
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
		testGen = new ExhaustiveGenerateTest(func, typeSystem, 10, lower, upper);
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
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 90, lower, upper);
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

	@Test
	public void testFunctionArraySingleBool() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 15, lower, upper);
		for(int i=0; i < boolCombinations.length; i++) {
			RValue[] generatedParameters = testGen.generateParameters();
			assertEquals(1, generatedParameters.length);
			assertEquals(semantics.Array(boolCombinations[i]), generatedParameters[0]);
		}
	}
	
	@Test
	public void testFunctionArraySingleInt() {
		Decl.Variable arrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(arrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 40, lower, upper);
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
	
	@Test
	public void testMultiArray() {
		Decl.Variable boolArrayParam = new Decl.Variable(null, new Identifier("boolArr"), new Type.Array(Type.Bool));
		Decl.Variable intArrayParam = new Decl.Variable(null, new Identifier("intArr"), new Type.Array(Type.Int));
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(boolArrayParam, intArrayParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(3);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, typeSystem, 600, lower, upper);
		
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
	
}
