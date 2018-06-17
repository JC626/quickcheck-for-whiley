package test.quickcheck;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import quickcheck.QCInterpreter;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Type;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter.CallStack;

/**
 * Test the QCInterpreter
 * 
 * @author Janice Chin
 *
 */
public class QCInterpreterTest {
	
	/**
	 * This directory contains the source files for each test case. Every test
	 * corresponds to a file in this directory.
	 */
	private final static String TEST_DIR = "tests";
	
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	private final static TestHelper helper = new TestHelper(TEST_DIR);
	/**
	 * Base interpreter used for the tests that do not require reading from a test file
	 */
	private static Interpreter baseInterpreter;
	
	@BeforeClass
	public static void setupClass() throws IOException {
		Build.Project project = helper.createProject();
		baseInterpreter = new QCInterpreter(project, System.out);
	}	

	/**
	 * Test calling a function within another function
	 * @throws IOException 
	 */
	@Test
	public void testFunctionOptimisation1() throws IOException {
		String testName = "function_op1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(10);
		Decl.Function func = functions.get(0);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		for(int i=0; i < 11; i++) {
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
			int ans = i*i + 1;
			if(i == 10) {
				assertEquals(semantics.Int(BigInteger.valueOf(1)), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(ans)), returns[0]);
			}
		}		
	}
	
	
	/**
	 * Test calling a function within another function
	 * @throws IOException 
	 */
	@Test
	public void testFunctionOptimisation2() throws IOException {
		String testName = "function_op2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		Interpreter interpreter = new QCInterpreter(project, System.out);
		List<Decl.Function> functions = helper.getFunctions(testName, project);
		
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(10);
		Decl.Function func = functions.get(0);
		GenerateTest testGen = new ExhaustiveGenerateTest(func, interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		for(int i=0; i < 11; i++) {
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, paramValues);
			int ans = i*2;
			if(i == 10) {
				assertEquals(semantics.Int(BigInteger.valueOf(0)), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(ans)), returns[0]);
			}
		}		
	}
	
	// TODO recursive call
	// TODO nominal
}
