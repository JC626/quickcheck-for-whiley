package test.quickcheck;

import static org.junit.Assert.*;

import org.junit.Test;

import quickcheck.generator.GenerateTest;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyc.lang.WhileyFile.Decl.Function;
import wyil.interpreter.ConcreteSemantics.RValue;

/**
 * Test the GenerateTest class
 * @author Janice
 *
 */
public class GenerateTestTest {

	/**
	 * Test when the function has no parameters
	 */
	@Test
	public void testFunctionNoParameters() {
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>();
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		GenerateTest testGen = new GenerateTest(func);
		assertArrayEquals(new RValue[0], testGen.generateParameters());
	}
	
	/**
	 * Test when the function has 1 Int parameter
	 */
	@Test
	public void testFunctionIntParameter() {
		Decl.Variable intParam = new Decl.Variable(null, new Identifier("firstInt"), Type.Int);
		Tuple<Decl.Variable> parameters = new Tuple<Decl.Variable>(intParam);
		Function func = new Function(null, new Identifier("testF"), parameters, null, null, null, null);
		GenerateTest testGen = new GenerateTest(func);
		RValue[] generatedParameters = testGen.generateParameters();
		assertEquals(1, generatedParameters.length);
		assertTrue(generatedParameters[0] instanceof RValue.Int);
	}
}
