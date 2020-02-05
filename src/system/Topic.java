package system;

import system.qa.UncorrectInputException;
import system.Query;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Topic
 * @author Francesco Raco
 * 
 */
public class Topic extends AbstractElement
{
	/**
	 * Constructor handling wrong input of ID
	 * @param id String value of ID
	 * @throws UncorrectIdException
	 */
	public Topic(String id) throws UncorrectIdException
	{
		super(id);
		isCorrect(id, "This is not a topic!");
	}
	
	@Override
	protected boolean isCorrect(String s, String message) throws UncorrectIdException
	{
		boolean isCorrect = false;
		for (int i = 0; i < id.length(); i++) if (Character.isDigit(id.charAt(i))) {isCorrect = true; break;}
		if (!isCorrect) throw new UncorrectIdException(message);
		return true;
	}
	
	/**
	 * Know if this topic is a person or not
	 * @return Whether this topic is a person or not
	 * @throws NotFoundException 
	 * @throws UncorrectIdException 
	 * @throws UncorrectInputException 
	 */
	public boolean isPerson() throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectIdException, UncorrectInputException
	{
		Set<String> valueSet = getProperties("type");
		if (valueSet.contains("people person")) return true;
		return false;
	}
	
	/**
	 * Common behavior of getLabels and getDescription
	 * @param field String value of field where to search
	 * @param lang String value of language chosen
	 * @return Set of String values related to the search
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
	private Set<String> actionPerLang(String field, Language lang) throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException
	{
		Query q = new Query(Topic.class, field, this, Searcheable.KEY);
		Set<String> targetsId = s.getQuery(q, true);
		Set<String> targetsPerLang = new TreeSet<String>();
		if (targetsId == null) throw new NotFoundException();
		for (String s : targetsId)
		{
			if (!s.contains("@")) throw new UncorrectFormatLanguage();
			int formatIndex = s.indexOf('@');
			if (s.substring(formatIndex).equals(lang.getLang())) targetsPerLang.add(s.substring(0, formatIndex).replaceAll("-", " ").trim());
		}
		return targetsPerLang;
	}
	
	@Override
	public Set<String> getLabels() throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException
	{return getLabels(new Language("@en"));}
	
	@Override
	public Set<String> getLabels(Language lang) throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException
	{
		return actionPerLang("label", lang);
	}
	
	/**
	 * Get the description (default language) of this topic
	 * @return String value of the description
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
	public String getDescription() throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException {return getDescription(new Language("@en"));}
	
	/**
	 * Get the description per language of this topic
	 * @param lang String value of language chosen
	 * @return String value of the description
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
	public String getDescription(Language lang) throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException,
	IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException
	{
		Set<String> descriptions = actionPerLang("common.topic.description", lang);
		String dForL = "";
		for (String description : descriptions) dForL = description;
		return dForL;
	}

}
