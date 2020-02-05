package system;

/**
 * Query needed by Searcher in order to return the answer
 * @author Francesco Raco
 * 
 */
public class Query extends AbstractElement
{
	/**
	 * Element to be searched
	 */
	protected Element e;
	
	/**
	 * Type of each element returned by the Searcher
	 */
	protected Class<?> c;
	
	/**
	 * Field where to search for the answer
	 */
	protected String field;
	
	/**
	 * Enum specifying whether to search subject (KEY) or object (VALUE)
	 */
	protected Searcheable search;
	
	/**
	 * Default operations of the constructors
	 * @param c Type of each element to be returned by the Searcher
	 * @param field String value of field to search for the answer
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE)
	 */
	private void defaultConstrOptions(Class<?> c, String field, Searcheable search)
	{
		this.c = c;
		this.field = field;
		this.search = search;
	}
	
	@Override
	protected boolean isCorrect(String s, String message)
			throws UncorrectIdException
	{
		return true;
	}
	
	/**
	 * Constructor
	 * @param c Type of each element to be returned by the Searcher
	 * @param field String value of field where to search for the answer
	 * @param id String value to be searched
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE)
	 */
	public Query(Class<?> c, String field, String id, Searcheable search)
	{
		super(id);
		defaultConstrOptions(c, field, search);
	}
	
	/**
	 * Constructor
	 * @param c Type of each element to be returned by the Searcher
	 * @param field String value of field where to search for the answer
	 * @param e Element to be searched
	 * @param search Enum specifying whether to search subject (KEY) or object (VALUE)
	 */
	public Query(Class<?> c, String field, Element e, Searcheable search)
	{
		super(e.getId());
		this.e = e;
		defaultConstrOptions(c, field, search);
	}

	/**
	 * Get type of each element to be returned by the Searcher
	 * @return
	 */
	public Class<?> getElementClass() {return c;}
	
	/**
	 * Get field where to search for the answer
	 * @return String value of the field
	 */
	public String getField() {return field;}
	
	/**
	 * Get enum specifying whether to search subject (KEY) or object (VALUE)
	 * @return Enum type (KEY or VALUE)
	 */
	public Searcheable getSearch() {return search;}
}
