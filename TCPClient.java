import java.io.*;
import java.net.*;

public class TCPClient {
	public static void main(String argv[]) throws Exception
	{
		String sentence;
		String outputFromServer = null;
		BufferedReader inFromUser = new BufferedReader(
				new InputStreamReader(System.in));
		Socket clientSocket = new Socket("localhost", 8000);
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
		sentence = inFromUser.readLine();
		outToServer.writeBytes(sentence + '\n');
		
		// now read input from server
		outputFromServer = inFromServer.readLine();
		while (outputFromServer != "FINISH") {
			outputFromServer = inFromServer.readLine();
			System.out.println(outputFromServer);
		}
		
		clientSocket.close();
	}
}