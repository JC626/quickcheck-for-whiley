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
 * Reads a WyIl? file, creating and executing
 * tests for each function in the file
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
		// TODO remove name 
		cmd.execute(id.toString(), "something");
	}
	
	// TODO something with the IOException
	public static void main(String[] args) throws IOException{
		// TODO get the args to run the test
		String name = "abs"; //args[0]
		// TODO get current directory
		String directory = "tests";
		File whileySrcDir = new File(directory); //new File(".");
		QuickCheck.execWyil(whileySrcDir, Trie.fromString(name));
	}
}
