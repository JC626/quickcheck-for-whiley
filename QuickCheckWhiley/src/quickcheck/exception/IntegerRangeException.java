package quickcheck.exception;

/**
 * Exception thrown when the upper limit
 * for an integer range is less than or equal to 
 * the lower limit of an integer range.
 * 
 * @author Janice Chin
 *
 */
public class IntegerRangeException extends Exception{
	private static final String message = "Upper integer limit is less than or equal to the lower integer limit";
	
	private static final long serialVersionUID = 1L;

	public IntegerRangeException() {
		super(message);
	}
}
