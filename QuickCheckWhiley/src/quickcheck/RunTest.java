package quickcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wybs.lang.Build;
import wybs.lang.NameID;
import wybs.util.StdProject;
import wybs.util.AbstractCompilationUnit.Identifier;
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
			Type.Function sig = new Type.Function(new Tuple<>(new Type[0]), new Tuple<>());
			List<Decl.Function> functions = getFunctions(id, project);
			
			// FIXME print statements
			Decl.Function function = functions.get(0);
			System.out.println(function.getName());
			System.out.println(function.getRequires());
			System.out.println(function.getEnsures());
			System.out.println(function.getParameters());
			System.out.println(function.getReturns());

			// TODO Generate tests
			//executeFunctionOrMethod(name, sig, project);
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
	
//	/**
//	 * Execute a given function or method in a wyil file.
//	 *
//	 * @param id
//	 * @param signature
//	 * @param project
//	 * @throws IOException
//	 */
//	private void executeFunctionOrMethod(NameID id, Type.Callable signature, Build.Project project)
//			throws IOException {
//		// Try to run the given function or method
//		Interpreter interpreter = new Interpreter(project, System.out);
//		RValue[] returns = interpreter.execute(id, signature, interpreter.new CallStack());
//		// Print out any return values produced
//		if (returns != null) {
//			for (int i = 0; i != returns.length; ++i) {
//				if (i != 0) {
//					System.out.println(", ");
//				}
//				System.out.println(returns[i]);
//			}
//		}
//	}
	
	// TODO map of NameId (function ID) -> GenerateTest 
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
