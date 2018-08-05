package test.quickcheck;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import quickcheck.QCInterpreter;
import quickcheck.exception.IntegerRangeException;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import test.utils.TestHelper;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
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
	 * Test calling a function within another function
	 * @throws IOException 
	 * @throws IntegerRangeException 
	 */
	@Test
	public void testFunctionOptimisation1() throws IOException, IntegerRangeException {
		String testName = "function_op1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(10);
		int numGen = 5;
		QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, true, true, numGen);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);

		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new ExhaustiveGenerateTest(func.getParameters(), interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		for(int i=0; i < 11; i++) {
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, true, true, paramValues);
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
	 * @throws IntegerRangeException 
	 */
	@Test
	public void testFunctionOptimisation2() throws IOException, IntegerRangeException {
		String testName = "function_op2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger upper = BigInteger.valueOf(10);
		int numGen = 5;
		QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, true, true, numGen);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);
		
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new ExhaustiveGenerateTest(func.getParameters(), interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		for(int i=0; i < 11; i++) {
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, true, true, paramValues);
			int ans = i*2;
			if(i == 10) {
				assertEquals(semantics.Int(BigInteger.valueOf(0)), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(ans)), returns[0]);
			}
		}		
	}
	
	/**
	 * Test recursively calling a function 
	 * using a factorial
	 * @throws IOException 
	 * @throws IntegerRangeException 
	 */
	@Test
	public void testFunctionOptimisationRecursive1() throws IOException, IntegerRangeException {
		String testName = "function_op_recursive1";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		BigInteger lower = BigInteger.valueOf(1);
		BigInteger upper = BigInteger.valueOf(8);
		int numGen = 5;
		QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, true, true, numGen);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);
		
		Decl.FunctionOrMethod func = functions.get(0);
		Tuple<Expr> empty = new Tuple<Expr>();		
		func.setOperand(4, empty); // Remove precondition
		func.setOperand(5, empty); // Remove postcondition
		GenerateTest testGen = new ExhaustiveGenerateTest(func.getParameters(), interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		int[] answers = new int[] {1, 2, 6, 24, 120, 720, 5040};
		for(int i=1; i < 9; i++) {
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, true, true, paramValues);
			if(i == 8) {
				assertEquals(semantics.Int(BigInteger.valueOf(answers[0])), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(answers[i-1])), returns[0]);
			}
		}		
	}
	
	/**
	 * Test recursively calling a function 
	 * by getting the sum of the numbers.
	 * @throws IOException 
	 * @throws IntegerRangeException 
	 */
	@Test
	public void testFunctionOptimisationRecursive2() throws IOException, IntegerRangeException {
		String testName = "function_op_recursive2";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		BigInteger lower = BigInteger.valueOf(1);
		BigInteger upper = BigInteger.valueOf(8);
		int numGen = 5;
		QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, true, true, numGen);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);
		
		Decl.FunctionOrMethod func = functions.get(0);
		Tuple<Expr> empty = new Tuple<Expr>();		
		func.setOperand(4, empty); // Remove precondition
		func.setOperand(5, empty); // Remove postcondition
		GenerateTest testGen = new ExhaustiveGenerateTest(func.getParameters(), interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		int ans = 0;
		for(int i=1; i < 9; i++) {
			ans += i;
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, true, true, paramValues);
			if(i == 8) {
				assertEquals(semantics.Int(BigInteger.valueOf(1)), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(ans)), returns[0]);
			}
		}		
	}
	
	/**
	 * Test when two functions call each other recursively.
	 * I.e. function A calls another function, B
	 * and the B calls A, recursively.
	 * @throws IOException 
	 * @throws IntegerRangeException 
	 */
	@Test
	public void testFunctionOptimisationRecursiveMulti() throws IOException, IntegerRangeException {
		String testName = "function_op_recursive_multi";
		helper.compile(testName);
		Build.Project project = helper.createProject();
		BigInteger lower = BigInteger.valueOf(1);
		BigInteger upper = BigInteger.valueOf(8);
		int numGen = 5;
		QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, true, true, numGen);
		List<Decl.FunctionOrMethod> functions = helper.getFunctionsAndMethods(testName, project);
	
		Decl.FunctionOrMethod func = functions.get(0);
		GenerateTest testGen = new ExhaustiveGenerateTest(func.getParameters(), interpreter, 25, lower, upper);
		
		Tuple<Decl.Variable> inputParameters = func.getParameters();
		Path.ID id = Trie.fromString(testName);
		NameID funcName = new NameID(id, func.getName().get());
		Type.Callable type = functions.get(0).getType();

		Identifier paramName = inputParameters.get(0).getName();
		int ans = 0;
		for(int i=1; i < 9; i++) {
			ans += i;
			CallStack frame = interpreter.new CallStack();
			RValue[] paramValues = testGen.generateParameters();
			frame.putLocal(paramName, paramValues[0]);

			RValue[] returns = interpreter.execute(funcName, type, frame, true, true, paramValues);
			if(i == 8) {
				assertEquals(semantics.Int(BigInteger.valueOf(1)), returns[0]);
			}
			else {
				assertEquals(semantics.Int(BigInteger.valueOf(ans)), returns[0]);
			}
		}		
	}
}
