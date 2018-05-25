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
	
	// Ignored tests from wyc.testing.AllValidTest and wyc.testing.AllValidVerificationTest
	static {
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
		// Null Checks as Type Tests in VCG (closed)
		IGNORED.put("IfElse_Valid_4", "#712");
		IGNORED.put("Complex_Valid_4", "#712");
		IGNORED.put("ListAccess_Valid_6", "712");
		// Verification Condition Generation and Dereference Assignment
		IGNORED.put("Process_Valid_1", "#743");
		IGNORED.put("Process_Valid_9", "#743");
		IGNORED.put("Process_Valid_10", "#743");
		IGNORED.put("Process_Valid_11", "#743");
		IGNORED.put("Reference_Valid_2", "#743");
		IGNORED.put("Reference_Valid_3", "#743");
		IGNORED.put("Reference_Valid_6", "#743");
		// Flow Typing and VerificationConditionGeneration
		IGNORED.put("RecursiveType_Valid_3", "#781");
		// WyTP Variable Ordering Effect
		IGNORED.put("ConstrainedList_Valid_26", "#782");
		// ===============================================================
		// Whiley Theorem Prover faults
		// ===============================================================
		// Issue 2 "Verification of Remainder Operator"
		IGNORED.put("ConstrainedInt_Valid_22", "WyTP#2");
		// Issue 12 "Support for Non-linear Arthmetic"
		IGNORED.put("IntMul_Valid_2", "WyTP#12");
		IGNORED.put("While_Valid_27", "WyTP#12");
		IGNORED.put("While_Valid_32", "WyTP#12");
		// Issue 29 "Triggerless Quantifier Instantiation"
		IGNORED.put("Assert_Valid_1", "#29");
		IGNORED.put("ConstrainedList_Valid_14", "WyTP#29");
		IGNORED.put("ConstrainedList_Valid_18", "WyTP#29");
		IGNORED.put("Quantifiers_Valid_2", "WyTP#29");
		// Issue 36 "Support for Division Operator Feature"
		IGNORED.put("Cast_Valid_5", "WyTP#36");
		IGNORED.put("IntOp_Valid_1", "WyTP#36");
		IGNORED.put("IntDiv_Valid_3", "WyTP#36");
		IGNORED.put("Lambda_Valid_3", "WyTP#36");
		IGNORED.put("Lambda_Valid_4", "WyTP#36");
		// Issue 41 "Case Split within Quantifier"
		IGNORED.put("Property_Valid_4", "WyTP#41");
		IGNORED.put("Subtype_Valid_5", "WyTP#41");
		IGNORED.put("RecursiveType_Valid_19", "WyTP#41");
		// Issue 76 "Casting Record Types"
		IGNORED.put("Coercion_Valid_9", "WyTP#76");
		IGNORED.put("RecordCoercion_Valid_1", "WyTP#76");
		// Issue 80 "(Non-)Empty Type"
		IGNORED.put("OpenRecord_Valid_4", "WyTP#80");
		IGNORED.put("OpenRecord_Valid_9", "WyTP#80");
		// Issue 85 "NegativeArraySizeException in CoerciveSubtypeOperator"
		IGNORED.put("ConstrainedRecord_Valid_9", "WyTP#85");
		IGNORED.put("TypeEquals_Valid_54", "WyTP#85");
		// Issue 89 "Unknown Assertion Failure"
		IGNORED.put("While_Valid_37", "WyTP#89");
		// Issue 102 "Support Reference Lifetimes"
		IGNORED.put("Lifetime_Valid_8", "WyTP#102");
		// Issue 104 "Incompleteness in CoerciveSubtypeOperator"
		IGNORED.put("RecursiveType_Valid_7", "WyTP#104");
		IGNORED.put("While_Valid_15", "WyTP#104");
		IGNORED.put("While_Valid_20", "WyTP#104");
		// Issue 107 "Limitation with ReadableRecordExtractor"
		IGNORED.put("TypeEquals_Valid_30", "WyTP#107");
		IGNORED.put("TypeEquals_Valid_25", "WyTP#107");
		// Issue 111 "Infinite Recursive Expansion"
		IGNORED.put("RecursiveType_Valid_28", "WyTP#111");
		IGNORED.put("RecursiveType_Valid_29", "WyTP#111");
		IGNORED.put("Complex_Valid_3", "WyTP#111");
		IGNORED.put("Complex_Valid_8", "WyTP#111");
		IGNORED.put("RecursiveType_Valid_2", "WyTP#111");
		// Issue 112 "More Performance Problems with Type Checking"
		IGNORED.put("Complex_Valid_2", "WyTP#112");
		IGNORED.put("BoolList_Valid_3", "WyTP#112");
		IGNORED.put("While_Valid_2", "WyTP#112");
		IGNORED.put("While_Valid_26", "WyTP#112");
		// Issue 114 "Limitation with TypeTestClosure"
		IGNORED.put("RecursiveType_Valid_4", "WyTP#114");
		//
		IGNORED.put("Ensures_Valid_6", "WyTP#133");
		IGNORED.put("RecursiveType_Valid_22", "WyTP#133");
		IGNORED.put("While_Valid_24", "WyTP#133");
		IGNORED.put("While_Valid_34", "#133");
		IGNORED.put("While_Valid_35", "WyTP#133");
		//
		IGNORED.put("String_Valid_6", "??");
		IGNORED.put("RecursiveType_Valid_12", "??");
		// Performance problems?
		IGNORED.put("Complex_Valid_10", "??");
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

//		// Check which limits pass
//		boolean negative = true;
//		boolean positive = true;
//		try {
//			// Negative limits
//			try {
//				String[] args = new String[] {TEST_DIR + "/" + this.testName, "exhaustive", "100", "-5", "0"};
//				QuickCheck.main(args);
//			}
//			catch (Error e) {
//				if(e.getMessage().equals("Upper integer limit is less than or equal to the lower integer limit")) {
//					negative = false;
//				}
//				else {
//					throw e;
//				}
//			}
//			try {
//				// Positive
//				String[] args = new String[] {TEST_DIR + "/" + this.testName, "exhaustive", "100", "0", "5"};
//				QuickCheck.main(args);
//			}
//			catch (Error e) {
//				if(e.getMessage().equals("Upper integer limit is less than or equal to the lower integer limit")) {
//					positive = false;
//				}
//				else {
//					throw e;
//				}
//			}
//		}
//		finally {
//			stream.close();
//			// Delete file if it is empty
//			if(file.length() == 0) {
//				file.delete();
//			}
//		}
//		if(!positive && !negative) {
//			fail("No possible values could be generated at all");
//		}
	}

}
