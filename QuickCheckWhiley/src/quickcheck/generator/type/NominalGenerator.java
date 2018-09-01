package quickcheck.generator.type;

import quickcheck.constraints.RangeHelper;
import quickcheck.exception.CannotGenerateException;
import quickcheck.exception.IntegerRangeException;
import wybs.util.AbstractCompilationUnit.Identifier;
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
	/**The next value that was generated*/
	private RValue nextValue;
	/**Used to check if the generators were resetted internally, due to failing invariants*/
	private boolean internalReset;

	public NominalGenerator(Generator generator, Interpreter interpreter, Decl.Type decl) throws IntegerRangeException {
		super();
		this.generator = generator;
		this.interpreter = interpreter;
		this.decl = decl;

		if(decl.getInvariant().size() > 0) {
			checkInvariantRange(decl.getInvariant(), decl.getVariableDeclaration().getName());
		}

	}

	@Override
	public RValue generate() {
		RValue value = this.nextValue;
		if(value == null) {
			value = generateNext();
		}
		this.nextValue = null;
		return value;
	}

	@Override
	public RValue generate(int comboNum) {
		return generator.generate(comboNum);
	}

	/**
	 * Check the ranges on the invariants against the generators.
	 * @param invariants The invariants to check against the generator on the nominal type
	 * @param name The name of the variable to check the invariant ranges
	 * @throws IntegerRangeException 
	 */
	void checkInvariantRange(Tuple<Expr> invariants, Identifier name) throws IntegerRangeException {
		if(generator instanceof IntegerGenerator) {
			RangeHelper.checkInvariantRange(generator, name, invariants, interpreter);
		}
		else if(generator instanceof RecordGenerator) {
			RecordGenerator recordGen = (RecordGenerator) generator;
			String prefix = name.get() + ".";
			recordGen.checkInvariantRange(invariants, interpreter, prefix);
		}
		else if(generator instanceof ArrayGenerator) {
			RangeHelper.checkInvariantRange(generator, name, invariants, interpreter);
		}
		else if(generator instanceof NominalGenerator) {
			NominalGenerator nomGen = (NominalGenerator) generator;
			nomGen.checkInvariantRange(invariants, name);
		}
		else if(generator instanceof UnionGenerator) {
			UnionGenerator unionGen = (UnionGenerator) generator;
			unionGen.checkInvariantRange(invariants, interpreter, name.get());
		}
	}
	
	/**
	 * Internally generate the next value possible.
	 * @return
	 * @throws CannotGenerateException 
	 */
	private RValue generateNext() {
		RValue.Bool isValid = RValue.Bool.False;
		int i = 1;
		RValue value = null;
		while(isValid == RValue.Bool.False) {
            // TODO might be a good idea to redefine the size of the nominal?
			// When the generator's limit has reached, reset the generator
			if(generator.exceedCount()) {
				internalReset = true;
				generator.resetCount();
			}
			value = generator.generate();
			// If an assertion error is thrown for the value, then invariant failed. Skip the value
			try {
				isValid = value.checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
			}
			catch(AssertionError e) {}
			// No valid values
			if(i > generator.size()) {
				throw new CannotGenerateException("No possible values can be generated for the nominal type: " + decl.getName());
			}
			i++;
		}
		return value;
	}

	@Override
	public int size() {
		return generator.size();
	}

	@Override
	public void resetCount() {
		if(!internalReset) {
			nextValue = null;
			generator.resetCount();
		}
		internalReset = false;
	}

	@Override
	public boolean exceedCount() {
		if(nextValue == null) {
			// Check if the generator has exceeded the count
			if(generator.exceedCount()) {
				return true;
			}
			// Generate next value to check if it will exceed the count
			this.nextValue = generateNext();
		}
		// If the count was resetted internally, return true.
		return internalReset;
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
