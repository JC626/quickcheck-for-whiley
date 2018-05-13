package quickcheck.generator.type;

import java.math.BigInteger;
import java.util.List;

import quickcheck.constraints.IntegerRange;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Int;
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
		
		if(decl.getInvariant().size() > 0) {
			if(generator instanceof IntegerGenerator) {
				 checkInvariant(generator, decl.getVariableDeclaration().getName(), decl.getInvariant(), interpreter);
			}
			else if(generator instanceof RecordGenerator) {
				RecordGenerator recordGen = (RecordGenerator) generator;
				List<Decl.Variable> fields = recordGen.getIntegerFields();
				List<IntegerGenerator> intGens = recordGen.getIntegerGenerators();
				if(!fields.isEmpty()) {
					assert fields.size() == intGens.size();
					// Apply invariant
					for(int i=0; i < fields.size(); i++) {
						checkInvariant(intGens.get(i), fields.get(i).getName(), decl.getInvariant(), interpreter);
					}
					recordGen.calculateSize();
				}
			}
			else if(generator instanceof ArrayGenerator) {
				 checkInvariant(generator, decl.getVariableDeclaration().getName(), decl.getInvariant(), interpreter);
			}
			// TODO if nominal type, need to pass invariant down?
			// then each generator needs to know it's name (within the nominal?)
			
			// TODO array as well (limit array size?)
		}

	}

	@Override
	public RValue generate() {
		RValue.Bool isValid = RValue.Bool.False;
		int i = 1;
		RValue value = null;
		while(isValid == RValue.Bool.False) {
			if (generator.exceedCount()) {
				generator.resetCount();
			}
			value = generator.generate();
			isValid = value.checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
			// No valid values
			if(i > generator.size()) {
				// TODO Change this to a different exception
				throw new Error("No possible values can be generated for the nominal type: " + decl.getName());
			}
			i++;
		}
		return value;
	}
	
	/**
	 * Check whether the invariant for a given nominal type holds for this value
	 * or not. This requires physically evaluating the invariant to see whether
	 * or not it holds true.
	 *
	 * @param nomName The name of the field the invariant should apply to
	 * @param invariant The invariants applied on the nominal type
	 * @param instance The interpreter
	 */
	private void checkInvariant(Generator gen, Identifier nomName, Tuple<Expr> invariant, Interpreter instance) {
		// Can have multiple invariants
		if (invariant.size() > 0) {
			/*
			 * One or more type invariants to check. 
			 * Therefore, we need to execute the invariant and
			 * determine whether or not it returns true.
			 */
			Interpreter.CallStack frame = instance.new CallStack();
//			frame.putLocal(var.getName(), val);
			for (int i = 0; i != invariant.size(); ++i) {
				IntegerRange b = findRange(invariant.get(i), nomName, frame, interpreter);
				if (b != null) {
					if(gen instanceof IntegerGenerator) {
						((IntegerGenerator) gen).joinRange(b);
					}
					if(gen instanceof ArrayGenerator) {
						((ArrayGenerator) gen).joinRange(b);
					}
				}
			}
		}
	}


	/**
	 * 
	 * Find the integer range for a given invariant 
	 * by executing expressions in the invariant.
	 * 
	 * @param expr - The expression to be executed
	 * @param nomName - The name of the field the invariant is applied to
	 * @param frame - The frame in which the expression is executing
	 * @param instance - The interpreter in which the expressions are executed
	 * @return The IntegerRange discovered from the invariant
	 */
	private IntegerRange findRange(Expr expr, Identifier nomName, CallStack frame, Interpreter instance) {
//		RValue val;
		IntegerRange range = null;
		int operator = expr.getOpcode();
		switch (operator) {
		case WhileyFile.EXPR_equal:
			Expr.BinaryOperator binaryEq = (Expr.BinaryOperator) expr;
			Expr firstEq = binaryEq.getFirstOperand();
			Expr secondEq = binaryEq.getSecondOperand();
			
			if(isExpForIntegerRange(firstEq, nomName)){
				RValue rhs = instance.executeExpression(RValue.class, secondEq, frame);
				if(rhs instanceof RValue.Int) {
					RValue.Int val = (Int) rhs;
					return new IntegerRange(val.intValue(), val.intValue() + 1);
				}
			}
			else if(isExpForIntegerRange(secondEq, nomName)){
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
			range = findRange(unary.getOperand(), nomName, frame, instance);
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
				IntegerRange other = findRange(operands.get(i), nomName, frame, instance);
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
			if(isExpForIntegerRange(first, nomName)){
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
			else if(isExpForIntegerRange(second, nomName)){
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
		return null;
	}
	
	/**
	 * Checks if the variable's name is contained, solely in the expression.
	 * @param exp The expression to check if the name is contained in it
	 * @param name The name of the variable to check
	 * @return Whether the variable's name is in the expression
	 */
	private boolean isExpForIntegerRange(Expr exp, Identifier name) {
		if(exp instanceof Expr.ArrayLength) {
			Expr array = ((Expr.ArrayLength) exp).getOperand();
			return array instanceof Expr.VariableAccess && ((Expr.VariableAccess) array).getVariableDeclaration().getName().equals(name);
		}
		return (exp instanceof Expr.RecordAccess && ((Expr.RecordAccess) exp).getField().equals(name)) || 
				(exp instanceof Expr.VariableAccess && ((Expr.VariableAccess) exp).getVariableDeclaration().getName().equals(name));
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
