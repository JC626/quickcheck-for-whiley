package quickcheck;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import quickcheck.exception.IntegerRangeException;
import quickcheck.generator.ExhaustiveGenerateTest;
import quickcheck.generator.GenerateTest;
import quickcheck.generator.RandomGenerateTest;
import quickcheck.util.TestType;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.lang.NameResolver.ResolutionError;
import wybs.util.StdProject;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.util.AbstractProjectCommand;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.DirectoryRoot;
import wyfs.util.JarFileRoot;
import wyfs.util.Trie;
import wyil.interpreter.Interpreter;
import wyil.interpreter.Interpreter.CallStack;
import wyil.interpreter.ConcreteSemantics.RValue;

import static wyc.lang.WhileyFile.*;

/**
 * Responsible for implementing the command "<code>java QuickCheck ...</code>" which
 * loads the appropriate <code>wyil</code> file and executes tests 
 * for the functions/methods using the <code>Interpreter</code>.
 * 	
 * Based on wyc.Command.Run
 *
 * @author Janice Chin
 *
 */
public class RunTest extends AbstractProjectCommand<RunTest.Result> {
	public static final int MAX_NUMBER_ARGUMENTS = 8;
	
	public static final int NUM_TESTS = 10;
	public static final int INT_LOWER_LIMIT = -10;
	public static final int INT_UPPER_LIMIT = 10;
	public static final int ARRAY_LOWER_LIMIT = 0;
	public static final int ARRAY_UPPER_LIMIT = 3;
	public static final int RECURSIVE_LIMIT = 3;
	
	/**
	 * Result kind for this command
	 *
	 */
	public enum Result {
		PASSED, // All tests passed
		FAILED, // Some tests failed
		SKIPPED, // All tests were skipped 
		ERRORS, // An error occurred before execution
		INTERNAL_FAILURE // An error occurred during the program
	}

	public RunTest(Content.Registry registry, Logger logger) {
		super(registry, logger);
	}

	// =======================================================================
	// Configuration
	// =======================================================================

	@Override
	public String getDescription() {
		return "Execute a given function from a WyIL";
	}

	@Override
	public String getName() {
		return "run";
	}

	// =======================================================================
	// Execute
	// =======================================================================

	@Override
	public Result execute(String... args) {
		if (args.length == 0) {
			System.out.println("usage: run <wyilfile> <method>");
			return Result.ERRORS;
		}
		try {
			// Get the Whiley standard library
			String whileystd = System.getenv("WHILEYSTD");
			if(whileystd == null) {
				System.out.println("error: WHILEYSTD environment variable not set.");
				System.out.println("This should be set to the wystd jar file or the directory of the library");
				System.out.println("Tests including the standard library may not execute.");
			}
			else {
				whileystd = whileystd.replace('\\', File.separatorChar);
				whileystd = whileystd.replace('/', File.separatorChar);
			}
			Build.Project project = createWhileyProject(whileystd, args[0]);
			Path.ID id = Trie.fromString(args[1]);
			TestType testType = TestType.valueOf(args[2]);
			List<Decl.Function> functions = getFunctions(id, project);
			BigInteger lower = new BigInteger(args[4]);
			BigInteger upper = new BigInteger(args[5]);
			// Function optimisation parameters
			boolean funcOpt = args[6].equals(Boolean.toString(true));
			int numFuncOpGen = Integer.parseInt(args[7]);
			// Generate tests for each function
			QCInterpreter interpreter = new QCInterpreter(project, System.out, lower, upper, funcOpt, numFuncOpGen);
			int numTests = RunTest.NUM_TESTS;
			try {
				numTests = Integer.parseInt(args[3]);
			}
			catch(NumberFormatException e) {}
			int numSkipped = 0;
			Result result = Result.PASSED;
			for(Decl.Function func : functions) {
				Result r = executeTest(id, interpreter, func, testType, numTests, lower, upper);
				if(r == Result.FAILED) {
					result = r;
				}
				else if(r == Result.ERRORS) {
					return r;
				}
				else if(r == Result.SKIPPED) {
					numSkipped++;
				}
			}
			if(result == Result.SKIPPED) {
				// Some of the tests were successful
				if(numSkipped != functions.size()) {
					return Result.PASSED;
				}
			}
			return result;
		} catch (IOException e) {
			// FIXME: need a better error reporting mechanism
			System.err.println("internal failure: " + e.getMessage());
			e.printStackTrace();
			return Result.INTERNAL_FAILURE;
		}
	}

	// =======================================================================
	// Helpers
	// =======================================================================
	
	/**
	 * Create a default Whiley project in the given directory. This can then be used
	 * to read WyIL files from that directory.
	 *
	 * @param dir - The path directory to look for the Whiley/Wyil file.
	 * @return Whiley Project
	 */
	private static Build.Project createWhileyProject(String standardLib, String dir) throws IOException {
		// The content registry maps file name extensions to their Content.Type.
		Content.Registry registry = new wyc.Activator.Registry();
		// The directory root specified where to look for Whiley / WyIL files.
		DirectoryRoot root = new DirectoryRoot(dir,registry);
		ArrayList<Path.Root> roots = new ArrayList<>();
		roots.add(root);
		// Add standard library location
		if(standardLib != null) {
			if(standardLib.endsWith("jar")) {
				roots.add(new JarFileRoot(standardLib, registry));
			}
			else {
				roots.add(new DirectoryRoot(standardLib, registry));
			}
		}
		// Finally, create the project itself		
		return new StdProject(roots);
	}

	
	/**
	 * Test a function from a Wyil file
	 * by executing the test with randomised parameters
	 * @param id The module used
	 * @param interpreter Whiley interpreter used to execute the function/method
	 * @param dec The function or method
	 * @param testType The type of tests to generate
	 * @param numTest The number of tests to execute
	 * @param lowerLimit The lower constraint used when generating integers
	 * @param upperLimit The upper constraint used when generating integers
	 */
	private Result executeTest(Path.ID id, QCInterpreter interpreter, Decl.FunctionOrMethod dec, TestType testType, int numTest, BigInteger lowerLimit, BigInteger upperLimit) {
		// Get the method for generating test values
		GenerateTest testGen;
		try {
			if(testType == TestType.EXHAUSTIVE) {
				testGen = new ExhaustiveGenerateTest(dec.getParameters(), interpreter, numTest, lowerLimit, upperLimit);
			}
			else {
	            testGen = new RandomGenerateTest(dec.getParameters(), interpreter, numTest, lowerLimit, upperLimit);
			}
		} catch (IntegerRangeException e) {
			System.out.println("Integer range was invalid for the limits given.");
			return Result.ERRORS;
		}
		// Get the function's relevant header information
		NameID name = new NameID(id, dec.getName().get());
		Type.Callable type = dec.getType();
		Tuple<Expr> preconditions = dec.getRequires();
		Tuple<Expr> postconditions = dec.getEnsures();
		Tuple<Decl.Variable> inputParameters = dec.getParameters();
		Tuple<Decl.Variable> outputParameters = dec.getReturns();
		
		System.out.println("FUNCTION "+ name);
		System.out.println("FUNCTION PARAM TYPES "+ inputParameters);
		System.out.println("PRECONDITION "+ preconditions);
		System.out.println("POSTCONDITION "+ postconditions);
				
//		// Have to remove the pre and post conditions out of the 
//		// function so the function is executed without validation
//		// Validation will be conducted manually inside the function.
//		Tuple<Expr> empty = new Tuple<Expr>();		
//		dec.setOperand(4, empty); // Remove precondition
//		dec.setOperand(5, empty); // Remove postcondition
		
		int numSkipped = 0;
		int numPassed = 0;
		for(int i=0; i < numTest; i++) {
			RValue[] paramValues = testGen.generateParameters();
			CallStack frame = interpreter.new CallStack();
			// Check the precondition
			try {
				for(int j=0; j < inputParameters.size(); j++) {
					Decl.Variable parameter = inputParameters.get(j);
					frame.putLocal(parameter.getName(), paramValues[j]);
				}
				interpreter.checkInvariants(frame, preconditions);
			}
			catch(AssertionError e){
				System.out.println("Pre-condition failed on input: " + Arrays.toString(paramValues));
				numSkipped++;
				continue;
			}
			
			System.out.println("INPUT: " + Arrays.toString(paramValues));
			// Checks the postcondition when it is executed
			RValue[] returns = null;
			try {
				returns = interpreter.execute(name, type, frame, false, false, i == 0, paramValues);
			}
			catch(AssertionError e) {
				System.out.println("Error occurred during execution " + e + ": " + e.getMessage());
				continue;
			} 
			try {
				// Add the return values into the frame for validation
				for(int j=0; j < outputParameters.size(); j++) {
					Decl.Variable parameter = outputParameters.get(j);
					Type paramType = parameter.getType();
					boolean valid = checkInvariant(interpreter, paramType, returns[j]);
					if(!valid) {
						System.out.println("Post condition for " + parameter  + " failed");
						throw new AssertionError("");
					}
					frame.putLocal(parameter.getName(), returns[j]);
				}				
				interpreter.checkInvariants(frame, postconditions);
				numPassed++;
//				// Print out any return values produced
//				if (returns != null) {
//					System.out.println("OUTPUT: " + Arrays.toString(returns));
//				}
			}
			catch(AssertionError e) {
				System.out.printf("Failed Input: %s Output: %s%n", Arrays.toString(paramValues), Arrays.toString(returns));
				System.out.println("Due to error " + e + ": " + e.getMessage());
			} 
			catch (ResolutionError e) {
				// FIXME resolution error
				e.printStackTrace();
				assert false;
			} 
		}
		// Overall test statistics
		if(numPassed + numSkipped == numTest) {
			System.out.printf("Ok: %d passed  (%.2f %%), %d skipped (%.2f %%), ran %d tests %n",
					numPassed, (double) 100 * numPassed/numTest, numSkipped, (double) 100 * numSkipped/numTest, numTest);
			return Result.PASSED;
		}
		else if(numSkipped == numTest) {
			System.out.println("All tests skipped!");
			return Result.SKIPPED;
		}
		else {
			int numFailed = numTest - numPassed;
			System.out.printf("Failed: %d passed (%.2f %%), %d failed (%.2f %%), %d skipped (%.2f %%), ran %d tests%n",
					numPassed, (double) 100 * numPassed/numTest, numFailed, (double) 100 * numFailed/numTest, numSkipped, (double) 100 * numSkipped/numTest, numTest);
			return Result.FAILED;
		}
	}
	
	/**
	 * Check the postcondition of all types,
	 * including invariants within unions and nominals.
	 * @param interpreter Whiley interpreter used to check the invariant on the function/method
	 * @param paramType The type of the output parameter
	 * @param returnVal The return value from the function/method
	 * @return If the invariant was valid or not
	 * @throws ResolutionError 
	 */
	public static boolean checkInvariant(Interpreter interpreter, Type paramType, RValue returnVal) throws ResolutionError {
		// Check the nominal type postcondition
		if(paramType instanceof Type.Nominal) {
			Type.Nominal nom = (Type.Nominal) paramType;
			Decl.Type decl = interpreter.getTypeSystem().resolveExactly(nom.getName(), Decl.Type.class);			
			if(decl.getInvariant().size() > 0) {
				RValue.Bool valid = returnVal.checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
				if(valid == RValue.Bool.False) {
					return false;
				}
			}
			// Need to go deeper as nominal wraps another type!
			return checkInvariant(interpreter, decl.getVariableDeclaration().getType(), returnVal);
		}
		else if(paramType instanceof Type.Union) {
			boolean isValid = false;
			Type.Union union = (Type.Union) paramType;
			// Need to check all values in the union to see if any of the types are valid
			for(Type t : union.getAll()) {
				if(returnVal.is(t, interpreter) == RValue.Bool.True) {
					boolean valid = checkInvariant(interpreter, t, returnVal);
					if(valid) {
						isValid = true;
						break;
					}
				}
			}
			if(!isValid) {
				return false;
			}
		} 
		else if(returnVal.is(paramType, interpreter) == RValue.Bool.True){
			if(paramType instanceof Type.Array) {
				Type.Array arr = (Type.Array) paramType;
				// Check if the return value adheres to the array's type and value
				Type elementType = arr.getElement();
				if(returnVal.is(elementType, interpreter) == RValue.Bool.True) {
					return checkInvariant(interpreter, elementType, returnVal);
				}
			}
		}
		else {
			return false;
		}
		return true;
	}
		
	/**
	 * Get all functions in the Wyil file 
	 * Based on part of wyil.interpreter.Interpreter execute function
	 * @param id 
	 * @param project
	 * @return A list of functions from the Wyil file
	 */
	private List<Decl.Function> getFunctions(Path.ID id, Build.Project project) {
		try {
			// NOTE: need to read WyilFile here as, otherwise, it forces a
			// rereading of the Whiley source file and a loss of all generation
			// information.
			Path.Entry<WhileyFile> entry = project.get(id, WhileyFile.BinaryContentType);
			if (entry == null) {
				throw new IllegalArgumentException("no WyIL file found: " + id);
			}
			WhileyFile wyilFile = entry.read();
			// Get declarations from the WhileyFile
			Tuple<Decl> declarations = wyilFile.getDeclarations();
			// Get the functions from the WhileyFile
			List<Decl.Function> functions = new ArrayList<Decl.Function>();
			for(Decl dec : declarations) {
				if(dec instanceof Decl.Function) {
					functions.add((Decl.Function) dec);
				}
			}
			return functions;
		}
		catch(IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
