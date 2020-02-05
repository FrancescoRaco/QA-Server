package system;

/**
 * Uncorrect ID Exception
 * @author Francesco Raco
 * 
 */
public class UncorrectIdException extends Exception
{
	/**
	 * Specific string message describing the error
	 */
	private String message;
	
	/**
	 * Constructor with string message describing the error
	 * @param message String message describing the error
	 */
	public UncorrectIdException(String message) {this.message = message;}
	
	@Override
	public String getMessage() {return message;}

}
