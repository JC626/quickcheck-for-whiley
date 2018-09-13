package quickcheck.generator.type;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import quickcheck.util.TestType;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
import wyc.lang.WhileyFile.Stmt;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter.CallStack;

/**
 * Generate the return value of a function
 * that meets its postcondition.
 * 
 * @author Janice Chin
 *
 */
public class FunctionGenerator implements Generator{
	private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final int RETURN_NAME_MAX = 5;
	private static final int RETURN_NAME_MIN = 3;

	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();

	/** Generators corresponding to each field */
	private List<Generator> generators;

	private Interpreter interpreter;

	/** Current return elements generated */
	private RValue[] returnElements;
		
	private Decl.Lambda lambda;
	private Stmt body;
	private List<Identifier> returnNames;

	private TestType testType;

	private int size;
	private int count = 1;

	public FunctionGenerator(List<Generator> generators, WhileyFile.Type.Callable funcType, Interpreter interpreter, TestType testType, int numTests) {
		this.generators = generators;
		this.interpreter = interpreter;
		this.testType = testType;
		calculateSize();
		
		// Set input and output parameters for creating a lambda
		List<Decl.Variable> inputVars = new ArrayList<Decl.Variable>();
		for(WhileyFile.Type t : funcType.getParameters()) { 
			WhileyFile.Decl.Variable var = new WhileyFile.Decl.Variable(null, null, t);
			inputVars.add(var);
		}
		List<WhileyFile.Expr> returnStmts = new ArrayList<WhileyFile.Expr>();
		List<Decl.Variable> outputVars = new ArrayList<Decl.Variable>();
		this.returnNames = new ArrayList<Identifier>();
		Tuple<WhileyFile.Type> returnTypes = funcType.getReturns();
		Set<String> returnNamesUsed = new HashSet<String>();
		/*
		 * The return statement in the lambda is returning
		 * a local variable(s).
		 * The local variable(s) are the generated values from the
		 * function.
		 */ 
		for(int i=0; i < returnTypes.size(); i++) {
			// Make a random return name
			String name = createRandomString();
			while(returnNamesUsed.contains(name)) {
				name = createRandomString();
			}
			returnNamesUsed.add(name);
			Identifier id = new Identifier(name);
			returnNames.add(id);
			WhileyFile.Decl.Variable var = new WhileyFile.Decl.Variable(null, id, returnTypes.get(i));
			Expr expr = new WhileyFile.Expr.VariableAccess(returnTypes.get(i), var);
			returnStmts.add(expr);
			outputVars.add(var);
		}		
		this.body = new WhileyFile.Stmt.Block(new WhileyFile.Stmt.Return(new Tuple<WhileyFile.Expr>(returnStmts))); 
		this.lambda = new WhileyFile.Decl.Lambda(null, null, new Tuple<Decl.Variable>(inputVars), new Tuple<Decl.Variable>(outputVars), null, null, null, funcType);
	}
	
	/**
	 * Randomly generate a string that is 
	 * a random length.
	 * This is for generating random return names.
	 * @return A random string
	 */
	private String createRandomString() {
		int len = (int)(Math.random() * ((RETURN_NAME_MAX - RETURN_NAME_MIN) + 1)) + RETURN_NAME_MIN;
		StringBuilder builder = new StringBuilder("$");
		while (len > 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
			len--;
		}
		return builder.toString();
	}

	@Override
	public RValue generate() {
		assert testType == TestType.EXHAUSTIVE;
		if(returnElements == null) {
			returnElements = new RValue[generators.size()];
			for(int i=0; i < returnElements.length; i++) {
				Generator gen = generators.get(i);
				gen.resetCount();
				returnElements[i] = gen.generate();
			}
		}
		else {
			// Generate the array elements backwards
			for(int i=returnElements.length - 1; i >= 0 ; i--) {
				Generator gen = generators.get(i);
				if(!gen.exceedCount()) {
					returnElements[i] = gen.generate();
					break;
				}
				else {
					gen.resetCount();
					returnElements[i] = gen.generate();
				}
			}
		}
		count++;
		CallStack frame = interpreter.new CallStack();
		for(int i=0; i < returnElements.length; i++) {
			frame.putLocal(returnNames.get(i), returnElements[i]);
		}
		return semantics.Lambda(this.lambda, frame, this.body);
	}
	
	@Override
	public RValue generate(int comboNum) {
		RValue[] returnEles = new RValue[generators.size()];
		int leftover = comboNum;
		for(int i=0; i < returnEles.length ; i++) {
			int divNum = 1;
			for(int j = i+1; j < returnEles.length; j++ ) {
				Generator gen = generators.get(j);
				divNum *= gen.size();
			}
			if(i+1 >= returnEles.length) {
				divNum = 0;
			}
			int num = leftover;
			// Note: Num is always rounded down
			if(divNum != 0) {
				num /= divNum;
			}
			Generator gen = generators.get(i);
			returnEles[i] = gen.generate(num);
			leftover -= num * divNum;
		}
		CallStack frame = interpreter.new CallStack();
		for(int i=0; i < returnEles.length; i++) {
			frame.putLocal(returnNames.get(i), returnEles[i]);
		}

		return semantics.Lambda(this.lambda, frame, this.body);
	}

	private void calculateSize() {
		//Calculate size
		if(generators.size() > 0) {
			this.size = 1;
			for(int i=0; i < generators.size(); i++) {
				size *= generators.get(i).size();
			}
		}
		else {
			this.size = 0;
		}
		if(this.size < 0) {
			size = Integer.MAX_VALUE;
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void resetCount() {
		count = 1;
		this.returnElements = null;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}
}