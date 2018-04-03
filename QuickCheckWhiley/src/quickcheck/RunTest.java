package quickcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import quickcheck.generator.GenerateTest;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.StdProject;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.util.AbstractProjectCommand;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.Interpreter;
import wyil.interpreter.Interpreter.CallStack;
import wyil.interpreter.ConcreteSemantics.RValue;

import static wyc.lang.WhileyFile.*;

/**
 * FIXME
 * Responsible for implementing the command "<code>wy run ...</code>" which
 * loads the appropriate <code>wyil</code> file and gets executes a given method
 * using the <code>Interpreter</code>.
 * 
 * Based on wyc.Command.Run
 *
 * @author Janice Chin
 *
 */
public class RunTest extends AbstractProjectCommand<RunTest.Result> {
	
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
			// FIXME: this is broken
			System.out.println("usage: run <wyilfile> <method>");
			return Result.ERRORS;
		}
		try {
			StdProject project = initialiseProject();
			Path.ID id = Trie.fromString(args[0]);
			List<Decl.Function> functions = getFunctions(id, project);
			// Generate tests for each function
			Interpreter interpreter = new Interpreter(project, System.out);
			for(Decl.Function func : functions) {
				// TODO set number of tests to execute?
				executeTest(id, interpreter, func, 1);
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
	
	// TODO change to return the output values
	/**
	 * Test a function from a Wyil file
	 * by executing the test with randomised paramters
	 * @param id The module used
	 * @param interpreter Whiley interpreter used to execute the function/method
	 * @param dec The function or method
	 * @param numTest The number of tests to execute
	 */
	private void executeTest(Path.ID id, Interpreter interpreter, Decl.FunctionOrMethod dec, int numTest) {
		GenerateTest testGen = new GenerateTest(dec);
		NameID name = new NameID(id, dec.getName().get());
		System.out.println("FUNCTION "+ name);
		System.out.println("FUNCTION PARAM TYPES "+ dec.getParameters());
		System.out.println("PRECONDITION "+ dec.getRequires());
		System.out.println("POSTCONDITION "+ dec.getEnsures());
		Type.Callable type = dec.getType();
		Tuple<Expr> preconditions = dec.getRequires();
		Tuple<Expr> postconditions = dec.getEnsures();
		int numPassed = 0;
		
		for(int i=0; i < numTest; i++) {
			RValue[] params = testGen.generateParameters();
			CallStack frame = interpreter.new CallStack();
			Tuple<Decl.Variable> parameters = dec.getParameters();
			// Check the precondition
			try {
				for(int j=0; j < parameters.size(); j++) {
					Decl.Variable parameter = parameters.get(j);
					frame.putLocal(parameter.getName(), params[j]);
				}
				interpreter.checkInvariants(frame, preconditions);
			}
			catch(AssertionError e){
				System.out.println("Pre-condition failed on input: " + Arrays.toString(params));
				continue;
			}
			
			System.out.println("INPUT: " + Arrays.toString(params));
			// Checks the postcondition when it is executed
			RValue[] returns = null;
			try {
				returns = interpreter.execute(name, type, frame, params);
				numPassed++;
//				// Print out any return values produced
//				if (returns != null) {
//					System.out.println("OUTPUT: " + Arrays.toString(returns));
//				}
			}
			catch(AssertionError e) {
				// TODO Would be nice to see the actual output, if postcondition failed!
//				System.out.printf("Failed Input: %s Output: %s%n", Arrays.toString(params), Arrays.toString(returns));
				System.out.printf("Failed Input: %s", Arrays.toString(params));
			}
		}
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
