package quickcheck;

import java.io.File;
import java.util.concurrent.TimeUnit;

import quickcheck.RunTest.Result;
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
	
	public static String[] prepareArguments(String[] args) {
		if(args.length == 0) {
			System.out.println("Usage: java QuickCheck <wyilfile> <testtype> <numtests> <lowerintegerlimit> <upperintegerlimit> <functionoptimisation> <num_gen_func_opt>");
			System.exit(-1);
		}
		String[] modified = new String[RunTest.MAX_NUMBER_ARGUMENTS];
		// Get the filepath e.g test/helloworld.wyil
		String filepath = args[0];
		filepath = filepath.replace('\\', File.separatorChar);
		filepath = filepath.replace('/', File.separatorChar);
		int lastSlash = filepath.lastIndexOf(File.separatorChar);
		// If the current directory is used to find the file
		String relativePath = ".";
		String filename = filepath;
		// If the file is in a subdirectory
		if(lastSlash > -1) {
			relativePath = filepath.substring(0, lastSlash);
			filename = filepath.substring(lastSlash+1);
		}
		modified[0] = relativePath;
		Path.ID id = extractPathID(filename);
		modified[1] = id.toString();
		// Get the test type, Default is random testing
		TestType testType = TestType.RANDOM;
		if(args.length >= 2) {
			String type = args[1];
			if(type.equalsIgnoreCase("exhaustive")) {
				 testType = TestType.EXHAUSTIVE;
			}
		}
		modified[2] = testType.toString();
		modified[3] = args.length >= 3 ? args[2] : Integer.toString(RunTest.NUM_TESTS);
		modified[4] = args.length >= 4 ? args[3] : Integer.toString(RunTest.INT_LOWER_LIMIT);
		modified[5] = args.length >= 5 ? args[4] : Integer.toString(RunTest.INT_UPPER_LIMIT);
		// Function optimisation flags
		modified[6] = args.length >= 6 ? args[5] : Boolean.toString(QCInterpreter.FUNCTION_OPTIMISATION);
		modified[7] = args.length >= 7 ? args[6] : Integer.toString(QCInterpreter.NUM_GEN_FUNC_OPT);
		return modified;
	}
	
	public static void main(String[] args){
		long startTime = System.nanoTime();
		if(args.length == 0) {
			System.out.println("Usage: java QuickCheck <wyilfile> <testtype> <numtests> <lowerintegerlimit> <upperintegerlimit> <functionoptimisation> <num_gen_func_opt>");
			System.exit(-1);
		}
		String[] modifiedArgs = prepareArguments(args);
		Content.Registry registry = new wyc.Activator.Registry();
		RunTest cmd = new RunTest(registry, Logger.NULL);
		RunTest.Result result = cmd.execute(modifiedArgs);			
		long endTime = System.nanoTime();
		System.out.println("Execution time: "+ TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " milliseconds"); 
		if(result == Result.PASSED) {
			System.out.println("All tests passed.");
		}
		else if(result == Result.PASSED) {
			System.out.println("Some tests failed.");
		}
		else {
			System.out.println("An error occurred during testing.");
		}
	}
}
