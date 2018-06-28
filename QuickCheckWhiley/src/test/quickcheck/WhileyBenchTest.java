package test.quickcheck;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import quickcheck.RunTest.Result;
import test.utils.TestHelper;

/**
 * Execute all the Whiley Benchmark files 
 * using QuickCheck for Whiley.
 * 
 * Uses code from the Whiley Compiler, wyc.testing by David J. Pearce
 * @author Janice Chin
 *
 */
@RunWith(Parameterized.class)
public class WhileyBenchTest {
	/**
	 * The directory where you want to store the results
	 */
	public final static String RESULT_DIR = "tests/bench_results/".replace('/', File.separatorChar);
	
	/**
	 * The directory containing the source files for each test case. 
	 * Every test corresponds to a file in this directory.
	 */
	public final static String TEST_DIR = "D://Documents/University/ENGR489/WyBench-develop/src".replace('/', File.separatorChar);
	
	/**
	 * Test helper used for compiling Whiley files
	 */
	public final static TestHelper helper = new TestHelper(TEST_DIR);

	/**
	 * Ignored tests and a reason why we ignore them.
	 */
	public final static Map<String, String> IGNORED = new HashMap<>();
	
	// ======================================================================
		// Tests
		// ======================================================================

		// Parameter to test case is the name of the current test.
		// It will be passed to the constructor by JUnit.
		private final String testName;
		
		public WhileyBenchTest(String testName) {
			this.testName = testName;
		}

		// Here we enumerate all available test cases.
		@Parameters(name = "{0}")
		public static Collection<Object[]> data() throws IOException {
			return TestHelper.findRecursiveTestNames(TEST_DIR);
		}

		// Skip ignored tests
//		@Before
//		public void beforeMethod() {
//			String ignored = IGNORED.get(this.testName);
//			Assume.assumeTrue("Test " + this.testName + " skipped: " + ignored, ignored == null);
//		}

		@Test
		public void benchmark() throws IOException {
			helper.compile(this.testName);
			
			String fileName = this.testName.replace(File.separatorChar, '_');
			
	        // Set system output to the file
			File file = new File(RESULT_DIR + fileName + "_result.txt");
			PrintStream stream = new PrintStream(file);
			System.setOut(stream);
			
			// Run tests
	        try {
	            String[] args = new String[] {TEST_DIR + File.separatorChar + this.testName, "exhaustive", "100", "-5", "0"};            
	            Result result = helper.createRunTest(args);
	            assertEquals("A test failed with negative integer limits", Result.PASSED, result);
	            
	            // Positive
	            args = new String[] {TEST_DIR + "/" + this.testName, "exhaustive", "100", "0", "5"};
	            result = helper.createRunTest(args);
	            assertEquals("A test failed with negative integer limits", Result.PASSED, result);
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
