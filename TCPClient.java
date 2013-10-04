import java.io.*;
import java.net.*;

public class TCPClient {
	public Integer port;
	public String serverIP;
	
	public TCPClient(String serverIPNo, Integer portNo)
	{
		serverIP = serverIPNo;
		port = portNo;
	}
	
	public static void main(String argv[]) throws Exception
	{
		new TCPClient(argv[0], Integer.parseInt(argv[1])).startClient();
	}
	
	public void startClient() throws Exception
	{
		String username;
		String outputFromServer = null;
		BufferedReader inFromUser = new BufferedReader(
				new InputStreamReader(System.in));
		Socket clientSocket = new Socket(serverIP, port);
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
		
		/* will get prompted for username, unless blacklisted */
		outputFromServer = inFromServer.readLine();
		System.out.print(outputFromServer);
		if (!outputFromServer.equals("username: ")) {
			System.out.println();
			close(clientSocket);
		}
		
		/* give username to server */
		username = inFromUser.readLine();
		outToServer.writeBytes(username + '\n');
		
		/* now read output from server */
		outputFromServer = inFromServer.readLine();
		
		/* if not valid username, keep trying*/
		while (outputFromServer.equals("Not a valid username, please try again")) {
			System.out.println(outputFromServer);
			System.out.print(inFromServer.readLine());
			
			/* give username to server */
			username = inFromUser.readLine();
			outToServer.writeBytes(username + '\n');
			
			/* now read input from server */
			outputFromServer = inFromServer.readLine();
		}
		
		/* enter password */
		System.out.print(outputFromServer); // should say password
		String password = inFromUser.readLine();
		outToServer.writeBytes(password + '\n');
		
		/* now read output from server */
		outputFromServer = inFromServer.readLine();
		
		/* if not valid password, keep trying*/
		while (outputFromServer.equals("Not a valid password, please try again")) {
			System.out.println(outputFromServer);
			System.out.print(inFromServer.readLine());
			
			/* give username to server */
			password = inFromUser.readLine();
			outToServer.writeBytes(password + '\n');
			
			/* now read input from server */
			outputFromServer = inFromServer.readLine();
		}
		
		/* are we locked out? */
		if (outputFromServer.equals("You've entered the wrong password too " +
						"many times. You will be locked for 60 seconds. ")) {
			/* if so, close out */
			System.out.println(outputFromServer);
			close(clientSocket);
		}

		/* else, give commands! */
		String command;
		while (clientSocket.isConnected()) {
			/* read all and any server output */
			while (inFromServer.ready()) {
				outputFromServer = inFromServer.readLine();
				System.out.print(outputFromServer);
				/* if output is a prompt for command, break and enter command */
				if (outputFromServer.equals("> ")) {
					break;
				} else {
					/* otherwise, block for server to continue writing output */
					while(!inFromServer.ready());
				}
				System.out.println();
				
			}

			/* user enters a command */
			command = inFromUser.readLine();
			outToServer.writeBytes(command + '\n');
			if (command.equals("logout")) {
				System.out.println("Goodbye.");
				break;
			}

			/* wait for response */
			while (!inFromServer.ready());

		}
		
		close(clientSocket);
	}
	
	public void close(Socket clientSocket) throws Exception
	{
		System.out.println("Client socket now closing");
		clientSocket.close();
		System.exit(0);
	}
	
}
