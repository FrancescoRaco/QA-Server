package system;

import system.qa.UncorrectInputException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.TreeSet;
import java.util.Set;
import system.Query;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Type
 * @author Francesco Raco
 * 
 */
public class Type extends AbstractElement
{
	/**
	 * Construtor handling wrong input of ID
	 * @param type String value of ID
	 * @throws UncorrectIdException
	 */
	public Type(String type) throws UncorrectIdException
	{
		super(type);
		isCorrect(id, "This is not a type!");
	}
	
	@Override
	protected boolean isCorrect(String s, String message) throws UncorrectIdException
	{
		if (s.charAt(1) == '.') throw new UncorrectIdException(message);
		return true;
	}
	
	/**
	 * Get topics related to this type
	 * @return Set of topics related to this type
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException 
	 * @throws UncorrectInputException 
	 */
	public Set<? extends Topic> getTopics() throws IOException, NoSuchMethodException, SecurityException, InstantiationException,
	IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, UncorrectIdException, NotFoundException, UncorrectInputException
	{
		{
			Query q = new Query(Topic.class, "type", this, Searcheable.VALUE);
			Set<String> topics =  s.getQuery(q, true);
			Set<Topic> returnTopics = new TreeSet<Topic>();
			for (String topic : topics)
			{
				returnTopics.add(new Topic(topic));
			}
			return returnTopics;
		}
	}
}
