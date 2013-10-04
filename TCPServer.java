import java.io.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class TCPServer {
	/* usernames and passwords */
	public Hashtable<String, String> entries = new Hashtable<String, String>();
	/* blacklisted users; user name and time of when they were locked out */
	public Hashtable<String, Long> blacklist = new Hashtable<String, Long>();
	/* list of current users */
	public Set<String> currentUsers = new HashSet<String>();
	/* log of users and when they last connected, only < hour old information */
	public Hashtable<String, Long> usersLog = new Hashtable<String, Long>();
	/* output streams of connected users, for broadcast */
	public Set<DataOutputStream> currentStreams = new HashSet<DataOutputStream>();
	public Integer port;

	public TCPServer(Integer port_no)
	{
		port = port_no;
	}
	
	public static void main(String argv[]) throws Exception
	{
		new TCPServer(Integer.parseInt(argv[0])).startServer();
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
                    ServerSocket waitingSocket = new ServerSocket(port);
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
					long remainingTime = 60 - elapsedTime;
					outToClient.writeBytes("You are blacklisted. Please wait " + remainingTime + " seconds to login again.\n");
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
							"many times. You will be locked for 60 seconds. \n");
					clientSocket.close();
					return;
				}
				outToClient.writeBytes("Not a valid password, please try again\n");
				outToClient.writeBytes("password: \n");
				password = inFromClient.readLine();

			}
			
			/* add to current users and login records */
			currentUsers.add(username);
			usersLog.put(username, System.currentTimeMillis());
			currentStreams.add(outToClient);

			System.out.println(username + " is logged in.");
			outToClient.writeBytes("***** Welcome to Chris's Remote Server! *****\n> \n");

			/* take inputs from client, execute commands */
			String clientCmd, output;
			while(clientSocket.isConnected()) {
				/* wait until inFromClient has some command */
				while(!inFromClient.ready());

				/* read in command */
				clientCmd = inFromClient.readLine();
				String firstPart = clientCmd.split(" ")[0];
				
				/* commands that are specific to the server */
				if (clientCmd.equals("logout")) {
					clientSocket.close();
					System.out.println(username + " logged out.");
					currentUsers.remove(username);
					currentStreams.remove(clientSocket);
					return;
				} else if (clientCmd.equals("whoelse")) {
					/* loop through user log, print out */
					Iterator<String> it = currentUsers.iterator();
					while (it.hasNext()){
					    outToClient.writeBytes(it.next() + '\n');
					}
					outToClient.writeBytes("\n> \n");
				} else if (clientCmd.equals("wholasthr")){
					/* loop through users log */
					for (Map.Entry<String, Long> entry : usersLog.entrySet()) {
						/* deleted outdated (older than an hour) login records */
						long elapsedTime = (System.currentTimeMillis() - entry.getValue())/1000;
						String user = entry.getKey();
						if (elapsedTime > 3600) {
							usersLog.remove(user);
						} else {
							/* write out users that connected within last hour */
							outToClient.writeBytes(user + '\n');
						}
					}
					outToClient.writeBytes("\n> \n");
				} else if (clientCmd.split(" ")[0].equals("broadcast")) {
					/* loop through all the streams of the connect sockets and broadcast message */ 
					Iterator<DataOutputStream> it = currentStreams.iterator();
					while (it.hasNext()) {
						it.next().writeBytes("<BROADCAST>\n" + clientCmd.substring(10) 
							+ "\n<END BROADCAST>\n");
					}
					outToClient.writeBytes("\n> \n");
				} else {
				
					/* user is executing a normal system command */
					try {
						Process p = Runtime.getRuntime().exec(clientCmd);
						BufferedReader stdInput = new BufferedReader(new 
								InputStreamReader(p.getInputStream()));

						/* read the output from the command */
						System.out.println("Here is the standard output of the command:\n");
						while ((output = stdInput.readLine()) != null) {
							System.out.println(output);
							/* if output is a blank line, just send that. Don't send two blank lines */
							if (output.equals('\n')) {
								outToClient.writeBytes(output);
							} else {
								outToClient.writeBytes(output + '\n');
							}
						}
						outToClient.writeBytes("\n> \n");

					} catch (IOException e) {
						outToClient.writeBytes("Unrecognized command\n");
						System.out.println("Unrecognized command from client socket [" + 
								clientSocket.getRemoteSocketAddress() +  "] \nError details: ");
						e.printStackTrace();
						outToClient.writeBytes(e.getMessage() + '\n');
						outToClient.writeBytes("\n> \n");
					}
				}
			}
			
		}
	}

}
