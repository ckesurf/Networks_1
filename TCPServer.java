import java.io.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class TCPServer {
	public Hashtable<String, String> entries = new Hashtable<String, String>();
	public Hashtable<String, Long> blacklist = new Hashtable<String, Long>();

	public static void main(String argv[]) throws Exception
	{
		new TCPServer().startServer();
	}

	public void startServer() throws Exception
	{
		/* read in records */
		ReadCSV obj = new ReadCSV();
		entries = obj.parse();

        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket waitingSocket = new ServerSocket(8000);
                    System.out.println("Waiting for clients to connect...");
                    while (true) {
                        Socket connectionSocket = waitingSocket.accept();
                        clientProcessingPool.submit(new ClientTask(connectionSocket));
                    }
                } catch (IOException e) {
                    System.err.println("Unable to process client request");
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();

	}


	private class ClientTask implements Runnable {
		private final Socket clientSocket;

		private ClientTask(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			System.out.println("Got a client !");

			try {
				handleClient();
			} catch (Exception e) {
				System.out.println("Error: Something went awry handling a client.");
				e.printStackTrace();
			}
			

			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void handleClient() throws Exception
		{
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
					clientSocket.getOutputStream());

			/* is client blacklisted? */
			String ipAddressFull = clientSocket.getRemoteSocketAddress().toString();
			String ipAddress = ipAddressFull.split(":")[0];
			if (blacklist.containsKey(ipAddress)) {
				/* if 60 seconds have elapsed, remove the IP address from blacklist and carry on as usual */ 
				long elapsedTime = (System.currentTimeMillis() - blacklist.get(ipAddress))/1000;
				if (elapsedTime > 60) {
					blacklist.remove(ipAddress);
				} else {
					/* Otherwise, block the user */
					outToClient.writeBytes("You are blacklisted. Please wait 90 seconds to login again.\n");
					return;
				}
			}

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
					/* blacklist client IP address */
					String bannedIPFull = clientSocket.getRemoteSocketAddress().toString();
					String bannedIP = bannedIPFull.split(":")[0];
					blacklist.put(bannedIP, System.currentTimeMillis());

					outToClient.writeBytes("You've entered the wrong password too " +
							"many times. You will be locked for 90 seconds. \n");
					clientSocket.close();
					return;
				}
				outToClient.writeBytes("Not a valid password, please try again\n");
				outToClient.writeBytes("password: \n");
				password = inFromClient.readLine();

			}

			System.out.println(username + " is logged in.");
			outToClient.writeBytes("\n***** Welcome to Chris's Remote Server! Please execute a command *****\n");


			/* take inputs from client, execute commands */
			String clientCmd, output;
			while(clientSocket.isConnected()) {

				clientCmd = inFromClient.readLine();

				try {
					Process p = Runtime.getRuntime().exec(clientCmd);
					BufferedReader stdInput = new BufferedReader(new 
							InputStreamReader(p.getInputStream()));

					// read the output from the command
					System.out.println("Here is the standard output of the command:\n");
					while ((output = stdInput.readLine()) != null) {
						System.out.println(output);
						outToClient.writeBytes(output + '\n');
					}

				} catch (IOException e) {
					System.out.println("Exception occurred: ");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}

}
