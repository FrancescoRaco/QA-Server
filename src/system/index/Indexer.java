package system.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Point of access for custom index creation
 * @author Francesco Raco
 * 
 */
public class Indexer
{
	/**
	 * Main method
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		String path = "data/";
		new FreeBaseIndexer(path + "fb_triples_film.gz", "index").index();
	}
}