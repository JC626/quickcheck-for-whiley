package quickcheck;

import quickcheck.util.TestType;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;

/**
 * Reads a Wyil file, creating and executing tests for each function in the file.
 * The tests uses the precondition to select suitable candidate tests and validates 
 * the tests using the postcondition.
 * 
 * Test values are generated randomly or exhaustively as configured by the user.
 * 
 * @author Janice Chin
 *
 */
public class QuickCheck {
		
	/**
	 * Extract the path ID for the given filename. This is a relative path from the
	 * project root.
	 *
	 * @param filename
	 * @return The path ID of the filename
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
			System.out.println("Usage: java QuickCheck <wyilfile> <testtype> <numtests> <lowerintegerlimit> <upperintegerlimit>");
			System.exit(-1);
		}
		// Get the filepath e.g test/helloworld.wyil
		String filepath = args[0];
		int lastSlash = filepath.lastIndexOf("/");
		// If the current directory is used to find the file
		String relativePath = ".";
		String filename = filepath;
		// If the file is in a subdirectory
		if(lastSlash > -1) {
			relativePath = filepath.substring(0, lastSlash);
			filename = filepath.substring(lastSlash+1);
		}
		Path.ID id = extractPathID(filename);
		// Get the test type
		TestType testType = TestType.RANDOM;
		if(args.length >= 2) {
			String type = args[1];
			if(type.equalsIgnoreCase("exhaustive")) {
				 testType = TestType.EXHAUSTIVE;
			}
			// Default is random testing
		}
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry,Logger.NULL);
		String numTests = args.length >= 3 ? args[2] : Integer.toString(RunTest.NUM_TESTS);
		String lowerLimit = args.length >= 4 ? args[3] : Integer.toString(RunTest.INT_LOWER_LIMIT);
		String upperLimit = args.length >= 5 ? args[4] : Integer.toString(RunTest.INT_UPPER_LIMIT);
		cmd.execute(relativePath, id.toString(), testType.toString(), numTests, lowerLimit, upperLimit);			
	}
}
