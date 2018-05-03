package quickcheck.generator.type;

import wyc.lang.WhileyFile.Decl;
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
	}

	@Override
	public RValue generate() {
		RValue.Bool isValid = RValue.Bool.False;
		int i = 1;
		RValue value = null;
		while(isValid == RValue.Bool.False) {
			value = generator.generate();
			isValid = value.checkInvariant(decl.getVariableDeclaration(), decl.getInvariant(), interpreter);
			i++;
			// No valid values
			if(i >= generator.size()) {
				// TODO Change this to a different exception
				throw new Error("No possible values can be generated for the nominal type: " + decl.getName());
			}
		}
		return value;
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

}
