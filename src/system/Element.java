package system;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.apache.lucene.queryparser.classic.ParseException;
import system.qa.UncorrectInputException;

/**
 * Common interface describing Topic and Type behavior
 * @author Francesco Raco
 * 
 */
public interface Element
{
	/**
	 * Get the ID
	 * @return String value of ID
	 */
	String getId();
	
	/**
	 * Get default labels related to this element
	 * @return Set of labels related
	 * @throws IOException
	 * @throws UncorrectFormatLanguage
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 * @throws NotFoundException
	 * @throws UncorrectInputException 
	 */
	Set<String> getLabels() throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException;
	
	/**
	 * Get labels per language related to this element
	 * @param lang Language object
	 * @return Set of labels related
	 * @throws IOException
	 * @throws UncorrectFormatLanguage
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 * @throws NotFoundException
	 * @throws UncorrectInputException 
	 */
	Set<String> getLabels(Language lang) throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException;
	
	/**
	 * Get the set of properties linked to this object
	 * @param field String value of the field where to search
	 * @return Set of properties linked to this object
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws UncorrectInputException 
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 */
	Set<String> getProperties(String field) throws NotFoundException, IOException, ParseException, UncorrectInputException, org.apache.lucene.queryparser.classic.ParseException;

	/**
	 * Know if this element is a person
	 * @return if this element is a person
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 * @throws NotFoundException 
	 * @throws UncorrectIdException 
	 * @throws UncorrectInputException 
	 * @throws org.apache.lucene.queryparser.classic.ParseException 
	 */
	boolean isPerson() throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectIdException, UncorrectInputException, org.apache.lucene.queryparser.classic.ParseException;
}
