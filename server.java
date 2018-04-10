import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class server extends Thread{
	
	private int nodePort;
	static Socket connection_socket;
	
	public server(int nodePort)
	   {	      
		   this.nodePort = nodePort;
	   }	
	
	static servercmds scmds = new servercmds();
	
	public void run() {
		
		try {
		
		// binding the socket with the given port		
		ServerSocket socket = new ServerSocket(nodePort);
		
		while(true) {
			
			 connection_socket = socket.accept();
			 new Thread().start();
			 
			 byte[] recv_req = new byte[65000];
			 DataInputStream recev = new DataInputStream(connection_socket.getInputStream());
			 recev.read(recv_req);			    
			 String in_req = new String(recv_req,0,recv_req.length,"UTF-8").replaceAll("\\p{C}", "");
			 
			 
			 String[] inReq = in_req.split(" ");
			 
			 String serveReq= inReq[1];
			 
			 switch(serveReq) 
			 {
			  case "UPFIN":				  
				  scmds.updateFTable(in_req.substring(11));
				  break;
			  case "ADD":
				  String msgSock=in_req.split(" ")[2]+":"+in_req.split(" ")[3];
				  scmds.addKey(in_req.substring(9),msgSock);
				  break;
			  case "GETKY":
				  scmds.sendKeys(in_req.substring(11));
				  break;
			  case "exitallnodes":
				  System.exit(1);	
				  break;
			  case "ADDOK":				  
				  scmds.addokmsg(in_req.substring(11));
				  break;
			  case "SER":
				  scmds.search(in_req);
				  break;
			  case "SEROK":
				  scmds.serOK(in_req);
				  break;
			  case "DELKEY":
				  String msgSock1=in_req.split(" ")[2]+":"+in_req.split(" ")[3];
				  scmds.delkey(in_req.substring(12),msgSock1);
				  break;
			  case "DELOK":
				  scmds.delokmsg(in_req.substring(11));
				  break;
			  case "GIVEKY":
				  scmds.giveaddKeys(in_req);
				  break;
			  case "SERFAIL":
				  scmds.serchfailed(in_req);
				  break;
			  default:
				  System.out.println("incomming request is "+in_req);
				  break;
			 }
			
			
		}
		}catch(SocketException e) {
			System.err.println(" Try using another port");
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(" Try using another port number");
			e.printStackTrace();
		}
	}

}
