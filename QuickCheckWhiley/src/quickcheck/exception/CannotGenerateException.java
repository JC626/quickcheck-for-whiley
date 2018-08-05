package quickcheck.exception;

/**
 * Exception thrown when it is not possible 
 * to generate any values.
 * 
 * For example, when the only nominal value
 * that can be generated is a natural number
 * but the numbers generated are all negative.
 * 
 * @author Janice Chin
 *
 */
public class CannotGenerateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public CannotGenerateException(String message) {
			super(message);
		}		
}	
