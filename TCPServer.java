import java.io.*;

import java.net.*;
import java.util.*;

public class TCPServer {
	private Hashtable<String, String> entries;
	private Set<SocketAddress> blacklist = new HashSet<SocketAddress>();

	public static void main(String argv[]) throws Exception
	{
		new TCPServer().startServer();
	}

	public void startServer() throws Exception
	{
		ServerSocket welcomeSocket = new ServerSocket(8000);

		ReadCSV obj = new ReadCSV();
		entries = obj.parse();
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
		
		
		while (!entries.containsKey(username)) {
			outToClient.writeBytes("Not a valid username, please try again\n");
			outToClient.writeBytes("username: \n");
			username = inFromClient.readLine();
		}
		
		/* ask for password */
		outToClient.writeBytes("password: \n");
		String password = inFromClient.readLine();
		
		int incorrect = 0;
		String real_password = entries.get(username);
		while (!real_password.equals(password)) {
			incorrect++;
			if (incorrect >= 3) {
				SocketAddress banned_IP = connectionSocket.getRemoteSocketAddress();
				blacklist.add(banned_IP);
				
				outToClient.writeBytes("You've entered the wrong password too " +
						"many times. You will be locked for 90 seconds. \n");
				connectionSocket.close();
				return;
			}
			outToClient.writeBytes("Not a valid password, please try again\n");
			outToClient.writeBytes("password: \n");
			password = inFromClient.readLine();
			
		}

		System.out.println(username + " is logged in.");
		outToClient.writeBytes("You're logged in!\n");


		
		
		
		
//		clientSentence = inFromClient.readLine();
//
//		try {
//			Process p = Runtime.getRuntime().exec(clientSentence);
//			BufferedReader stdInput = new BufferedReader(new 
//					InputStreamReader(p.getInputStream()));
//
//			// read the output from the command
//			System.out.println("Here is the standard output of the command:\n");
//			while ((output = stdInput.readLine()) != null) {
//				System.out.println(output);
//				outToClient.writeBytes(output + '\n');
//			}
//
//			outToClient.writeBytes("FINISH\n");
//
//		} catch (IOException e) {
//			System.out.println("Exception occurred: ");
//			e.printStackTrace();
//			System.exit(-1);
//		}
	}


	public Hashtable<String, String> getEntries() {
		return entries;
	}

	private void setEntries(Hashtable<String, String> entries) {
		this.entries = entries;
	}



}
