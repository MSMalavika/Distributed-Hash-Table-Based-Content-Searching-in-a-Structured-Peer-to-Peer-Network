# Distributed-Hash-Table-Based-Content-Searching-in-a-Structured-Peer-to-Peer-Network
The repository contains:
structuredpp.java
server.java
client.java
clientcmds.java
servercmds.java
resources.txt
commons-math3-3.6.jar


Commands:

To compile all the java files:

javac -cp commons-math3-3.6.jar structuredpp.java server.java client.java clientcmds.java servercmds.java

To execute:

java -cp .:commons-math3-3.6.jar structuredpp <Node port> <Bootstrap server IP> <Bootstrap server port>

After we enter into the interactive mode on the terminal a help is diplayed as follows:

Give any of the following options:

details: displays the node details

fingertable: displays the finger table

keytable: displays the key table

search: Query search using zipf distribution.

entries: diaplays the node entries in the node

findfile: To find a file given as user input

findentrie: checkes for if that entries in that node

unReg:To unregister from the Bootstrap server.

clear: to clear the screen

Reg:To register.

exit: To exit.

exitall: to exit all the nodes 
