package quickcheck;

import java.io.IOException;

import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;

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
	public static void execWyil(String dir, Path.ID id) throws IOException {
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry,Logger.NULL);
		cmd.execute(dir, id.toString());
	}
	
	/**
	 * Extract the path ID for the given filename. This is a relative path from the
	 * project root.
	 *
	 * @param filename
	 * @return
	 */
	public static Path.ID extractPathID(String filename) {
		// Strip the filename extension
		filename = filename.replace(".wyil", "");
		// Create ID from ROOT constant
		Path.ID id = Trie.ROOT.append(filename);
		return id;
	}
	
	public static void main(String[] args){
		if(args.length == 0) {
			System.out.println("Usage: java QuickCheck <wyilfile>");
			System.exit(-1);
		}
		try {
			String filepath = args[0];
			int lastSlash = filepath.lastIndexOf("/");
			String relativePath = filepath.substring(0, lastSlash);
			String filename = filepath.substring(lastSlash+1);
			Path.ID id = extractPathID(filename);
			filepath.lastIndexOf(filename);
			execWyil(relativePath, id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
