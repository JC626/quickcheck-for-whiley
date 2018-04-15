package quickcheck;

import quickcheck.generator.TestType;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import wyfs.util.Trie;

/**
 * FIXME Doc QC
 * Reads a Wyil file, creating and executing tests for each function in the file.
 * The tests uses the precondition
 * to select suitable candidate tests and validates 
 * the tests using the postcondition.
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
		// TODO have a Map<String, Object> for customisation of the generator?
		if(args.length == 0) {
			System.out.println("Usage: java QuickCheck <wyilfile> <testtype> <numtests> <lowerintegerlimit> <upperintegerlimit>");
			System.exit(-1);
		}
		String filepath = args[0];
		int lastSlash = filepath.lastIndexOf("/");
		String relativePath = filepath.substring(0, lastSlash);
		String filename = filepath.substring(lastSlash+1);
		Path.ID id = extractPathID(filename);
		filepath.lastIndexOf(filename);
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
		String lowerLimit = args.length >= 4 ? args[3] : Integer.toString(RunTest.LOWER_LIMIT);
		String upperLimit = args.length >= 5 ? args[4] : Integer.toString(RunTest.UPPER_LIMIT);
		cmd.execute(relativePath, id.toString(), testType.toString(), numTests, lowerLimit, upperLimit);			
	}
}
