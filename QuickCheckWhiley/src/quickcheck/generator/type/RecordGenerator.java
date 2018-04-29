package quickcheck.generator.type;

import java.util.List;

import quickcheck.util.TestType;
import wybs.util.AbstractCompilationUnit.Identifier;
import wyil.interpreter.ConcreteSemantics;
import wyil.interpreter.ConcreteSemantics.RValue;
import wyil.interpreter.ConcreteSemantics.RValue.Field;

public class RecordGenerator implements Generator{
	/**
	 * Used for generating appropriate values
	 */
	private static final ConcreteSemantics semantics = new ConcreteSemantics();
	
	private int count = 1;
	private TestType testType;
	private List<Generator> generators;
	private List<Identifier> names;
	private Field[] elements;
	
	public RecordGenerator(List<Generator> generators, List<Identifier> names, TestType testType) {
		this.generators = generators;
		this.names = names;
		this.testType = testType;
	}
	
	@Override
	public RValue generate() {
		if(testType == TestType.EXHAUSTIVE) {
			if(elements == null) {
				elements = new Field[generators.size()];
				for(int i=0; i < elements.length; i++) {
					Generator gen = generators.get(i);
					gen.resetCount();
					elements[i] = semantics.Field(names.get(i), gen.generate());
				}
			}
			else {
				// Generate the array elements backwards 
				for(int i=elements.length - 1; i >= 0 ; i--) {
					Generator gen = generators.get(i);
					if(!gen.exceedCount()) {
						elements[i] = semantics.Field(names.get(i), gen.generate());
						break;
					}
					else {
						gen.resetCount();
						elements[i] = semantics.Field(names.get(i), gen.generate());
					}
				}
			}
			count++;
			return semantics.Record(elements);		
		}
		else {
			Field[] recordFields = new Field[generators.size()];
			for(int i=0; i < recordFields.length; i++) {
				Generator gen = generators.get(i);
				recordFields[i] = semantics.Field(names.get(i), gen.generate());
			}
			count++;
			return semantics.Record(recordFields);
		}
	}

	@Override
	public int size() {
		int size = 0;
		for(int i=0; i < generators.size(); i++) {
			size += generators.get(i).size();
		}
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
}
