package quickcheck;

import java.io.File;
import java.io.IOException;

import wybs.lang.Build;
import wybs.lang.NameID;
import wyc.command.Run;
import wyc.lang.WhileyFile.Type;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter.CallStack;

/**
 * FIXME
 * Reads a Wyil file, creating and executing
 * randomised tests for each function in the file.
 * The tests uses the precondition
 * to select suitable candidate tests and validates 
 * the tests using the postcondition.
 * 
 * @author Janice Chin
 *
 */
public class QuickCheck {
	
	/**
	 * Based on TestUtils.execWyil
	 * @param wyilDir
	 * @param id
	 * @throws IOException
	 */
	public static void execWyil(File wyilDir, Path.ID id) throws IOException {
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry,Logger.NULL);
		cmd.setWyildir(wyilDir);
		cmd.execute(id.toString());
	}
	
	// TODO something with the IOException
	public static void main(String[] args) throws IOException{
		// TODO get the args to run the test
		String name = "abs"; //args[0]
		// TODO get current directory
		String directory = "examples";
		File whileySrcDir = new File(directory); //new File(".");
		QuickCheck.execWyil(whileySrcDir, Trie.fromString(name));
	}
}
