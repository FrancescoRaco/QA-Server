package system.qa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.lucene.queryparser.classic.ParseException;
import system.Element;
import system.Language;
import system.NotFoundException;
import system.Query;
import system.Searcheable;
import system.Topic;
import system.Type;
import system.UncorrectFormatLanguage;
import system.UncorrectIdException;
import system.search.Searcher;

/**
 * Machine
 * @author Francesco Raco
 *
 */
public class Machine
{
	/**
	 * Instance of Searcher
	 */
	private Searcher s = Searcher.getInstance();
	
	/**
	 * Language to choose
	 */
	private Language lang = new Language("@en");
	
	/**
	 * Map representing the correspondences between an element (whose ID or label has been typed in the question) and a set of elements related (according to question)
	 */
	private Map<Element, Set<Element>> map = new TreeMap<Element, Set<Element>>();
	
	/**
	 * Map representing correspondences between a conventional term and a String value of a topic which implements a collection of predicate 
	 */
	private Map<String, String> starringColl = new TreeMap<String, String>();
	
	/**
	 * Map representing correspondences between a conventional term and a predicate/object
	 */
	private Map<String, String> predicates = new TreeMap<String, String>();
	
	/**
	 * Constructor which add default predicate/object to predicates and starringColl map
	 */
	public Machine()
	{
		addPredicate("description", "common.topic.description");
		addPredicate("genre", "film.film.genre");
		addPredicate("directed", "film.film.directed_by");
		addPredicate("written", "film.film.written_by");
		addPredicate("produced", "film.film.produced_by");
		addPredicate("city", "location citytown"); addPredicate("cities", "location citytown");
		addPredicate("movie", "film film"); addPredicate("movies", "film film");
		addPredicate("actors", "film.performance.actor", "film.film.starring");
		addPredicate("birth date", "date_of_birth");
	}
	
	/**
	 * Get index to be searched
	 * @return
	 */
	public String getSearchIndex()
	{
		return s.getIndex();
	}
	
	/**
	 * Change index to be searched
	 * @param index Index to be searched
	 */
	public void changeSearchIndex(String index)
	{
		s.changeIndex(index);
	}
	
	/**
	 * Add predicate/object to predicates map
	 * @param label String value of conventional term
	 * @param value String value of predicate/term
	 */
	public void addPredicate(String label, String value) {predicates.put(label, value);}
	
	/**
	 * Add predicate/object to predicates and starringColl map
	 * @param label String value of conventional term
	 * @param value String value of predicate/term
	 * @param starring collection of further predicates
	 */
	public void addPredicate(String label, String value, String starring)
	{
		addPredicate(label, value);
		starringColl.put(label, starring);
	}
	
	/**
	 * Set the language of labels specified by the question
	 * @param lang
	 */
	public void setLanguage(Language lang) {this.lang = lang;}
	
	/**
	 * Define string message error analyzing the tokens
	 * @param c Type of elements to be searched
	 * @param list List of string value representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the index from which start to analyze ID/Label of desired element
	 * @param endIndex Integer value representing the last index from which to stop the analysis of ID/Label of desired element
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void queryType(Class<?> c, List<String> list, int last, int startIndex, int endIndex, String search)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, NotFoundException, UncorrectInputException, UncorrectQuestionException,
			UncorrectTypeException, ParseException, UncorrectIdException
	{
		boolean isPerson = false;
		String error = "";
		if (search.equalsIgnoreCase("what")) error = "Only objects required!";
		else if (search.equalsIgnoreCase("Who")) {error = "Only people required!"; isPerson = true;}
		createAndInsertQuery(c, list, last, startIndex, endIndex, "type", isPerson, error);
	}
	
	/**
	 * Put in the map the association between a query and an element
	 * @param q Query object
	 * @param key Element object
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
	private void put(Element key, Set<Element> answer) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
	IOException, NotFoundException, ParseException, UncorrectIdException
	{
		Set<Element> values;
		if (!map.containsKey(key)) values = answer;
		else
		{
			values = map.get(key);
		    values.addAll(answer);
		}
		map.put(key, values);
	}
	
	/**
	 * Analyze the list of tokens in order to get the string value of ID/Label of desired element (default end index is the last of the list)
	 * @param list List of string values representing tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param index Integer value representing the starting index of the analysis
	 * @return String value of ID/Label of desired element
	 * @throws UncorrectQuestionException
	 */
	private String getLabel(List<String> list, int last, int index) throws UncorrectQuestionException
	{
		return getLabel(list, last, index, list.size());
	}
	
	/**
	 * Analyze the list of tokens in order to get the string value of ID/Label of desired element
	 * @param list List of string values representing tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param index Integer value representing the starting index of the analysis
	 * @param end Integer value representing the ending index of the analysis
	 * @return String value of ID/Label of desired element
	 * @throws UncorrectQuestionException
	 */
	private String getLabel(List<String> list, int last, int index, int end) throws UncorrectQuestionException
	{
		if (list.size() < last) throw new UncorrectQuestionException();
		list = list.subList(index, end);
		String word = "";
		for (String s : list) word += s + " ";
		return word.trim();
	}
	
	/**
	 * Create and insert query
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(boolean dontCare, Class<?> c, List<String> list, int last, int startIndex, int endIndex, String field)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException,
			UncorrectQuestionException, NotFoundException, UncorrectInputException,
			UncorrectTypeException, ParseException, UncorrectIdException
	{
		createAndInsertQuery(c, list, last, startIndex, endIndex, field, false, "Wrong input type", Searcheable.KEY, null, dontCare);
	}
	
	/**
	 * Create and insert query
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(Class<?> c, List<String> list, int last, int startIndex, int endIndex, String field, boolean isPerson)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException,
			UncorrectQuestionException, NotFoundException, UncorrectInputException,
			UncorrectTypeException, ParseException, UncorrectIdException
	{
		createAndInsertQuery(c, list, last, startIndex, endIndex, field, isPerson, "Wrong input type");
	}
	
	/**
	 * Create and insert query
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @param error String value representing the specific error message
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(Class<?> c, List<String> list, int last, int startIndex, int endIndex, String field, boolean isPerson, String error)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, UncorrectQuestionException, NotFoundException,
			UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException
	{
		createAndInsertQuery(c, list, last, startIndex, endIndex, field, isPerson, error, Searcheable.KEY, null, false);
	}
	
	/**
	 * Create and insert query
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @param error String value representing the specific error message
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @param predicate Type object corresponding to that of question input
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(Class<?> c, List<String> list, String field, boolean isPerson, String error, Searcheable search, Type predicate, boolean dontCare)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, UncorrectQuestionException, NotFoundException,
			UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException
	{
		createAndInsertQuery(c, list, 0, 0, 0, field, isPerson, error, search, predicate, dontCare);
	}
	
	/**
	 * Create and insert query
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @param error String value representing the specific error message
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @param predicate Type object corresponding to that of question input
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(Class<?> c, List<String> list, int last, int startIndex, int endIndex, String field, boolean isPerson, String error, Searcheable search, Type predicate, boolean dontCare)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, UncorrectQuestionException, NotFoundException,
			UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException
	{
		createAndInsertQuery(null, c, list, last, startIndex, endIndex, field, isPerson, error, search, predicate, dontCare);
	}
	
	/**
	 * Create and insert query
	 * @param starringTopic Starring topic
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @param error String value representing the specific error message
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @param predicate Type object corresponding to that of question input
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 */
	private void createAndInsertQuery(String starringTopic, Class<?> c, List<String> list, int last, int startIndex, int endIndex, String field, boolean isPerson, String error, Searcheable search, Type predicate, boolean dontCare)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, UncorrectQuestionException, NotFoundException,
			UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException
	{
		boolean areUncorrectElements = false;
		Set<Topic> topics;
		if (starringTopic == null) topics = s.getTopics(getLabel(list, last, startIndex, endIndex), lang);
		else {topics = new TreeSet<Topic>(); topics.add(s.getTopic(starringTopic));}
		if (predicate == null)
		{
			for (Topic t : topics)
			{
				Set<Element> values = new HashSet<Element>();
				if (c.equals(Topic.class)) values = s.getQuery(new Query(Topic.class, field, t, search));
				else if (c.equals(Type.class)) values = s.getQuery(new Query(Type.class, field, t, search));
				if (t.isPerson() != isPerson && dontCare == false) {areUncorrectElements = true; continue;}
				put(t, values);
			}
		}
		else
		{
			Set<Element> values = s.getQuery(new Query(Topic.class, field, predicate, search));
			put(predicate, values);
		}
		if (map.keySet().size() == 0 && areUncorrectElements == true)
			throw new UncorrectInputException(error);
		else if (map.keySet().size() == 0 && areUncorrectElements == false)
			throw new NotFoundException();
	}
	
	/**
	 * If the question does not have answer, reformulate it in order to consider a collection of predicates
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param last Integer value representing the min length allowed for the list
	 * @param startIndex Integer value representing the starting index of the analysis of ID/Label typed
	 * @param endIndex Integer value representing the ending index of the analysis of ID/Label typed
	 * @param field String value representing the field where to search
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws NotFoundException
	 */
	private void askForStarring(boolean dontCare, Class<Topic> c,
			List<String> list, int last, int startIndex, int endIndex, String field) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, UncorrectQuestionException,
			UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException, NotFoundException
	{
		try {createAndInsertQuery(dontCare, c, list, last, startIndex, endIndex, predicates.get(field));}
		catch (NotFoundException e)
		{
			if (!starringColl.containsKey(field)) throw new NotFoundException();
			Set<Topic> mainTopics = s.getTopics(getLabel(list, last, startIndex, endIndex), lang);
			boolean areAnyStarring = false;
			boolean areResults = false;
			for (Topic t : mainTopics)
			{
				Set<Element> collections;
				try {collections = s.getQuery(new Query(Topic.class, starringColl.get(field), t, Searcheable.KEY)); if (!areAnyStarring) areAnyStarring = true;}
				catch(Exception e2) {continue;}
				for (Element myColl : collections)
				{
					try {createAndInsertQuery(myColl.getId(), c, list, last, startIndex, endIndex, predicates.get(field), false, "Wrong input type", Searcheable.KEY, null, true); if (!areResults) areResults = true;}
					catch (Exception e3) {}
				}
			}
			if (!areAnyStarring || !areResults) throw new NotFoundException();
		}
	}
	
	/**
	 * Set the number of hits to a specific integer value
	 * @param dontCare If it does not matter whether the topic is a person or not
	 * @param count Integer value representing the max number of hits allowed
	 * @param c Type of element to be searched
	 * @param list List of string values representing the tokens
	 * @param field String value representing the field where to search
	 * @param isPerson Boolean value which states whether the desired element must be a person or not
	 * @param error String value representing the specific error message
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE) in the field
	 * @param predicate Type object corresponding to that of question input
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws UncorrectQuestionException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws GrammaticalException
	 */
	private void quantityChoice(boolean dontCare, int count, Class<?> c, List<String> list, String field, boolean isPerson, String error, Searcheable search, String predicate) throws NoSuchMethodException, SecurityException,
	InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, UncorrectQuestionException,
	NotFoundException, UncorrectInputException, UncorrectTypeException, ParseException, UncorrectIdException, GrammaticalException
	{
		if (predicate.endsWith("y") && count > 1) throw new GrammaticalException("Type plural form!");
		else if (predicate.endsWith("s") && count == 1) throw new GrammaticalException("Type singular form!");
		Type key = new Type(predicates.get(predicate));
		createAndInsertQuery(c, list, field, isPerson, error, search, key, dontCare);
		Set<Element> set = map.get(key);
		Set<Element> set2 = new HashSet<Element>();
		int k = 0;
		for (Element e : set)
		{
			if (k == count) break;
			set2.add(e); k++;
		}
		map.put(key, set2);
	}
	
	/**
	 * Get the answer returned by query() method
	 * @param list List of string values representing the tokens
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectFormatLanguage
	 * @throws UncorrectIdException
	 * @throws GrammaticalException
	 */
	public String getAnswer(String list) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, IOException, NotFoundException, UncorrectInputException,
	UncorrectQuestionException, UncorrectTypeException, ParseException, UncorrectFormatLanguage, UncorrectIdException, GrammaticalException
	{
		Map<Element, Set<Element>> map2 = query(list);
		
		String labels = "";
		
		for (Element key : map2.keySet())
		{
			String keyLabels = "";
			
			for (Element e : map2.get(key))
				for (String label : e.getLabels(lang)) keyLabels += label + ", ";
			if (keyLabels.length() > 0) keyLabels = keyLabels.substring(0, keyLabels.length() -2).trim();
			
			labels += key + ": " + keyLabels + "\n";
		}
		
		return labels;
	}
	
	/**
	 * Print the answer returned by query() method
	 * @param list List of string values representing the tokens
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectFormatLanguage
	 * @throws UncorrectIdException
	 * @throws GrammaticalException
	 */
	public void printAnswer(String list) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, IOException, NotFoundException, UncorrectInputException,
	UncorrectQuestionException, UncorrectTypeException, ParseException, UncorrectFormatLanguage, UncorrectIdException, GrammaticalException
	{
		System.out.print(getAnswer(list));
	}
	
	/**
	 * Analyze a question and return a map whose couples of Element key-value represent the results
	 * @param question String value of the question to process
	 * @return map whose couples of Element key-value represent the results
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectTypeException
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws GrammaticalException
	 */
	public Map<Element, Set<Element>> query(String question)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, NotFoundException,
			UncorrectInputException, UncorrectQuestionException, UncorrectTypeException,
			ParseException, UncorrectIdException, GrammaticalException
	{
		question = question.replace('?', ' ').trim();
		List<String> list1 = Arrays.asList(question.split(" "));
		List<String> list = new ArrayList<String>();
		for (String s : list1) if (!s.equals("")) list.add(s);
		if (list.get(0).equalsIgnoreCase("What") || list.get(1).equalsIgnoreCase("is"))
            queryType(Type.class, list, 3, 2, list.size(), list.get(0));
		else if (list.get(0).equalsIgnoreCase("Where")) createAndInsertQuery(Topic.class, list, 4, 2, list.size() -1, "people.person.place_of_birth", true);
		else if (list.get(1).equalsIgnoreCase("has")) createAndInsertQuery(Topic.class, list, 4, 3, list.size(), predicates.get(list.get(2)), false);
		else if (list.get(0).equalsIgnoreCase("Tell"))
		{
			if (list.get(2).equalsIgnoreCase("the") && list.contains("of") && list.indexOf("of") < list.size() -1)
			  askForStarring(true, Topic.class, list, 6, list.indexOf("of") +1, list.size(), getLabel(list, 6, 3, list.indexOf("of")));
			else quantityChoice(true, Integer.parseInt(list.get(2)), Topic.class, list, "type", false, "Wrong input", Searcheable.VALUE, list.get(3));
		}
		Map<Element, Set<Element>> map2 = map;
		map = new TreeMap<Element, Set<Element>>();
		return map2;
	}

	/**
	 * Enter to endless loop (interaction with console)
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws UncorrectInputException
	 * @throws UncorrectQuestionException
	 * @throws UncorrectTypeException
	 * @throws UncorrectFormatLanguage
	 * @throws ParseException
	 * @throws UncorrectIdException
	 * @throws GrammaticalException
	 */
	public void runConsole()
			throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, NotFoundException, UncorrectInputException, UncorrectQuestionException,
			UncorrectTypeException, UncorrectFormatLanguage, ParseException, UncorrectIdException, GrammaticalException
	{
		try {while(true)
		{
			InputStreamReader input = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(input);
			String str = "";
			str += br.readLine();
			printAnswer(str);
		}} catch(IndexOutOfBoundsException e) {}
	}
}
