package test.quickcheck;

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

import quickcheck.QuickCheck;
import test.utils.TestHelper;
import wyc.util.TestUtils;

/**
 * Execute all the Whiley Valid tests using QuickCheck for Whiley.
 * 
 * Uses code from the Whiley Compiler, wyc.testing by David J. Pearce
 * @author Janice Chin
 *
 */
@RunWith(Parameterized.class)
public class WhileyValidTest {
	/**
	 * The directory where you want to store the results
	 */
	public final static String RESULT_DIR = "tests/valid_results/";
	
	/**
	 * The directory containing the source files for each test case. 
	 * Every test corresponds to a file in this directory.
	 */
	public final static String TEST_DIR = "D://Documents/University/ENGR489/WhileyCompiler-0.5.6/tests/valid";
	
	/**
	 * Test helper used for compiling Whiley files
	 */
	public final static TestHelper helper = new TestHelper(TEST_DIR);

	/**
	 * Ignored tests and a reason why we ignore them.
	 */
	public final static Map<String, String> IGNORED = new HashMap<>();

	static {
		// Problem Type Checking Union Type
		IGNORED.put("RecordSubtype_Valid_1", "#696");
		IGNORED.put("RecordSubtype_Valid_2", "#696");
		// Function Overloading for Nominal Types
		IGNORED.put("Function_Valid_11", "#702");
		IGNORED.put("Function_Valid_15", "#702");
		//  Normalisation for Method Subtyping
		IGNORED.put("Lifetime_Lambda_Valid_2", "#794");
		IGNORED.put("Lifetime_Lambda_Valid_5", "#794");
		IGNORED.put("Lifetime_Lambda_Valid_6", "#794");
		// Support Captured Lifetime Parameters
		IGNORED.put("Lifetime_Lambda_Valid_7", "#795");
		// Type Tests with Invariants
		IGNORED.put("TypeEquals_Valid_23", "#787");
		IGNORED.put("TypeEquals_Valid_25", "#787");
		IGNORED.put("TypeEquals_Valid_30", "#787");
		IGNORED.put("TypeEquals_Valid_41", "#787");
		//
		IGNORED.put("ConstrainedReference_Valid_1", "#827");
		// Unclassified
		IGNORED.put("Lifetime_Valid_8", "???");
	}

	// ======================================================================
	// Tests
	// ======================================================================

	// Parameter to test case is the name of the current test.
	// It will be passed to the constructor by JUnit.
	private final String testName;
	
	public WhileyValidTest(String testName) {
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
	public void valid() throws IOException {
		helper.compile(this.testName);
        // Set system output to the file
		File file = new File(RESULT_DIR + this.testName + "_result.txt");
		PrintStream stream = new PrintStream(file);
		System.setOut(stream);
		
		// Run tests
		// Negative
		try {
			String[] args = new String[] {TEST_DIR + "/" + this.testName, "exhaustive", "100", "-5", "0"};
			QuickCheck.main(args);
			
			// Positive
			args = new String[] {TEST_DIR + "/" + this.testName, "exhaustive", "100", "0", "5"};
			QuickCheck.main(args);
			
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
