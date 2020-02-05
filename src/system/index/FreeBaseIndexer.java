package system.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;


/**
 * FreeBase Indexer
 * @author Francesco Raco
 * 
 */
public class FreeBaseIndexer
{
	/**
	 * Name of compressed file
	 */
	private String nomeFileGz;
	
	/**
	 * Name of index root directory
	 */
	private String cartellaDestinazione;
	
	/**
	 * Set of properties according to which the creation of index has to be restricted
	 */
	private Set<String> properties;
	
	/**
	 * Constructor with names of compressed file and index root directory
	 * @param nomeFileGz Name of compressed file
	 * @param cartellaDestinazione Name of index root directory
	 */
	public FreeBaseIndexer(String nomeFileGz, String cartellaDestinazione)
	{
		this.nomeFileGz = nomeFileGz;
		this.cartellaDestinazione = cartellaDestinazione;
	}
	
	/**
	 * Trim the string specified by the StringBuilder object argument
	 * @param sb StringBuilder specifying a string to be trimmed
	 * @return StringBuilder containing the trimmed string
	 */
	private StringBuilder trim(StringBuilder sb)
	{
		return new StringBuilder(sb.toString().trim());
	}
	
	/**
	 * Replace all occurrences of a substring contained by a String value specified by a StringBuilder object argument
	 * @param sb StringBuilder specifying the string to be processed
	 * @param toReplace String value to be replaced
	 * @param replaceTo String value which replaces the previous
	 * @return
	 */
	private StringBuilder replace(StringBuilder sb,String toReplace,String replaceTo)
	{
		 int start;
		 while ((start=sb.indexOf(toReplace))>=0) sb = sb.replace(start,start+toReplace.length(),replaceTo);
		 return sb;
	}
	
	/**
	 * Subdivide the input text into a list of tokens delimited by an input char
	 * @param s The text to be processed
	 * @param delimiter The char value which represents the delimiter of tokens
	 * @return The list of tokens obtained 
	 */
	private List<String> getTokens(String s, char delimiter)
	{
		List<String> list = new ArrayList<String>();
		StringBuilder token = new StringBuilder("");
		for (int i = 0; i < s.length(); i++)
		{
			char myChar = s.charAt(i);
			if (myChar != delimiter) token.append(myChar);
			if (myChar == delimiter || i == s.length() - 1) {list.add(token.toString().trim()); token.delete(0, token.length());}
		}
		if (list.size() > 0)
		{
			String last = list.get(list.size() - 1);
			StringBuilder sb = new StringBuilder(last);
			if (last.endsWith(".")) sb.delete(sb.lastIndexOf("."), sb.length());
			list.set(list.size() - 1, sb.toString());
		}
		return list;
	}
	
	/**
	 * Analyze input text
	 * @param text String value of text
	 * @return List of tokens
	 * @throws IOException 
	 */
	private List<String> scan(String stringText) throws IOException
	{
		StringBuilder text = new StringBuilder(stringText);
		replace(text, "<", " ");
		replace(text, ">", " ");
		replace(text, "\"", " "); text = trim(text);
		List<String> line = getTokens(text.toString(), ' ');
		List<String> args = new ArrayList<String>();
		StringBuilder lastString = new StringBuilder("");
		int c = 0;
		for (int i = 0; i < line.size(); i++)
		{
			if (line.get(i).equals("") || line.get(i).equals(".")) continue; c++;
			StringBuilder line2 = new StringBuilder(line.get(i));
			remove(line2, "/");
			remove(line2, "#");
			if (c > 2 && lastString.length() < 512) {lastString.append(line2); lastString.append(' ');}
			else args.add(line2.toString());
		}
		lastString = trim(lastString);
		args.add(lastString.toString());
		return args;
	}
	
	/**
	 * Delete substrings (contained in the string specified by the StringBuilder object argument) until the index of their last char included
	 * @param sb StringBuilder specifying the string to be processed
	 * @param toRemove String value to be removed, starting from the first char of the String specified by the StringBuilder object argument
	 * @return StringBuilder after the changes
	 */
	private StringBuilder remove(StringBuilder sb, String toRemove)
	{
		int index;
		while ((index = sb.indexOf(toRemove)) >= 0) sb = sb.delete(0, index + toRemove.length());
		return sb;
	}
	
	/**
	 * Normalize the format of a type
	 * @param s String value representing the type
	 * @return String normalized
	 */
	private String reduceTypes(String s)
	{
		StringBuilder sb = new StringBuilder(s); replace(sb, ".", " "); replace(sb, "_", " ");
   	 	StringBuilder filteredSb = new StringBuilder("");
   	 	List<String> values = getTokens(sb.toString(), ' ');
   	 	int size = values.size();
   	    if (size > 2) values = values.subList(size - 2, size);
   	    for (int i = 0; i < values.size(); i++)
	 	{
	 		filteredSb.append(values.get(i));
	 		if (i < values.size() - 1) filteredSb.append(" ");
	    }
	 	return filteredSb.toString();
	}

	/**
	 * Set the properties according to which the creation of index has to be restricted
	 * @param properties
	 */
	public void setProperties(Set<String> properties) {this.properties = properties;}
	
	/**
	 * Create the index
	 * @throws IOException
	 */
	public void index() throws IOException {index(null);}
	
	/**
	 * Create index only for elements chosen
	 * @param coll Collection containing the string value of elements ID
	 * @throws IOException
	 */
	public void index(Collection<String> coll) throws IOException
	{
		FileInputStream fin = new FileInputStream(nomeFileGz);
	    GZIPInputStream gzis = new GZIPInputStream(fin);
	    InputStreamReader isr = new InputStreamReader(gzis);
	    BufferedReader br = new BufferedReader(isr);
	    Directory dir = SimpleFSDirectory.open(new File(cartellaDestinazione));
	    WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_48);
	    IndexWriter writer = new IndexWriter(dir, new
	    IndexWriterConfig(Version.LUCENE_48, analyzer));
	    while (br.ready())
	    {
	    	String s = br.readLine();
        	if (s.equals("")) continue;
        	List<String> triple = scan(s);
        	String key = triple.get(0);
        	String name = triple.get(1);
        	String value = triple.get(2);
        	if (name.equals("type")) value = reduceTypes(triple.get(2));
 		    if (((coll != null && coll.contains(key)) || coll == null) && (properties == null || properties.contains(name)))
 		    {
 		    	Document doc = new Document();
 		    	doc.add(new StringField("subject", key, Field.Store.YES));
 		    	doc.add(new StringField("predicate", name, Field.Store.YES));
 		    	doc.add(new StringField("object", value, Field.Store.YES));
 		    	writer.addDocument(doc);
 		    }
	     }
         writer.close();
         br.close();
    }

}