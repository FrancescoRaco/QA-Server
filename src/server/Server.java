package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import system.NotFoundException;
import system.UncorrectFormatLanguage;
import system.UncorrectIdException;
import system.qa.GrammaticalException;
import system.qa.Machine;
import system.qa.UncorrectInputException;
import system.qa.UncorrectQuestionException;
import system.qa.UncorrectTypeException;

/**
 * Server
 * @author Francesco Raco
 *
 */
public class Server extends Thread
{
	/**
	 * Logger for configuring which message types are written
	 */
    public final static Logger log = Logger.getLogger("Server");
	
	
	/**
	 * Port number
	 */
    public static final int PORT_NUMBER = 8080;

	/**
	 * Socket
	 */
    protected Socket socket;
    
    /**
	 * Machine
	 */
    protected Machine machine;
	

	/**
	 * Constructor with socket
	 * @param socket Socket
	 */
    protected Server(Socket socket)
	{
		//Initialize fields
    	this.socket = socket;
    	machine = new Machine();
		
		//Begin execution calling run() method
		start();
	}

	/**
	 * Begin execution
	 */
    public void run()
	{
		//Initialize input and output streams + buffered reader (for input streams reading)
    	InputStream in = null;
		PrintWriter out = null;
		BufferedReader br = null;
		
		try
		{
			//Get input and output stream (PrintWriter is needed for writing into output stream)
			in = socket.getInputStream();
			out = new PrintWriter(socket.getOutputStream(), true);
			
			//Read input stream
			br = new BufferedReader(new InputStreamReader(in));
			
			//Log a message object with debug
	        log.debug("Connection established");
			
			//Initialize query String Builder
	        StringBuilder query = new StringBuilder();
	        
	        //List of tokens
	        List<String> tokens = new ArrayList<String>();
			
			//Read client data until receiving "END" string
	        String line;
			while ((line = br.readLine()) != null && !line.equals("END"))
			{
				tokens.add(line);
			}
			
			//Search index
			String searchIndex = tokens.get(0);
			
			if (!searchIndex.equals(machine.getSearchIndex())) machine.changeSearchIndex(searchIndex);
			
			for (int i = 1; i < tokens.size(); i++)
			{
				query.append(tokens.get(i).trim()).append("\n");
			}
			
			out.println(machine.getAnswer(query.toString()));
			
		}
		catch (IOException e)
		{
			out.println("I was unable to process your request!");
		}
		catch (NoSuchMethodException e)
		{
			out.println("I was unable to find the corresponding operation!");
		}
		catch (SecurityException e)
		{
			
			out.println("I have detected a security breach!");
		}
		catch (InstantiationException e)
		{
			out.println("Error in formulating the answer!");
		}
		catch (IllegalAccessException e)
		{
			out.println("Illegal access!");
		}
		catch (IllegalArgumentException e)
		{
			out.println("Unprocessable data!");
		}
		catch (InvocationTargetException e)
		{
			out.println("Operation not allowed!");
		}
		catch (NotFoundException e)
		{
			out.println("Data not found!");
		}
		catch (UncorrectInputException e)
		{
			out.println("Error in data formulation!");
		}
		catch (UncorrectQuestionException e)
		{
			out.println("Request not processable!");
		}
		catch (UncorrectTypeException e)
		{
			out.println("Invalid type!");
		}
		catch (ParseException e)
		{
			out.println("Error while searching for data!");
		}
		catch (UncorrectFormatLanguage e)
		{
			out.println("Language not supported!");
		}
		catch (UncorrectIdException e)
		{
			out.println("Invalid identifier!");
		}
		catch (GrammaticalException e)
		{
			out.println("Grammar mistake!");
		}
		
		finally
		{
			//Tell the client to stop listening by sending "END"
			out.println("END");
			
			//Confirmation of received data from the client 
			try
			{
				System.out.print("Il Client ha risposto: " + br.readLine() + "\n\n");
			}
			catch (IOException e)
			{
				System.out.print("Errore nella lettura della risposta del client" + "\n\n");
			}
			
			//Close the resources
			try
			{
				in.close();
				out.close();
				socket.close();
			}
			catch (IOException ex) {}
		}
	}

	/**
	 * Access point of the server
	 * @param args Args
	 */
    public static void main(String[] args)
	{
		//Initialize server
    	ServerSocket server = null;
		
    	//Endlessly listen for a Client connection on the port number chosen
    	try
		{
			server = new ServerSocket(PORT_NUMBER);
			while (true)
			{
				Socket socket = server.accept();
				
				if (socket != null) new Server(socket);
			}
		}
		
    	//Handle exceptions and finally close the server
    	catch (IOException ex)
		{
			System.out.println("Impossibile eseguire il server!");
		}
		finally
		{
			try
			{
				if (server != null)
					server.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
