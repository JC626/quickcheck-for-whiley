package test.quickcheck;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Execute all tests
 * 
 * @author Janice Chin
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	GenerateRandomTest.class,
	GenerateExhaustiveTest.class,
	RangeTest.class,
	QCInterpreterTest.class
})
public class AllTests {
}