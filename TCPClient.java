import java.io.*;
import java.net.*;

public class TCPClient {
	public static void main(String argv[]) throws Exception
	{
		new TCPClient().startClient();
	}
	
	public void startClient() throws Exception
	{
		String username;
		String outputFromServer = null;
		BufferedReader inFromUser = new BufferedReader(
				new InputStreamReader(System.in));
		Socket clientSocket = new Socket("localhost", 8000);
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
		
		/* will get prompted for username */
		System.out.println(inFromServer.readLine());
		
		/* give username to server */
		username = inFromUser.readLine();
		outToServer.writeBytes(username + '\n');
		
		/* now read output from server */
		outputFromServer = inFromServer.readLine();
		
		/* if not valid username, keep trying*/
		while (outputFromServer.equals("Not a valid username, please try again")) {
			System.out.println(outputFromServer);
			System.out.println(inFromServer.readLine());
			
			/* give username to server */
			username = inFromUser.readLine();
			outToServer.writeBytes(username + '\n');
			
			/* now read input from server */
			outputFromServer = inFromServer.readLine();
		}
		
		/* enter password */
		System.out.println(outputFromServer); // should say password
		String password = inFromUser.readLine();
		outToServer.writeBytes(password + '\n');
		
		/* now read output from server */
		outputFromServer = inFromServer.readLine();
		
		/* if not valid password, keep trying*/
		while (outputFromServer.equals("Not a valid password, please try again")) {
			System.out.println(outputFromServer);
			System.out.println(inFromServer.readLine());
			
			/* give username to server */
			password = inFromUser.readLine();
			outToServer.writeBytes(password + '\n');
			
			/* now read input from server */
			outputFromServer = inFromServer.readLine();
		}
		
		/* are we locked out? */
		if (outputFromServer.equals("You've entered the wrong password too " +
						"many times. You will be locked for 90 seconds. ")) {
			/* if so, close out */
			System.out.println(outputFromServer);
			close(clientSocket);
			return;
		}
		
		/* else, give commands! */
		
//		while (!outputFromServer.equals("FINISH")) {
//			System.out.println(outputFromServer);
//			outputFromServer = inFromServer.readLine();
//		}
		close(clientSocket);
		return;
	}
	
	public void close(Socket clientSocket) throws Exception
	{
		System.out.println("Client socket now closing");
		clientSocket.close();
	}
	
}
