package test.quickcheck;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import quickcheck.RunTest.Result;
import test.utils.TestHelper;
import wyc.util.TestUtils;

/**
 * Execute all the Whiley Invalid tests using QuickCheck for Whiley.
 * 
 * Uses code from the Whiley Compiler, wyc.testing by David J. Pearce
 * @author Janice Chin
 *
 */
@RunWith(Parameterized.class)
public class WhileyInvalidTest {
	/**
	 * The directory where you want to store the results
	 */
	public final static String RESULT_DIR = "tests/invalid_results/".replace('/', File.separatorChar);
	
	/**
	 * The directory containing the source files for each test case. 
	 * Every test corresponds to a file in this directory.
	 */
	public final static String TEST_DIR = "D://Documents/University/ENGR489/WhileyCompiler-0.5.6/tests/invalid".replace('/', File.separatorChar);
	
	/**
	 * Test helper used for compiling Whiley files
	 */
	public final static TestHelper helper = new TestHelper(TEST_DIR);

	/**
	 * Ignored tests and a reason why we ignore them.
	 */
	public final static Map<String, String> IGNORED = new HashMap<>();
	

	static {
		IGNORED.put("Export_Invalid_1", "unclassified");
		IGNORED.put("Function_Invalid_2", "unclassified");
		IGNORED.put("Function_Invalid_9", "unclassified");
		IGNORED.put("Native_Invalid_1", "unclassified");
		//
		IGNORED.put("Parsing_Invalid_1", "608");
		IGNORED.put("Parsing_Invalid_2", "608");
		//
		IGNORED.put("Parsing_Invalid_15", "609");
		IGNORED.put("Parsing_Invalid_27", "609");
		IGNORED.put("Parsing_Invalid_28", "609");
		// Normalisation for Method Subtyping
		IGNORED.put("Lifetime_Lambda_Invalid_3", "#794");
		// Support Captured Lifetime Parameters
		IGNORED.put("Lifetime_Lambda_Invalid_5", "#795");
		IGNORED.put("Lifetime_Lambda_Invalid_6", "#765");
		// Access Static Variable from Type Invariant
		IGNORED.put("Type_Invalid_11", "793");
		// Infinite Array Types
		IGNORED.put("Type_Invalid_10", "823");
		// ===============================================================
		// Whiley Theorem Prover faults
		// ===============================================================
		IGNORED.put("RecursiveType_Invalid_9", "unclassified");
		IGNORED.put("RecursiveType_Invalid_2", "WyTP#26");
	}

	// ======================================================================
	// Tests
	// ======================================================================

	// Parameter to test case is the name of the current test.
	// It will be passed to the constructor by JUnit.
	private final String testName;
	
	public WhileyInvalidTest(String testName) {
		this.testName = testName;
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return TestUtils.findTestNames(TEST_DIR);
	}

	// Skip ignored tests
	@Before
	public void beforeMethod() {
		String ignored = IGNORED.get(this.testName);
		Assume.assumeTrue("Test " + this.testName + " skipped: " + ignored, ignored == null);
	}

	@Test
	public void invalid() throws IOException {
		try {
			helper.compile(this.testName);

		}
		catch(AssertionError e){
			if(e.getMessage().equals("Test failed to compile!")) {
				return;
			}
			else {
				throw e;
			}
		}
        // Set system output to the file
		File file = new File(RESULT_DIR + this.testName + "_result.txt");
		PrintStream stream = new PrintStream(file);
		System.setOut(stream);
		
		// Run tests
        try {
        	// Negative
            String[] args = new String[] {TEST_DIR + File.separatorChar + this.testName, "exhaustive", "100", "-5", "0"};
            Result result = helper.createRunTest(args);
            if(result == Result.FAILED) {
            	// A negative test failed.
            	return;
            }
            // Positive
            args = new String[] {TEST_DIR + File.separatorChar + this.testName, "exhaustive", "100", "0", "5"};
            result = helper.createRunTest(args);
            assertEquals("All tests passed when some tests should fail.", Result.FAILED, result);   
            return;
        }
        catch(Error e) {
        	return;
        }
		finally {
			stream.close();
			// Delete file if it is empty
			if(file.length() == 0) {
				file.delete();
			}
		}        
	}

}
