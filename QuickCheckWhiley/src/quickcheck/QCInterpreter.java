package quickcheck;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import quickcheck.generator.GenerateTest;
import quickcheck.generator.RandomGenerateTest;
import wybs.lang.Build;
import wybs.lang.NameResolver.ResolutionError;
import wybs.util.AbstractCompilationUnit.Tuple;
import wybs.lang.SyntacticElement;
import wyil.interpreter.Interpreter;
import wyil.type.TypeSystem;

import static wyc.lang.WhileyFile.*;
import static wyil.interpreter.ConcreteSemantics.LValue;
import static wyil.interpreter.ConcreteSemantics.RValue;

import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Decl.FunctionOrMethod;
import wyc.lang.WhileyFile.Expr;
import wyc.lang.WhileyFile.Type;

/**
 * 
 * A majority of the code is a copy of wyil.interpreter.Interprter
 * by David J. Pearce.
 * 
 * This is due to the need to override a certain private method,
 * which also calls other private methods.
 * 
 * However, it is not possible to override private methods from 
 * a super class in Java.
 * Ideally, all the private methods would be protected
 * so they can be overridden and called.
 *
 */
public class QCInterpreter extends Interpreter {

	/**
	 * Provides mechanism for operating on types. For example, expanding them
	 * and performing subtype tests, etc.
	 */
	private final TypeSystem typeSystem;

	/**
	 * The debug stream provides an I/O stream through which debug bytecodes can
	 * write their messages.
	 */
	private final PrintStream debug;
	
	private enum Status {
		RETURN,
		BREAK,
		CONTINUE,
		NEXT
	}
	
	/** A map from function name to a map of inputs to outputs */
	private final Map<FunctionOrMethod, Map<RValue[], RValue[]>> functionParameters;

	public QCInterpreter(Build.Project project, PrintStream debug) {
		super(project, debug);
		this.debug = debug;
		this.typeSystem = new TypeSystem(project);
		this.functionParameters = new HashMap<FunctionOrMethod, Map<RValue[], RValue[]>>();
	}	

	/**
	 * Overriden method
	 * Execute an Invoke bytecode instruction at a given point in the function
	 * or method body. This generates a recursive call to execute the given
	 * function. If the function does not exist, or is provided with the wrong
	 * number of arguments, then a runtime fault will occur.
	 * 
	 * The function/method call is optimised generating a verified, 
	 * random return value instead of calling the function/method.
	 *
	 * @param expr
	 *            --- The expression to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 * @throws ResolutionError
	 */
	private RValue[] executeInvoke(Expr.Invoke expr, CallStack frame) throws ResolutionError {
		// Resolve function or method being invoked to a concrete declaration
		Decl.Callable decl = typeSystem.resolveExactly(expr.getName(), expr.getSignature(),
				Decl.Callable.class);
		// Evaluate argument expressions
		RValue[] arguments = executeExpressions(expr.getOperands(), frame);
		Decl.FunctionOrMethod fun = ((Decl.FunctionOrMethod) decl);
		Map<RValue[], RValue[]> functionIO = functionParameters.getOrDefault(fun, new HashMap<RValue[], RValue[]>());
		// Every function should return the same output for the same input
		Tuple<Expr> postconditions = fun.getEnsures();
		Tuple<Decl.Variable> outputParameters = fun.getReturns();
		// Generate until the return type meets the postcondition
		// If it is unable to generate after a certain number of times,
		// just call the function/method instead 
		frame = frame.enter(fun);
		extractParameters(frame, arguments, fun);
		int numGeneration = 10;
		// TODO need the integer limits!
		GenerateTest testGen = new RandomGenerateTest(fun, this, numGeneration, BigInteger.valueOf(RunTest.INT_LOWER_LIMIT), BigInteger.valueOf(RunTest.INT_UPPER_LIMIT));
		RValue[] returns;
		boolean isValid = false;
		CallStack tempFrame = frame.clone();
		for(int i=0; i < numGeneration; i++) {
			// Create a generator for the return type of the function based on the input
			returns = testGen.generateParameters();
			try {
				for(int j=0; j < outputParameters.size(); j++) {
					Decl.Variable parameter = outputParameters.get(j);
					Type paramType = parameter.getType();
					isValid = RunTest.checkInvariant(this, paramType, returns[j]);
					if(!isValid) {
						break;
					}
					frame.putLocal(parameter.getName(), returns[j]);
				}	
				this.checkInvariants(frame, postconditions);
			}
			catch(AssertionError e) {
				isValid = false;
			}
			if(isValid) {
				functionIO.put(arguments, returns);
				return returns;
			}
			// Need to reset frame to remove the old inputs
			frame = tempFrame.clone();
		}
		// Invoke the function or method in question
		return execute(decl.getQualifiedName().toNameID(), decl.getType(), frame, arguments);
	}
	

	// =============================================================
	// Remainder is code from wyil.intepreter.Interpreter
	// =============================================================

	private void extractParameters(CallStack frame, RValue[] args, Decl.Callable decl) {
		Tuple<Decl.Variable> parameters = decl.getParameters();
		for(int i=0;i!=parameters.size();++i) {
			Decl.Variable parameter = parameters.get(i);
			frame.putLocal(parameter.getName(), args[i]);
		}
	}

	/**
	 * Given an execution frame, extract the return values from a given function
	 * or method. The parameters of the function or method are located first in
	 * the frame, followed by the return values.
	 *
	 * @param frame
	 * @param type
	 * @return
	 */
	private RValue[] packReturns(CallStack frame, Decl.Callable decl) {
		if (decl instanceof Decl.Property) {
			return new RValue[] { RValue.True };
		} else {
			Tuple<Decl.Variable> returns = decl.getReturns();
			RValue[] values = new RValue[returns.size()];
			for (int i = 0; i != values.length; ++i) {
				values[i] = frame.getLocal(returns.get(i).getName());
			}
			return values;
		}
	}

	/**
	 * Execute a given block of statements starting from the beginning. Control
	 * may terminate prematurely in a number of situations. For example, when a
	 * return or break statement is encountered.
	 *
	 * @param block
	 *            --- Statement block to execute
	 * @param frame
	 *            --- The current stack frame
	 *
	 * @return
	 */
	private Status executeBlock(Stmt.Block block, CallStack frame, EnclosingScope scope) {
		for (int i = 0; i != block.size(); ++i) {
			Stmt stmt = block.get(i);
			Status r = executeStatement(stmt, frame, scope);
			// Now, see whether we are continuing or not
			if (r != Status.NEXT) {
				return r;
			}
		}
		return Status.NEXT;
	}

	/**
	 * Execute a statement at a given point in the function or method body
	 *
	 * @param stmt
	 *            --- The statement to be executed
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeStatement(Stmt stmt, CallStack frame, EnclosingScope scope) {
		try {
			switch (stmt.getOpcode()) {
			case WhileyFile.STMT_assert:
				return executeAssert((Stmt.Assert) stmt, frame, scope);
			case WhileyFile.STMT_assume:
				return executeAssume((Stmt.Assume) stmt, frame, scope);
			case WhileyFile.STMT_assign:
				return executeAssign((Stmt.Assign) stmt, frame, scope);
			case WhileyFile.STMT_break:
				return executeBreak((Stmt.Break) stmt, frame, scope);
			case WhileyFile.STMT_continue:
				return executeContinue((Stmt.Continue) stmt, frame, scope);
			case WhileyFile.STMT_debug:
				return executeDebug((Stmt.Debug) stmt, frame, scope);
			case WhileyFile.STMT_dowhile:
				return executeDoWhile((Stmt.DoWhile) stmt, frame, scope);
			case WhileyFile.STMT_fail:
				return executeFail((Stmt.Fail) stmt, frame, scope);
			case WhileyFile.STMT_if:
			case WhileyFile.STMT_ifelse:
				return executeIf((Stmt.IfElse) stmt, frame, scope);
			case WhileyFile.EXPR_indirectinvoke:
				executeIndirectInvoke((Expr.IndirectInvoke) stmt, frame);
				return Status.NEXT;
			case WhileyFile.EXPR_invoke:
				executeInvoke((Expr.Invoke) stmt, frame);
				return Status.NEXT;
			case WhileyFile.STMT_namedblock:
				return executeNamedBlock((Stmt.NamedBlock) stmt, frame, scope);
			case WhileyFile.STMT_while:
				return executeWhile((Stmt.While) stmt, frame, scope);
			case WhileyFile.STMT_return:
				return executeReturn((Stmt.Return) stmt, frame, scope);
			case WhileyFile.STMT_skip:
				return executeSkip((Stmt.Skip) stmt, frame, scope);
			case WhileyFile.STMT_switch:
				return executeSwitch((Stmt.Switch) stmt, frame, scope);
			case WhileyFile.DECL_variableinitialiser:
			case WhileyFile.DECL_variable:
				return executeVariableDeclaration((Decl.Variable) stmt, frame);
			}
		}
		catch (ResolutionError e) {
			error(e.getMessage(), stmt);
			return null;
		}

		deadCode(stmt);
		return null; // deadcode
	}

	private Status executeAssign(Stmt.Assign stmt, CallStack frame, EnclosingScope scope) {
		// FIXME: handle multi-assignments properly
		Tuple<WhileyFile.LVal> lhs = stmt.getLeftHandSide();
		RValue[] rhs = executeExpressions(stmt.getRightHandSide(), frame);
		for (int i = 0; i != lhs.size(); ++i) {
			LValue lval = constructLVal(lhs.get(i), frame);
			lval.write(frame, rhs[i]);
		}
		return Status.NEXT;
	}

	/**
	 * Execute an assert or assume statement. In both cases, if the condition
	 * evaluates to false an exception is thrown.
	 *
	 * @param stmt
	 *            --- Assert statement.
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeAssert(Stmt.Assert stmt, CallStack frame, EnclosingScope scope) {
		//
		checkInvariants(frame,stmt.getCondition());
		return Status.NEXT;
	}

	/**
	 * Execute an assert or assume statement. In both cases, if the condition
	 * evaluates to false an exception is thrown.
	 *
	 * @param stmt
	 *            --- Assert statement.
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeAssume(Stmt.Assume stmt, CallStack frame, EnclosingScope scope) {
		//
		checkInvariants(frame,stmt.getCondition());
		return Status.NEXT;
	}

	/**
	 * Execute a break statement. This transfers to control out of the nearest
	 * enclosing loop.
	 *
	 * @param stmt
	 *            --- Break statement.
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeBreak(Stmt.Break stmt, CallStack frame, EnclosingScope scope) {
		// TODO: the break bytecode supports a non-nearest exit and eventually
		// this should be supported.
		return Status.BREAK;
	}

	/**
	 * Execute a continue statement. This transfers to control back to the start
	 * the nearest enclosing loop.
	 *
	 * @param stmt
	 *            --- Break statement.
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeContinue(Stmt.Continue stmt, CallStack frame, EnclosingScope scope) {
		return Status.CONTINUE;
	}

	/**
	 * Execute a Debug statement at a given point in the function or method
	 * body. This will write the provided string out to the debug stream.
	 *
	 * @param stmt
	 *            --- Debug statement to executed
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeDebug(Stmt.Debug stmt, CallStack frame, EnclosingScope scope) {
		//
		// FIXME: need to do something with this
		RValue.Array arr = executeExpression(ARRAY_T, stmt.getOperand(), frame);
		for (RValue item : arr.getElements()) {
			RValue.Int i = (RValue.Int) item;
			char c = (char) i.intValue();
			debug.print(c);
		}
		//
		return Status.NEXT;
	}

	/**
	 * Execute a DoWhile statement at a given point in the function or method
	 * body. This will loop over the body zero or more times.
	 *
	 * @param stmt
	 *            --- Loop statement to executed
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeDoWhile(Stmt.DoWhile stmt, CallStack frame, EnclosingScope scope) {
		Status r = Status.NEXT;
		while (r == Status.NEXT || r == Status.CONTINUE) {
			r = executeBlock(stmt.getBody(), frame, scope);
			if (r == Status.NEXT) {
				RValue.Bool operand = executeExpression(BOOL_T, stmt.getCondition(), frame);
				if (operand == RValue.False) {
					return Status.NEXT;
				}
			}
		}

		// If we get here, then we have exited the loop body without falling
		// through to the next bytecode.
		if (r == Status.BREAK) {
			return Status.NEXT;
		} else {
			return r;
		}
	}

	/**
	 * Execute a fail statement at a given point in the function or method body.
	 * This will generate a runtime fault.
	 *
	 * @param stmt
	 *            --- The fail statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeFail(Stmt.Fail stmt, CallStack frame, EnclosingScope scope) {
		throw new AssertionError("Runtime fault occurred");
	}

	/**
	 * Execute an if statement at a given point in the function or method body.
	 * This will proceed done either the true or false branch.
	 *
	 * @param stmt
	 *            --- The if statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeIf(Stmt.IfElse stmt, CallStack frame, EnclosingScope scope) {
		RValue.Bool operand = executeExpression(BOOL_T, stmt.getCondition(), frame);
		if (operand == RValue.True) {
			// branch taken, so execute true branch
			return executeBlock(stmt.getTrueBranch(), frame, scope);
		} else if (stmt.hasFalseBranch()) {
			// branch not taken, so execute false branch
			return executeBlock(stmt.getFalseBranch(), frame, scope);
		} else {
			return Status.NEXT;
		}
	}

	/**
	 * Execute a named block which is simply a block of statements.
	 *
	 * @param stmt
	 *            --- Block statement to executed
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeNamedBlock(Stmt.NamedBlock stmt, CallStack frame, EnclosingScope scope) {
		return executeBlock(stmt.getBlock(),frame,scope);
	}

	/**
	 * Execute a While statement at a given point in the function or method
	 * body. This will loop over the body zero or more times.
	 *
	 * @param stmt
	 *            --- Loop statement to executed
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeWhile(Stmt.While stmt, CallStack frame, EnclosingScope scope) {
		Status r;
		do {
			RValue.Bool operand = executeExpression(BOOL_T, stmt.getCondition(), frame);
			if (operand == RValue.False) {
				return Status.NEXT;
			}
			// Keep executing the loop body until we exit it somehow.
			r = executeBlock(stmt.getBody(), frame, scope);
		} while (r == Status.NEXT || r == Status.CONTINUE);
		// If we get here, then we have exited the loop body without falling
		// through to the next bytecode.
		if (r == Status.BREAK) {
			return Status.NEXT;
		} else {
			return r;
		}
	}

	/**
	 * Execute a Return statement at a given point in the function or method
	 * body
	 *
	 * @param stmt
	 *            --- The return statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeReturn(Stmt.Return stmt, CallStack frame, EnclosingScope scope) {
		// We know that a return statement can only appear in either a function
		// or method declaration. It cannot appear, for example, in a type
		// declaration. Therefore, the enclosing declaration is a function or
		// method.
		Decl.Callable context = scope.getEnclosingScope(FunctionOrMethodScope.class).getContext();
		Tuple<Decl.Variable> returns = context.getReturns();
		RValue[] values = executeExpressions(stmt.getReturns(), frame);
		for (int i = 0; i != returns.size(); ++i) {
			frame.putLocal(returns.get(i).getName(), values[i]);
		}
		return Status.RETURN;
	}

	/**
	 * Execute a skip statement at a given point in the function or method
	 * body
	 *
	 * @param stmt
	 *            --- The skip statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeSkip(Stmt.Skip stmt, CallStack frame, EnclosingScope scope) {
		// skip !
		return Status.NEXT;
	}

	/**
	 * Execute a Switch statement at a given point in the function or method
	 * body
	 *
	 * @param stmt
	 *            --- The swithc statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeSwitch(Stmt.Switch stmt, CallStack frame, EnclosingScope scope) {
		Tuple<Stmt.Case> cases = stmt.getCases();
		//
		Object value = executeExpression(ANY_T, stmt.getCondition(), frame);
		for (int i = 0; i != cases.size(); ++i) {
			Stmt.Case c = cases.get(i);
			Stmt.Block body = c.getBlock();
			if (c.isDefault()) {
				return executeBlock(body, frame, scope);
			} else {
				// FIXME: this is a temporary hack until a proper notion of
				// ConstantExpr is introduced.
				RValue[] values = executeExpressions(c.getConditions(), frame);
				for (RValue v : values) {
					if (v.equals(value)) {
						return executeBlock(body, frame, scope);
					}
				}
			}
		}
		return Status.NEXT;
	}

	/**
	 * Execute a variable declaration statement at a given point in the function or method
	 * body
	 *
	 * @param stmt
	 *            --- The statement to execute
	 * @param frame
	 *            --- The current stack frame
	 * @return
	 */
	private Status executeVariableDeclaration(Decl.Variable stmt, CallStack frame) {
		// We only need to do something if this has an initialiser
		if(stmt.hasInitialiser()) {
			RValue value = executeExpression(ANY_T, stmt.getInitialiser(), frame);
			frame.putLocal(stmt.getName(),value);
		}
		return Status.NEXT;
	}

	// =============================================================
	// Multiple expressions
	// =============================================================

	/**
	 * Execute one or more expressions. This is slightly more complex than for
	 * the single expression case because of the potential to encounter
	 * "positional operands". That is, severals which arise from executing the
	 * same expression.
	 *
	 * @param expressions
	 * @param frame
	 * @return
	 */
	private RValue[] executeExpressions(Tuple<Expr> expressions, CallStack frame) {
		RValue[][] results = new RValue[expressions.size()][];
		int count = 0;
		for(int i=0;i!=expressions.size();++i) {
			results[i] = executeMultiReturnExpression(expressions.get(i),frame);
			count += results[i].length;
		}
		RValue[] rs = new RValue[count];
		int j = 0;
		for(int i=0;i!=expressions.size();++i) {
			Object[] r = results[i];
			System.arraycopy(r, 0, rs, j, r.length);
			j += r.length;
		}
		return rs;
	}

	/**
	 * Execute an expression which has the potential to return more than one
	 * result. Thus the return type must accommodate this by allowing zero or
	 * more returned values.
	 *
	 * @param expr
	 * @param frame
	 * @return
	 */
	private RValue[] executeMultiReturnExpression(Expr expr, CallStack frame) {
		try {
			switch (expr.getOpcode()) {
			case WhileyFile.EXPR_indirectinvoke:
				return executeIndirectInvoke((Expr.IndirectInvoke) expr, frame);
			case WhileyFile.EXPR_invoke:
				return executeInvoke((Expr.Invoke) expr, frame);
			case WhileyFile.EXPR_constant:
			case WhileyFile.EXPR_cast:
			case WhileyFile.EXPR_recordaccess:
			case WhileyFile.EXPR_recordborrow:
			case WhileyFile.DECL_lambda:
			case WhileyFile.EXPR_logicalexistential:
			case WhileyFile.EXPR_logicaluniversal:
			default:
				RValue val = executeExpression(ANY_T, expr, frame);
				return new RValue[] { val };
			}
		} catch (ResolutionError e) {
			error(e.getMessage(), expr);
			return null;
		}
	}

	/**
	 * Execute an IndirectInvoke bytecode instruction at a given point in the
	 * function or method body. This first checks the operand is a function
	 * reference, and then generates a recursive call to execute the given
	 * function. If the function does not exist, or is provided with the wrong
	 * number of arguments, then a runtime fault will occur.
	 *
	 * @param expr
	 *            --- The expression to execute
	 * @param frame
	 *            --- The current stack frame
	 * @param context
	 *            --- Context in which bytecodes are executed
	 * @return
	 */
	private RValue[] executeIndirectInvoke(Expr.IndirectInvoke expr, CallStack frame) {
		RValue.Lambda src = executeExpression(LAMBDA_T, expr.getSource(),frame);
		RValue[] arguments = executeExpressions(expr.getArguments(), frame);
		// Here we have to use the enclosing frame when the lambda was created.
		// The reason for this is that the lambda may try to access enclosing
		// variables in the scope it was created.
		frame = src.getFrame();
		extractParameters(frame,arguments,src.getContext());
		// Execute the method or function body
		Stmt body = src.getBody();
		if(body instanceof Stmt.Block) {
			executeBlock((Stmt.Block) body, frame, new FunctionOrMethodScope(src.getContext()));
			// Extra the return values
			return packReturns(frame,src.getContext());
		} else {
			RValue retval = executeExpression(ANY_T,(Expr) body, frame);
			return new RValue[]{retval};
		}
	}

	

	// =============================================================
	// Constants
	// =============================================================

	/**
	 * This method constructs a "mutable" representation of the lval. This is a
	 * bit strange, but is necessary because values in the frame are currently
	 * immutable.
	 *
	 * @param operand
	 * @param frame
	 * @param context
	 * @return
	 */
	private LValue constructLVal(Expr expr, CallStack frame) {
		switch (expr.getOpcode()) {
		case EXPR_arrayborrow:
		case EXPR_arrayaccess: {
			Expr.ArrayAccess e = (Expr.ArrayAccess) expr;
			LValue src = constructLVal(e.getFirstOperand(), frame);
			RValue.Int index = executeExpression(INT_T, e.getSecondOperand(), frame);
			return new LValue.Array(src, index);
		}
		case EXPR_dereference: {
			Expr.Dereference e = (Expr.Dereference) expr;
			LValue src = constructLVal(e.getOperand(), frame);
			return new LValue.Dereference(src);
		}
		case EXPR_recordaccess:
		case EXPR_recordborrow: {
			Expr.RecordAccess e = (Expr.RecordAccess) expr;
			LValue src = constructLVal(e.getOperand(), frame);
			return new LValue.Record(src, e.getField());
		}
		case EXPR_variablemove:
		case EXPR_variablecopy: {
			Expr.VariableAccess e = (Expr.VariableAccess) expr;
			Decl.Variable decl = e.getVariableDeclaration();
			return new LValue.Variable(decl.getName());
		}
		}
		deadCode(expr);
		return null; // deadcode
	}

	/**
	 * This method is provided to properly handled positions which should be
	 * dead code.
	 *
	 * @param context
	 *            --- Context in which bytecodes are executed
	 */
	private <T> T deadCode(SyntacticElement element) {
		// FIXME: do more here
		throw new RuntimeException("internal failure --- dead code reached");
	}

	private static final Class<RValue> ANY_T = RValue.class;
	private static final Class<RValue.Bool> BOOL_T = RValue.Bool.class;
	private static final Class<RValue.Int> INT_T = RValue.Int.class;
	private static final Class<RValue.Array> ARRAY_T = RValue.Array.class;
	private static final Class<RValue.Lambda> LAMBDA_T = RValue.Lambda.class;

	/**
	 * An enclosing scope captures the nested of declarations, blocks and other
	 * staments (e.g. loops). It is used to store information associated with
	 * these things such they can be accessed further down the chain. It can
	 * also be used to propagate information up the chain (for example, the
	 * environments arising from a break or continue statement).
	 *
	 * @author David J. Pearce
	 *
	 */
	private abstract static class EnclosingScope {
		private final EnclosingScope parent;

		public EnclosingScope(EnclosingScope parent) {
			this.parent = parent;
		}

		/**
		 * Get the innermost enclosing block of a given kind. For example, when
		 * processing a return statement we may wish to get the enclosing
		 * function or method declaration such that we can type check the return
		 * types.
		 *
		 * @param kind
		 */
		@SuppressWarnings("unchecked")
		public <T extends EnclosingScope> T getEnclosingScope(Class<T> kind) {
			if (kind.isInstance(this)) {
				return (T) this;
			} else if (parent != null) {
				return parent.getEnclosingScope(kind);
			} else {
				// FIXME: better error propagation?
				return null;
			}
		}
	}

	/**
	 * Represents the enclosing scope for a function or method declaration.
	 *
	 * @author David J. Pearce
	 *
	 */
	private static class FunctionOrMethodScope extends EnclosingScope {
		private final Decl.Callable context;;

		public FunctionOrMethodScope(Decl.Callable context) {
			super(null);
			this.context = context;
		}

		public Decl.Callable getContext() {
			return context;
		}
	}
}
