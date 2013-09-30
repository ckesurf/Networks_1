import java.io.*;
import java.net.*;

public class TCPServer {
	public static void main(String argv[]) throws Exception
	{
		String clientSentence;
		String output;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(8000);
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
							connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
					connectionSocket.getOutputStream());
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
				
	            outToClient.writeBytes("FINISH");
	            
			} catch (IOException e) {
	            System.out.println("Exception occurred: ");
	            e.printStackTrace();
	            System.exit(-1);
	        }
			
			
			capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		}
	}
}
