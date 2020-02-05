package system.search;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import system.Element;
import system.Language;
import system.NotFoundException;
import system.Query;
import system.Searcheable;
import system.Topic;
import system.Type;
import system.UncorrectIdException;
import system.qa.UncorrectInputException;

public class Searcher
{
	/**
	 * single instance of Searcher
	 * @author Francesco Raco
	 * 
	 */
	private static Searcher s;
	
	/**
	 * Index directory with its default value
	 */
	private String index = "index";
	
	/**
	 * Private constructor
	 */
	private Searcher() {};
	
	/**
	 * Get a set containing the results of the search
	 * @param c Type of element to be searched
	 * @param field String value representing the field where to search
	 * @param term String value representing the term to be searched
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE)
	 * @return Set containing the results of the search
	 * @throws IOException
	 * @throws ParseException
	 * @throws NotFoundException
	 */
	private Set<String> getAllProperties(Class<?> c, String field, String term, Searcheable search) throws IOException, ParseException, NotFoundException
	{
		Map<String, Set<String>> queryMap = getAllElementsMap(field, term, search);
		Set<String> results = new TreeSet<String>();
		switch(search)
		{
		case KEY: results = queryMap.get(term); break;
		case VALUE: results = queryMap.keySet(); break;
		}
		return results;
	}
	
	/**
	 * Get the set containing all elements related to the search
	 * @param c Type of element to be searched
	 * @param field String value representing the field where to search
	 * @param term String value representing the term to be searched
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE)
	 * @return set containing all elements related to the search
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private Set<Element> getAllElements(Class<?> c, String field, String term, Searcheable search)
			throws IOException, NoSuchMethodException, NotFoundException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ParseException, UncorrectIdException
	{
		Set<String> results = getAllProperties(c, field, term, search);
		Set<Element> mySet = new TreeSet<Element>();
		try
		{
			for (String str: results)
		    {
				if (c.equals(Topic.class)) mySet.add(new Topic(str));
				else mySet.add(new Type(str));
		    }
		}
		catch (NullPointerException e) {throw new NotFoundException();}
		return mySet;
	}
	
	/**
	 * Get name of index directory to be read
	 * @return
	 */
	public String getIndex()
	{
		return index;
	}
	
	/**
	 * Change index
	 * @param index Name of index directory to be read
	 */
	public void changeIndex(String index)
	{
		this.index = index;
	}
	
	/**
	 * Get a type object
	 * @param str String value representing the ID of the type
	 * @return Object type
	 * @throws UncorrectIdException
	 */
	public Type getType(String str) throws UncorrectIdException
	{
		return new Type(str);
	}

	/**
	 * Get the set of topics returned by the search (getTopics(...) methods delegate to this one) 
	 * @param field String value representing the field where to search
	 * @param label String value representing the term to be searched
	 * @return Set of topics returned by the search
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException 
	 */
	private Set<Topic> topicsProcessing(String field, String label) throws NoSuchMethodException,
	SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
	InvocationTargetException, IOException, ParseException, UncorrectIdException, NotFoundException
	{
		Set<Topic> topics = new TreeSet<Topic>();
		Set<Element> elements = getAllElements(Topic.class, field, label, Searcheable.VALUE);
		for (Element e : elements) topics.add((Topic) e);
		return topics;
	}
	
	/**
	 * Get the map containing all associations between string values (representing ID/Label) of elements returned by the search
	 * @param field String value representing the field where to search
	 * @param term String value representing the term to be searched
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @return map containing all associations between string values (representing ID/Label) of elements returned by the search 
	 * @throws IOException
	 * @throws ParseException
	 * @throws NotFoundException 
	 */
	private Map<String, Set<String>> getAllElementsMap(String field, String term, Searcheable search) throws IOException, ParseException, NotFoundException
	{
		TermQuery q = new TermQuery(new Term("predicate", field));
		IndexReader dir = DirectoryReader.open( SimpleFSDirectory.open(new
		File(index)));
		IndexSearcher is = new IndexSearcher(dir);
		TopDocs docs = is.search(q, 20000000);
		Map<String, Set<String>> map = new TreeMap<String, Set<String>>();
		String toSearch = "";
		switch(search)
		{
			case KEY : toSearch = "subject"; break;
			case VALUE: toSearch = "object"; break;
		}
		for (ScoreDoc scoreDoc : docs.scoreDocs)
		{
			Document doc = is.doc(scoreDoc.doc);
		    IndexableField myFld = doc.getField(toSearch);
		    if (myFld == null || !myFld.stringValue().equals(term)) continue;
		    String name = doc.getField("subject").stringValue();
		    for (IndexableField fld : doc.getFields())
		    {
		    	if (fld.name().equals("predicate") || fld.name().equals(toSearch)) continue;
		    	String result = fld.stringValue();
		    	Set<String> values = new TreeSet<String>();
		    	if (map.containsKey(name)) values = map.get(name);
		    	values.add(result.replaceAll("-", " "));
		    	map.put(name, values);
		    }
		}
		return map;
	}
	
	/**
	 * (Singleton) Constructor
	 * @return Single instance of Searcher
	 */
	static public Searcher getInstance()
	{
		if (s == null) s = new Searcher();
		return s;
	}
	
	/**
	 * Get a topic by string value of ID
	 * @param id String value of ID
	 * @return Topic created by ID
	 * @throws UncorrectIdException
	 */
	public Topic getTopic(String id) throws UncorrectIdException {return new Topic(id);}
	
	/**
	 * Get the set of topics that have required type 
	 * @param type Required type
	 * @return Set of topics that have required type
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
	 */
	public Set<Topic> getTopics(Type type)
			throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParseException, UncorrectIdException, NotFoundException
			{
				return topicsProcessing("type", type.getId());
			}
	
	/**
	 * Get the set of topics that have required label (default language)
	 * @param label String value of required label
	 * @return Set of topics that have required label
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException 
	 */
	public Set<Topic> getTopics(String label)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, ParseException, UncorrectIdException, NotFoundException
			{
				return getTopics(label, new Language("@en"));
			}
	
	/**
	 * Get the set of topics that have required label (specific language)
	 * @param label String value of required label
	 * @param lang Language object
	 * @return Set of topics that have required label
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException 
	 */
	public Set<Topic> getTopics(String label, Language lang)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, ParseException, UncorrectIdException, NotFoundException
	{
		return topicsProcessing("label", label + ' ' + lang.getLang());
	}
	
	/**
	 * Get the set of types related to an input topic
	 * @param topic Topic typed
	 * @return Set of types related to an input topic
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException 
	 */
	public Set<Type> getTypes(Topic topic) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
	InvocationTargetException, IOException, ParseException, UncorrectIdException, NotFoundException
	{
		Set<Type> types = new TreeSet<Type>();
		Set<Element> elements = getAllElements(Topic.class, "type", topic.getId(), Searcheable.KEY);
		for (Element e : elements) types.add((Type) e);
		return types;
	}
	
	public Set<String> getQuery(Query q, boolean isPropertiesBased) throws IOException, ParseException, NotFoundException, UncorrectInputException
	{
		if (isPropertiesBased) return getAllProperties(q.getElementClass(), q.getField(), q.getId(), q.getSearch());
		throw new UncorrectInputException("You must invoke this method if you want to search properties!");
	}

	/**
	 * Get the set of elements related to the question formulated by an input query object
	 * @param q Query object
	 * @param  
	 * @return Set of elements related to the question
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	public Set<Element> getQuery(Query q)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			IOException, NotFoundException, ParseException, UncorrectIdException
	{
		return getAllElements(q.getElementClass(), q.getField(), q.getId(), q.getSearch());
	}
}
