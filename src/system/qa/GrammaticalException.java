package system.qa;

/**
 * Grammatical Exception
 * @author Francesco Raco
 * 
 */
public class GrammaticalException extends Exception
{
	private String message;
	
	public GrammaticalException(String message) {this.message = message;}
	
	@Override
	public String getMessage() {return message;}

}
