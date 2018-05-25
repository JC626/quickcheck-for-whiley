package quickcheck.generator.type;

import java.util.List;

import quickcheck.constraints.RangeHelper;
import quickcheck.util.TestType;
import wybs.util.AbstractCompilationUnit.Identifier;
import wybs.util.AbstractCompilationUnit.Tuple;
import wyc.lang.WhileyFile.Decl;
import wyc.lang.WhileyFile.Expr;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.Interpreter;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Field;

/**
 * Generates record values.
 * Since a record is made up of a number of fields of varying types,
 * multiple generators of varying types are required
 * to correspond to each field.
 *
 * e.g. type Point {int x, int y}
 * would require two IntegerGenerators
 * and could return a possible value of {x: 10, y: 6}.
 *
 * @author Janice Chin
 *
 */
public class RecordGenerator implements Generator{
	/** Used for generating appropriate values */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();

	/** Generators corresponding to each field */
	private List<Generator> generators;
	/** Field names for the record */
	private List<Decl.Variable> fields;

	/** Current field elements generated */
	private Field[] elements;

	private TestType testType;

	private int size;
	private int count = 1;

	public RecordGenerator(List<Generator> generators, List<Decl.Variable> fields, TestType testType) {
		this.generators = generators;
		this.fields = fields;
		this.testType = testType;
		calculateSize();
	}

	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			if(elements == null) {
				elements = new Field[generators.size()];
				for(int i=0; i < elements.length; i++) {
					Generator gen = generators.get(i);
					gen.resetCount();
					elements[i] = semantics.Field(fields.get(i).getName(), gen.generate());
				}
			}
			else {
				// Generate the array elements backwards
				for(int i=elements.length - 1; i >= 0 ; i--) {
					Generator gen = generators.get(i);
					if(!gen.exceedCount()) {
						elements[i] = semantics.Field(fields.get(i).getName(), gen.generate());
						break;
					}
					else {
						gen.resetCount();
						elements[i] = semantics.Field(fields.get(i).getName(), gen.generate());
					}
				}
			}
			count++;
			// Need to clone (shallow is fine) so the elements array doesn't get sorted
			return semantics.Record(elements.clone());
		}
		else {
			Field[] recordFields = new Field[generators.size()];
			for(int i=0; i < recordFields.length; i++) {
				Generator gen = generators.get(i);
				recordFields[i] = semantics.Field(fields.get(i).getName(), gen.generate());
			}
			count++;
			// Need to clone (shallow is fine) so the elements array doesn't get sorted
			return semantics.Record(recordFields.clone());
		}
	}

	/**
	 * Check the ranges on the invariants against the generators.
	 * @param invariants The invariants to check against the generator on the nominal type
	 * @param interpreter The interpreter used
	 */
	public void checkInvariantRange(Tuple<Expr> invariants, Interpreter interpreter, String prefix) {
		// Can have multiple invariants
		if (invariants.size() > 0 && !fields.isEmpty()) {
			assert fields.size() == generators.size();
			for(int i=0; i < fields.size(); i++) {
				Generator gen = generators.get(i);
				if(gen instanceof ArrayGenerator || gen instanceof IntegerGenerator) {
					String name = prefix + fields.get(i).getName().get();
					RangeHelper.checkInvariantRange(generators.get(i), new Identifier(name), invariants, interpreter);
				}
				else if(gen instanceof NominalGenerator) {
					NominalGenerator nomGen = (NominalGenerator) gen;
					String name = prefix + fields.get(i).getName().get();
					nomGen.checkInvariantRange(invariants, new Identifier(name));
				}
				else if(gen instanceof RecordGenerator) {
					RecordGenerator recordGen = (RecordGenerator) gen;
					prefix = prefix + ".";
					System.out.println("Prefix within record " + prefix);
					recordGen.checkInvariantRange(invariants, interpreter, prefix);
				}
				else if(gen instanceof UnionGenerator) {
					UnionGenerator unionGen = (UnionGenerator) gen;
					prefix = prefix + ".";
					unionGen.checkInvariantRange(invariants, interpreter, prefix);
				}
			}
			calculateSize();
		}
	}

	public void calculateSize() {
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
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void resetCount() {
		count = 1;
		this.elements = null;
	}

	@Override
	public boolean exceedCount() {
		return this.size() < count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((generators == null) ? 0 : generators.hashCode());
		result = prime * result + size;
		result = prime * result + ((testType == null) ? 0 : testType.hashCode());
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
		RecordGenerator other = (RecordGenerator) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (generators == null) {
			if (other.generators != null)
				return false;
		} else if (!generators.equals(other.generators))
			return false;
		if (size != other.size)
			return false;
		if (testType != other.testType)
			return false;
		return true;
	}


}
