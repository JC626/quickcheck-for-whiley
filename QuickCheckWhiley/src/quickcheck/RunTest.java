package quickcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import quickcheck.generator.GenerateTest;
import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.StdProject;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.util.AbstractProjectCommand;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.Interpreter;
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
		Type.Callable type = dec.getType();
		for(int i=0; i < numTest; i++) {
			RValue[] params = testGen.generateParameters();
			System.out.println("INPUT: " + Arrays.toString(params));
			RValue[] returns = interpreter.execute(name, type, interpreter.new CallStack(), params);
			// Print out any return values produced
			if (returns != null) {
				System.out.println("OUTPUT: " + Arrays.toString(returns));
			}
		}
	}
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
					// TODO create GenerateTest instead
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
