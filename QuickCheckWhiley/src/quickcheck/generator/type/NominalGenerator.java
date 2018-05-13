package quickcheck.generator.type;

import quickcheck.constraints.RangeHelper;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
import wyil.interpreter.ConcreteSemantics.RValue;
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
			checkInvariantRange(decl.getInvariant());
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
	 * Check the ranges on the invariants against the generators.
	 * @param invariants The invariants to check against the generator on the nominal type
	 */
	private void checkInvariantRange(Tuple<Expr> invariants) {
		if(generator instanceof IntegerGenerator) {
			RangeHelper.checkInvariantRange(generator, decl.getVariableDeclaration().getName(), invariants, interpreter);
		}
		else if(generator instanceof RecordGenerator) {
			RecordGenerator recordGen = (RecordGenerator) generator;
			recordGen.checkInvariantRange(invariants, interpreter);
		}
		else if(generator instanceof ArrayGenerator) {
			RangeHelper.checkInvariantRange(generator, decl.getVariableDeclaration().getName(), decl.getInvariant(), interpreter);
		}
		// TODO if nominal type, need to pass invariant down?
		// then each generator needs to know it's name (within the nominal?)
		
		// TODO array as well (limit array size?)
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
