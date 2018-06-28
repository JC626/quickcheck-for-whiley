package test.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import quickcheck.QuickCheck;
import quickcheck.RunTest;
import wybs.lang.Build;
import wybs.util.StdProject;
import wyc.command.Compile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.Function;
import wyc.util.TestUtils;
import wycc.util.Logger;
import wycc.util.Pair;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.DirectoryRoot;
import wyfs.util.Trie;

/**
 * Helper functions used for unit testing
 * 
 * @author Janice Chin
 *
 */
public class TestHelper {
	/**
	 * Directory where the tests are
	 */
	private String directory;
	
	public TestHelper(String directory) {
		this.directory = directory;
	}
	
	/**
	 * Compile a .whiley file
	 * @param testName The name of the test
	 * @throws IOException
	 */
	public void compile(String testName) throws IOException{		
		File whileySrcDir = new File(directory);
		String whileyFilename = directory + File.separatorChar + testName
				+ ".whiley";
		// Compile the file
		Pair<Compile.Result,String> p = TestUtils.compile(
				whileySrcDir,      // location of source directory
				false,               // no verification
				whileyFilename);     // name of test to compile

		Compile.Result r = p.first();

		if (r != Compile.Result.SUCCESS) {
			fail("Test failed to compile!");
		} else if (r == Compile.Result.INTERNAL_FAILURE) {
			fail("Test caused internal failure!");
		}
	}
	
	/**
	 * Basically, a copy of QuickCheck's main method 
	 * so that RunTest can be executed
	 * @param args The command line arguments used to execute the test
	 * @return Result of executing a test
	 */
	public RunTest.Result createRunTest(String[] args) {
		String[] modifiedArgs = QuickCheck.prepareArguments(args);
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry, Logger.NULL);
		return cmd.execute(modifiedArgs);		
	}
	
	/**
	 * Create a project
	 * @return A project to use for testing
	 * @throws IOException
	 */
	public Build.Project createProject() throws IOException {
		Content.Registry registry = new wyc.Activator.Registry();
		// The directory root specified where to look for Whiley / WyIL files.
		DirectoryRoot root = new DirectoryRoot(directory, registry);
		ArrayList<Path.Root> roots = new ArrayList<>();
		roots.add(root);
		return new StdProject(roots);
	}
	
	/**
	 * Get the functions from the testName file
	 * by using Java reflection on RunTest
	 * @param testName The filename of the test
	 * @param project The project
	 * @return Functions in the file
	 */
	@SuppressWarnings("unchecked")
	public List<Decl.Function> getFunctions(String testName, Build.Project project){
		// Create ID from ROOT constant
		Path.ID id = Trie.ROOT.append(testName);
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry,Logger.NULL);
		Method met;
		try {
			met = cmd.getClass().getDeclaredMethod("getFunctions", Path.ID.class, Build.Project.class);
			met.setAccessible(true);
			List<Decl.Function> functions = (List<Function>) met.invoke(cmd, id, project);
			return functions;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			fail("Could not get the functions");
		}
		fail("Functions could not be found for " + testName);
		return null;
	}
}
