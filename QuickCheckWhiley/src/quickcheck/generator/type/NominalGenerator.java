package quickcheck.generator.type;

import java.math.BigInteger;

import quickcheck.constraints.IntegerRange;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
import wyc.lang.WhileyFile.Expr.VariableAccess;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.Interpreter.CallStack;
import wyil.interpreter.Interpreter;

/**
 * Generate values for a nominal type.
 * Since a nominal type renames an existing type, 
 * it takes another generator to generate values 
 * for the existing type.
 * 
 * e.g. type nat is int where x > 0
 * Would require an IntegerGenerator 
 * and could return a value of 20.
 * 
 * @author Janice Chin
 *
 */
public class NominalGenerator implements Generator{
	/**Generator for the type that is renamed.*/
	private Generator generator;
	private Interpreter interpreter;
	private Decl.Type decl;
	
	public NominalGenerator(Generator generator, Interpreter interpreter, Decl.Type decl) {
		super();
		this.generator = generator;
		this.interpreter = interpreter;
		this.decl = decl;
		
		// TODO need to know the field/type
		// If record, need to know the record's field names
		
		// When nominal type wraps an integer
		if(generator instanceof IntegerGenerator) {
			 checkInvariant(generator, decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
		}

	}

	@Override
	public RValue generate() {
		// TODO after ranges generated, need to be able to pass it into the integer generator?
		// TODO remove invariants that have already been applied as ranges?
		if(!(generator instanceof IntegerGenerator)) {
			RValue.Bool isValid = RValue.Bool.False;
			int i = 1;
			RValue value = null;
			while(isValid == RValue.Bool.False) {
				value = generator.generate();
				isValid = value.checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
				i++;
				// No valid values
				if(i > generator.size()) {
					// TODO Change this to a different exception
					throw new Error("No possible values can be generated for the nominal type: " + decl.getName());
				}
			}
			return value;
		}
		return generator.generate();
	}
	
	/**
	 * Check whether the invariant for a given nominal type holds for this value
	 * or not. This requires physically evaluating the invariant to see whether
	 * or not it holds true.
	 *
	 * @param var
	 * @param invariant
	 * @param instance
	 * @return
	 */
	public void checkInvariant(Generator gen, Decl.Variable var, Tuple<Expr> invariant, Interpreter instance) {
		// TODO might need dummy value for the nominal type to be able to check invariant.
		// TODO Only one invariant?
		if (invariant.size() > 0) {
			// One or more type invariants to check. Therefore, we need
			// to execute the invariant and determine whether or not it
			// returns true.
			Interpreter.CallStack frame = instance.new CallStack();
//			frame.putLocal(var.getName(), val);
			for (int i = 0; i != invariant.size(); ++i) {
				IntegerRange b = discoverRanges(invariant.get(i), var.getName(), frame, interpreter);
				if (b != null) {
					if(gen instanceof IntegerGenerator) {
						((IntegerGenerator) gen).joinRange(b);
					}
				}
			}
		}
	}

	// TODO create own version of executeExpression for the integer ranges
	/**
	 * Execute a single expression which is expected to return a single result
	 * of an expected type. If a result of an incorrect type is returned, then
	 * an exception is raised.
	 *
	 * @param expr
	 *            The expression to be executed
	 * @param frame
	 *            The frame in which the expression is executing
	 * @return
	 */
	public IntegerRange discoverRanges(Expr expr, Identifier nomName, CallStack frame, Interpreter instance) {
		// TODO check order of operations for equal and not equal
		RValue val;
		switch (expr.getOpcode()) {
		case WhileyFile.EXPR_equal:
		case WhileyFile.EXPR_notequal:
			// TODO check type of values on both sides
//				val = executeNotEqual((Expr.NotEqual) expr, frame);
			break;
		case WhileyFile.EXPR_integerlessthan:
		case WhileyFile.EXPR_integerlessequal:
		case WhileyFile.EXPR_integergreaterthan:
		case WhileyFile.EXPR_integergreaterequal:
			Expr.BinaryOperator binary = (Expr.BinaryOperator) expr;
			Expr first = binary.getFirstOperand();
			Expr second = binary.getSecondOperand();
			
			// TODO check if field is an integer?
			BigInteger upperLimit = null;
			BigInteger lowerLimit = null;
			/* TODO Would fail for x + 2 < 10 as 
			 * the lhs is not checked for the extra + 2
			 * To make the range x < 8 , we want to flip all the 
			 * operations applied to the x to the other side
			 * i.e. x + 2 < 10 ==> x < 10 - 2
			 */
			// TODO need to know which side the variable is on.
			if(first instanceof VariableAccess && ((VariableAccess) first).getVariableDeclaration().getName().equals(nomName)){
				RValue.Int rhs = instance.executeExpression(RValue.Int.class, second, frame);
				int op = expr.getOpcode();			
				if(op == WhileyFile.EXPR_integerlessthan) {
					upperLimit = BigInteger.valueOf(rhs.intValue());
				}
				else if(op == WhileyFile.EXPR_integerlessequal) {
					upperLimit = BigInteger.valueOf(rhs.intValue() + 1);
				}
				else if(op == WhileyFile.EXPR_integergreaterthan) {
					lowerLimit = BigInteger.valueOf(rhs.intValue() + 1);
				}
				else if(op == WhileyFile.EXPR_integergreaterequal) {
					lowerLimit = BigInteger.valueOf(rhs.intValue());
				}	
				return new IntegerRange(lowerLimit, upperLimit);
			}
			else if(second instanceof VariableAccess && ((VariableAccess) second).getVariableDeclaration().getName().equals(nomName)){
				RValue.Int lhs = instance.executeExpression(RValue.Int.class, first, frame);
				int op = expr.getOpcode();
				if(op == WhileyFile.EXPR_integerlessthan) {
					lowerLimit = BigInteger.valueOf(lhs.intValue() + 1);
				}
				else if(op == WhileyFile.EXPR_integerlessequal) {
					lowerLimit = BigInteger.valueOf(lhs.intValue());
				}
				else if(op == WhileyFile.EXPR_integergreaterthan) {
					upperLimit = BigInteger.valueOf(lhs.intValue());
				}
				else if(op == WhileyFile.EXPR_integergreaterequal) {
					upperLimit = BigInteger.valueOf(lhs.intValue() + 1);
				}
				return new IntegerRange(lowerLimit, upperLimit);
			}
			else {
				// normal expression
				return null;
			}
//			case WhileyFile.EXPR_bitwisenot:
//				val = executeBitwiseNot((Expr.BitwiseComplement) expr, frame);
//				break;
//			case WhileyFile.EXPR_bitwiseor:
//				val = executeBitwiseOr((Expr.BitwiseOr) expr, frame);
//				break;
//			case WhileyFile.EXPR_bitwisexor:
//				val = executeBitwiseXor((Expr.BitwiseXor) expr, frame);
//				break;
//			case WhileyFile.EXPR_bitwiseand:
//				val = executeBitwiseAnd((Expr.BitwiseAnd) expr, frame);
//				break;
//			case WhileyFile.EXPR_bitwiseshl:
//				val = executeBitwiseShiftLeft((Expr.BitwiseShiftLeft) expr, frame);
//				break;
//			case WhileyFile.EXPR_bitwiseshr:
//				val = executeBitwiseShiftRight((Expr.BitwiseShiftRight) expr, frame);
//				break;
		default:
			// TODO Do we really care about the other invariants?
			return null;
		}
		return null;
	}
	
	@Override
	public int size() {
		return generator.size();
	}

	@Override
	public void resetCount() {
		generator.resetCount();
	}

	@Override
	public boolean exceedCount() {
		return generator.exceedCount();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decl == null) ? 0 : decl.hashCode());
		result = prime * result + ((generator == null) ? 0 : generator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NominalGenerator other = (NominalGenerator) obj;
		if (decl == null) {
			if (other.decl != null)
				return false;
		} else if (!decl.equals(other.decl))
			return false;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		return true;
	}

}
