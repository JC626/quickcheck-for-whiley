package quickcheck;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	public static final int NUM_TESTS = 10;
	public static final int INT_LOWER_LIMIT = -10;
	public static final int INT_UPPER_LIMIT = 10;
	public static final int ARRAY_LOWER_LIMIT = 0;
	public static final int ARRAY_UPPER_LIMIT = 3;
	
	/**
	 * Result kind for this command
	 *
	 */
	public enum Result {
		SUCCESS,
		ERRORS,
		INTERNAL_FAILURE
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
			Build.Project project = createWhileyProject(args[0]);
			Path.ID id = Trie.fromString(args[1]);
			TestType testType = TestType.valueOf(args[2]);
			List<Decl.Function> functions = getFunctions(id, project);
			// Generate tests for each function
			Interpreter interpreter = new Interpreter(project, System.out);
			int numTests = RunTest.NUM_TESTS;
			try {
				numTests = Integer.parseInt(args[3]);
			}
			catch(NumberFormatException e) {}
			for(Decl.Function func : functions) {
				executeTest(id, interpreter, func, testType, numTests, args[4], args[5]);
			}
			
		} catch (IOException e) {
			// FIXME: need a better error reporting mechanism
			System.err.println("internal failure: " + e.getMessage());
			e.printStackTrace();
			return Result.INTERNAL_FAILURE;
		}
		return Result.SUCCESS;
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
	private static Build.Project createWhileyProject(String dir) throws IOException {
		// The content registry maps file name extensions to their Content.Type.
		Content.Registry registry = new wyc.Activator.Registry();
		// The directory root specified where to look for Whiley / WyIL files.
		DirectoryRoot root = new DirectoryRoot(dir,registry);
		ArrayList<Path.Root> roots = new ArrayList<>();
		roots.add(root);
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
	private void executeTest(Path.ID id, Interpreter interpreter, Decl.FunctionOrMethod dec, TestType testType, int numTest, String lowerLimit, String upperLimit) {
		// Get the method for generating test values
		GenerateTest testGen;
		BigInteger lower = new BigInteger(lowerLimit);
		BigInteger upper = new BigInteger(upperLimit);
		if(testType == TestType.EXHAUSTIVE) {
			testGen = new ExhaustiveGenerateTest(dec,interpreter.getTypeSystem(), numTest, lower, upper);
		}
		else {
			testGen = new RandomGenerateTest(dec, interpreter.getTypeSystem(), lower, upper);
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
				
		// Have to remove the pre and post conditions out of the 
		// function so the function is executed without validation
		// Validation will be conducted manually inside the function.
		Tuple<Expr> empty = new Tuple<Expr>();		
		dec.setOperand(4, empty); // Remove precondition
		dec.setOperand(5, empty); // Remove postcondition

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
				continue;
			}
			
			System.out.println("INPUT: " + Arrays.toString(paramValues));
			// Checks the postcondition when it is executed
			RValue[] returns = null;
			try {
				returns = interpreter.execute(name, type, frame, paramValues);
				// Add the return values into the frame for validation
				for(int j=0; j < outputParameters.size(); j++) {
					Decl.Variable parameter = outputParameters.get(j);
					Type paramType = parameter.getType();
					// Check the nominal type postcondition
					if(paramType instanceof Type.Nominal) {
						Type.Nominal nom = (Type.Nominal) paramType;
						Decl.Type decl = interpreter.getTypeSystem().resolveExactly(nom.getName(), Decl.Type.class);
						RValue.Bool valid = returns[j].checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
						if(valid == RValue.Bool.False) {
							System.out.println("Post condition for " + parameter  + " failed");
							throw new AssertionError("");
						}
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
			} catch (ResolutionError e) {
				// FIXME
				e.printStackTrace();
				assert false;
			}
		}
		// Overall test statistics
		if(numPassed == numTest) {
			System.out.printf("Ok: ran %d tests%n", numTest);
		}
		else {
			int numFailed = numTest - numPassed;
			System.out.printf("Failed: %d passed (%.2f %%), %d failed (%.2f %%), ran %d tests%n",
					numPassed, (double) 100 * numPassed/numTest, numFailed, (double) 100 * numFailed/numTest, numTest);
		}
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
