

main:
	javac TCPClient.java TCPServer.java ReadCSV.java

server:
	java TCPServer

client:
	java TCPClient

clean:
	rm -f *.class
