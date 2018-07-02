package quickcheck.constraints;

import java.math.BigInteger;

import quickcheck.exception.IntegerRangeException;
import quickcheck.generator.type.ArrayGenerator;
import quickcheck.generator.type.Generator;
import quickcheck.generator.type.IntegerGenerator;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Expr;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Int;
import wyil.interpreter.Interpreter.CallStack;

/**
 * Helper method for calculating ranges and invariants
 * for the type generators.
 *
 * @author Janice Chin
 *
 */
public class RangeHelper {
	
	public static final String NULL_INVARIANT_VAR_ERROR = "null operand";

	/**
	 * Check the ranges on the invariants against the generators.
	 * This requires physically evaluating the invariant to see whether
	 * or not it can be applied to the generators.
	 *
	 * @param name The name of the field the invariant should apply to
	 * @param invariant The invariants applied on the nominal type
	 * @param instance The interpreter
	 * @throws IntegerRangeException 
	 */
	public static void checkInvariantRange(Generator gen, Identifier name, Tuple<Expr> invariant, Interpreter instance) throws IntegerRangeException {
		// Can have multiple invariants (such as multiple where clauses for a nominal type)
		if (invariant.size() > 0) {
			/*
			 * One or more type invariants to check.
			 * Therefore, we need to execute the invariant and
			 * determine whether or not it returns true.
			 */
			Interpreter.CallStack frame = instance.new CallStack();
			for (int i = 0; i != invariant.size(); i++) {
				IntegerRange b = RangeHelper.findRange(invariant.get(i), name, frame, instance);
				if (b != null) {
					if(gen instanceof IntegerGenerator) {
						((IntegerGenerator) gen).joinRange(b);
					}
					else if(gen instanceof ArrayGenerator) {
						((ArrayGenerator) gen).joinRange(b);
					}
				}
			}
		}
	}


	/**
	 * Find the integer range for a given invariant
	 * by executing expressions in the invariant.
	 *
	 * @param expr - The expression to be executed
	 * @param name - The name of the field the invariant is applied to
	 * @param frame - The frame in which the expression is executing
	 * @param instance - The interpreter in which the expressions are executed
	 * @return The IntegerRange discovered from the invariant
	 */
	public static IntegerRange findRange(Expr expr, Identifier name, CallStack frame, Interpreter instance) {
		IntegerRange range = null;
		try {
			int operator = expr.getOpcode();
			switch (operator) {
			case WhileyFile.EXPR_equal:
				Expr.BinaryOperator binaryEq = (Expr.BinaryOperator) expr;
				Expr firstEq = binaryEq.getFirstOperand();
				Expr secondEq = binaryEq.getSecondOperand();
	
				if(isExpForIntegerRange(firstEq, name)){
					RValue rhs = instance.executeExpression(RValue.class, secondEq, frame);
					if(rhs instanceof RValue.Int) {
						RValue.Int val = (Int) rhs;
						return new IntegerRange(val.intValue(), val.intValue() + 1);
					}
				}
				else if(isExpForIntegerRange(secondEq, name)){
					RValue lhs = instance.executeExpression(RValue.class, firstEq, frame);
					if(lhs instanceof RValue.Int) {
						RValue.Int val = (Int) lhs;
						return new IntegerRange(val.intValue(), val.intValue() + 1);
					}
				}
				// normal expression
				break;
			case WhileyFile.EXPR_logicalnot:
				Expr.UnaryOperator unary = (Expr.UnaryOperator) expr;
				range = findRange(unary.getOperand(), name, frame, instance);
				// Flip the ranges around
				if(range != null) {
					BigInteger upper = range.upperBound() == null ? null : range.upperBound();
					BigInteger lower = range.lowerBound() == null ? null : range.lowerBound();
					return new IntegerRange(upper, lower);
				}
				break;
			case WhileyFile.EXPR_logicalor:
			case WhileyFile.EXPR_logicaland:
				Expr.NaryOperator nary = (Expr.NaryOperator) expr;
				Tuple<Expr> operands = nary.getOperands();
				for(int i=0;i!=operands.size();++i) {
					IntegerRange other = findRange(operands.get(i), name, frame, instance);
					if(range == null) {
						range = other;
					}
					else if(other != null) {
						if(operator == WhileyFile.EXPR_logicalor) {
							range = range.union(other);
						}
						else {
							range = range.intersection(other);
						}
					}
				}
				return range;
			case WhileyFile.EXPR_integerlessthan:
			case WhileyFile.EXPR_integerlessequal:
			case WhileyFile.EXPR_integergreaterthan:
			case WhileyFile.EXPR_integergreaterequal:
				Expr.BinaryOperator binary = (Expr.BinaryOperator) expr;
				Expr first = binary.getFirstOperand();
				Expr second = binary.getSecondOperand();
	
				BigInteger upperLimit = null;
				BigInteger lowerLimit = null;
				/* TODO Would fail for x + 2 < 10 as
				 * the lhs is not checked for the extra + 2
				 * To make the range x < 8 , we want to flip all the
				 * operations applied to the x to the other side
				 * i.e. x + 2 < 10 ==> x < 10 - 2
				 */
				if(isExpForIntegerRange(first, name)){
					RValue.Int rhs = instance.executeExpression(RValue.Int.class, second, frame);
					if(operator == WhileyFile.EXPR_integerlessthan) {
						upperLimit = BigInteger.valueOf(rhs.intValue());
					}
					else if(operator == WhileyFile.EXPR_integerlessequal) {
						upperLimit = BigInteger.valueOf(rhs.intValue() + 1);
					}
					else if(operator == WhileyFile.EXPR_integergreaterthan) {
						lowerLimit = BigInteger.valueOf(rhs.intValue() + 1);
					}
					else if(operator == WhileyFile.EXPR_integergreaterequal) {
						lowerLimit = BigInteger.valueOf(rhs.intValue());
					}
					return new IntegerRange(lowerLimit, upperLimit);
				}
				else if(isExpForIntegerRange(second, name)){
					RValue.Int lhs = instance.executeExpression(RValue.Int.class, first, frame);
					if(operator == WhileyFile.EXPR_integerlessthan) {
						lowerLimit = BigInteger.valueOf(lhs.intValue() + 1);
					}
					else if(operator == WhileyFile.EXPR_integerlessequal) {
						lowerLimit = BigInteger.valueOf(lhs.intValue());
					}
					else if(operator == WhileyFile.EXPR_integergreaterthan) {
						upperLimit = BigInteger.valueOf(lhs.intValue());
					}
					else if(operator == WhileyFile.EXPR_integergreaterequal) {
						upperLimit = BigInteger.valueOf(lhs.intValue() + 1);
					}
					return new IntegerRange(lowerLimit, upperLimit);
				}
				// normal expression
				break;
			default:
				return null;
			}
		}
		// FIXME another way to check this would be better!
		catch(RuntimeException e) {
			if(e.getMessage().equals(NULL_INVARIANT_VAR_ERROR)) {
				return null;
			}
			throw e;
		}
		return null;
	}

	/**
	 * Checks if the variable's name is contained, solely in the expression.
	 * @param exp The expression to check if the name is contained in it
	 * @param name The name of the variable to check
	 * @return Whether the variable's name is in the expression
	 */
	private static boolean isExpForIntegerRange(Expr exp, Identifier name) {
		if(exp instanceof Expr.ArrayLength) {
			Expr array = ((Expr.ArrayLength) exp).getOperand();
			return isExpForIntegerRange(array, name);
		}
		return (exp instanceof Expr.RecordAccess && ((Expr.RecordAccess) exp).toString().equals(name.get())) ||
				(exp instanceof Expr.VariableAccess && ((Expr.VariableAccess) exp).getVariableDeclaration().getName().equals(name));
	}
}
