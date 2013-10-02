import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Map;

public class TCPServer {
	private Hashtable<String, String> entries;
	
	public static void main(String argv[]) throws Exception
	{
		new TCPServer().startServer();
	}
	
	public void startServer() throws Exception
	{
		ServerSocket welcomeSocket = new ServerSocket(8000);
		
		ReadCSV obj = new ReadCSV();
		Hashtable<String, String> entries = obj.parse();
		// print usernames and passwords

		for (Map.Entry<String, String> entry : entries.entrySet()) {
			System.out.println("user: " + entry.getKey() + ", password: " + entry.getValue());
		}
		
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			handleClient(connectionSocket);
			
		}
	}
	
	public void handleClient(Socket connectionSocket) throws Exception
	{
		String clientSentence;
		String output;
		String capitalizedSentence;
		
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
				connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(
				connectionSocket.getOutputStream());
		
		/* ask for username */
		outToClient.writeBytes("username: \n");
		String username = inFromClient.readLine();
		if (entries.containsKey(username)) {
			outToClient.writeBytes("Not a valid username\n");
		}
		
		clientSentence = inFromClient.readLine();

		try {
			Process p = Runtime.getRuntime().exec(clientSentence);
			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((output = stdInput.readLine()) != null) {
				System.out.println(output);
				outToClient.writeBytes(output + '\n');
			}

			outToClient.writeBytes("FINISH\n");

		} catch (IOException e) {
			System.out.println("Exception occurred: ");
			e.printStackTrace();
			System.exit(-1);
}
	}

	
	public Hashtable<String, String> getEntries() {
		return entries;
	}

	public void setEntries(Hashtable<String, String> entries) {
		this.entries = entries;
	}
	
	
	
}
