package system;

/**
 * Language
 * @author Francesco Raco
 * 
 */
public class Language
{
	/**
	 * Type of language
	 */
	private String lang;
	
	/**
	 * Constructor with the string representing the type of language
	 * @param lang String value of language
	 */
	public Language(String lang) {this.lang = lang;}
	
	/**
	 * Get type of language
	 * @return String value of language
	 */
	public String getLang() {return lang;}

}
