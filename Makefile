

main:
	javac TCPClient.java TCPServer.java ReadCSV.java

server:
	java TCPServer 8000

client:
	java TCPClient localhost 8000

clean:
	rm -f *.class
