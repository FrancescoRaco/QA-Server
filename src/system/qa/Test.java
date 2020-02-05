package system.qa;

import system.NotFoundException;
import system.UncorrectFormatLanguage;
import system.UncorrectIdException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Test class providing command line interface
 * @author Francesco Raco
 *
 */
public class Test
{
	static public void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NotFoundException, UncorrectInputException, UncorrectQuestionException, UncorrectTypeException, UncorrectFormatLanguage, ParseException, UncorrectIdException, GrammaticalException
	{
		Machine machine = new Machine();
		machine.runConsole();
	}
}

