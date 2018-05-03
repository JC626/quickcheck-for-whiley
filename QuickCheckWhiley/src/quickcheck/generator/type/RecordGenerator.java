package quickcheck.generator.type;

import java.util.List;

import quickcheck.util.TestType;
import wybs.util.AbstractCompilationUnit.Identifier;
import wyil.interpreter.ConcreteSemantics;
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
	private List<Identifier> fieldNames;
	/** Current field elements generated */
	private Field[] elements;
	
	private TestType testType;
	
	private int size;
	private int count = 1;
	
	public RecordGenerator(List<Generator> generators, List<Identifier> names, TestType testType) {
		this.generators = generators;
		this.fieldNames = names;
		this.testType = testType;
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
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			if(elements == null) {
				elements = new Field[generators.size()];
				for(int i=0; i < elements.length; i++) {
					Generator gen = generators.get(i);
					gen.resetCount();
					elements[i] = semantics.Field(fieldNames.get(i), gen.generate());
				}
			}
			else {
				// Generate the array elements backwards 
				for(int i=elements.length - 1; i >= 0 ; i--) {
					Generator gen = generators.get(i);
					if(!gen.exceedCount()) {
						elements[i] = semantics.Field(fieldNames.get(i), gen.generate());
						break;
					}
					else {
						gen.resetCount();
						elements[i] = semantics.Field(fieldNames.get(i), gen.generate());
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
				recordFields[i] = semantics.Field(fieldNames.get(i), gen.generate());
			}
			count++;
			// Need to clone (shallow is fine) so the elements array doesn't get sorted
			return semantics.Record(recordFields.clone());
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
		result = prime * result + ((fieldNames == null) ? 0 : fieldNames.hashCode());
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
		if (fieldNames == null) {
			if (other.fieldNames != null)
				return false;
		} else if (!fieldNames.equals(other.fieldNames))
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
