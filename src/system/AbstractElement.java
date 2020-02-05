package system;

import system.qa.UncorrectInputException;
import system.search.Searcher;
import system.Query;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.queryparser.classic.ParseException;


/**
 * Prototype of Topic and Type
 * @author Francesco Raco
 * 
 */
abstract public class AbstractElement implements Element, Comparable<AbstractElement>
{
	/**
	 * Final integer representing the max number of hits allowed
	 */
	protected static final int MAX = 30000;
	
	protected Searcher s = Searcher.getInstance();
	
	/**
	 * String value of ID
	 */
	protected String id;
	
	/**
	 * String value of birth place
	 */
	protected String birthPlace;
	
	/**
	 * Constructor with the ID
	 * @param id String value of ID
	 */
	public AbstractElement(String id) {this.id = id;}
	
	@Override
	public boolean isPerson() throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectIdException, UncorrectInputException
	{
		return false;
	}
	
	/**
	 * Verify whether the format of ID is correct or not
	 * @param s String supposed to be the ID
	 * @param message Error message
	 * @return if the format of ID is correct or not
	 * @throws UncorrectIdException
	 */
	abstract protected boolean isCorrect(String s, String message) throws UncorrectIdException;
	
	@Override
	public int compareTo(AbstractElement e) {return id.compareTo(e.id);}
	
	@Override
	public Set<String> getProperties(String field) throws NotFoundException, IOException, ParseException, UncorrectInputException
	{
		Query q = new Query(Topic.class, field, this, Searcheable.KEY);
		Set<String> set = s.getQuery(q, true);
		if (set.size() > 0) return set;
		throw new NotFoundException();
	}
	
	@Override
	public String getId() {return id;}
	
	@Override
	public String toString() {return getId();}
	
	@Override
	public Set<String> getLabels() throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException {return getLabels(new Language("@en"));}
	
	@Override
	public Set<String> getLabels(Language lang) throws IOException, UncorrectFormatLanguage, NoSuchMethodException, SecurityException, InstantiationException,
	IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, NotFoundException, UncorrectInputException
	{
		Set<String> set = new TreeSet<String>();
		set.add(id);
		return set;
	}
}
