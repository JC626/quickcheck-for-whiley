package test.quickcheck;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class WhileyRunner {
	
	public static final int NUM_RUNS = 6;
	

	public static void main(String[] args) throws IOException {
		runnerTest("INVALID_TEST_RESULT.csv", "Whiley Invalid Tests", WhileyInvalidTest.class);
		runnerTest("VALID_TEST_RESULT.csv", "Whiley Valid Tests", WhileyValidTest.class);
		runnerTest("BENCHMARK_TEST_RESULT.csv", "Whiley Benchmark Tests", WhileyBenchTest.class);
	}
	
	public static void runnerTest(String fileName, String testSuiteName, Class<?> testClass) throws IOException {
		String[] types = new String[] {"None", "Function Optimisation", "Memoisation", "Function Optimisation & Memoisation"};
		FileWriter fw = new FileWriter(fileName, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		out.print(testSuiteName);
		out.print("Passed,");
		out.print("Failed,");
		out.print("Skipped,");
		out.print("Executed,");
		out.print("Total,");
		out.println("Execution Time");
		out.flush();
		for(String type : types) {
			if(type.equals("None")) {
				System.setProperty("memoisation", "false");
				System.setProperty("optimisation", "false");
			}
			else if(type.equals("Function Optimisation")) {
				System.setProperty("memoisation", "false");
				System.setProperty("optimisation", "true");
			}
			else if(type.equals("Memoisation")){
				System.setProperty("memoisation", "true");
				System.setProperty("optimisation", "false");
			}
			else {
				System.setProperty("memoisation", "true");
				System.setProperty("optimisation", "true");
			}
			for(int i=0; i < NUM_RUNS; i++) {
				Result result = JUnitCore.runClasses(testClass);
//				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				if(i==0) {
					continue;
				}
				int numFailed = result.getFailureCount();
				int numExecuted = result.getRunCount();
				int numSkipped = result.getIgnoreCount();
				out.print(type);
				out.print(",");
				out.print(numExecuted - numSkipped - numFailed);
				out.print(",");
				out.print(numFailed);
				out.print(",");
				out.print(numSkipped);
				out.print(",");
				out.print(numExecuted - numSkipped);
				out.print(",");
				out.print(numExecuted);
				out.print(",");
				out.println(result.getRunTime());
				out.flush();
				// Record the failures in a file
				String failuresFile = testSuiteName + " - " + type + " #" + i + ".txt";
				FileWriter failureW = new FileWriter(failuresFile, true);
				BufferedWriter failureBw = new BufferedWriter(failureW);
				PrintWriter failureOut = new PrintWriter(failureBw);
				for(Failure f : result.getFailures()) {
					failureOut.println(f.toString());
					failureOut.println(f.getTrace());
				}
				failureOut.close();
			}
		}
		out.close();
	}
}
