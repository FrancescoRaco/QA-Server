package system.qa;

/**
 * Uncorrect Input Exception
 * @author Francesco Raco
 * 
 */
public class UncorrectInputException extends Exception
{
	/**
	 * Specific string message describing the error
	 */
	private String info;
	
	/**
	 * Constructor with string message describing the error
	 * @param info String message describing the error
	 */
	public UncorrectInputException(String info) {this.info = info;}
	
	@Override
	public String getMessage() {return info;}

}
