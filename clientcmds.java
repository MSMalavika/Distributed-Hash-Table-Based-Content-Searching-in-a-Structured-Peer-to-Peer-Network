import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.math3.distribution.ZipfDistribution;


public class clientcmds {
	
	static ConcurrentSkipListMap<Integer,String> node_Keys = new ConcurrentSkipListMap<Integer,String>();
	static ConcurrentSkipListMap<Integer,String> nodeResKeyTable = new ConcurrentSkipListMap<Integer,String>();
	static ConcurrentSkipListMap<Integer, ArrayList<String>> fingerTable = new ConcurrentSkipListMap<Integer, ArrayList<String>>();
	static ConcurrentSkipListMap<Integer,ArrayList<String>> Keytable = new ConcurrentSkipListMap<Integer,ArrayList<String>>();
	static ConcurrentSkipListMap<Integer,ArrayList<String>> Keytablepredcopy = new ConcurrentSkipListMap<Integer,ArrayList<String>>();
	
	static int generatedQueries = 0;
	
	
	public void reg(InetAddress BS_ip, int BS_port, int Node_port) {
		
		try {
			
			String Node_IP = InetAddress.getLocalHost().getHostAddress();
				
			String request =  "REG" + " " + Node_IP + " " + Node_port + " " + structuredpp.uname ;
			int msg_len =  request.length() + 5;
			String reg_msg = String.format("%04d", msg_len) + " " +request;
			byte[] Reg_request = reg_msg.getBytes();
		
			Socket clientSocket = new Socket(BS_ip, BS_port);
			
			//sending the register request to the BS
				
				DataOutputStream REG_out = new DataOutputStream(clientSocket.getOutputStream());
				REG_out.write(Reg_request);
				
				//receiving node socket address from BS
				
				byte[] BS_response = new byte[65000];
				DataInputStream BSResponse = new DataInputStream(clientSocket.getInputStream());
				BSResponse.read(BS_response); 
			    
			    String BSR = new String(BS_response,0,BS_response.length,"UTF-8").replaceAll("\\p{C}", "");
			    
			    String[] BS_Response = BSR.split(" ");
			    
			    if (BS_Response[3].equals("9999"))
				    {
				    	System.err.println("Error: Registration failure" );
					    }else if (BS_Response[3].equals("9998")){
					    	System.err.println("Error: Already registered, unregister first" );
						    }else if(BS_Response[3].equals("-1")){
						    	System.out.println("Error: Unknow REG comand ");
							    }else if(BS_Response[3].equals("-9999")){
							    	System.out.println("Error: Unknown command, undefined characters to Bootstrapper ");
								    }else{
							    	System.out.println("Status: REGISTERED");
							    
			    
			 // Storing the details of the other nodes received from Bootstrap Server
			    
			    int regNodeNum= Integer.parseInt(BS_Response[3]);
			    
			    if(regNodeNum>0 && regNodeNum<=20) 
				    {
				    	int k=3;
				    	for(int i=1;i<=regNodeNum;i++) 
				    	{					
				    		String node=BS_Response[k+i]+":"+BS_Response[k+i+1];
				    		
				    		MessageDigest hashing = MessageDigest.getInstance("SHA-1"); 
							byte[] key = hashing.digest(node.getBytes());							
							
							String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
									String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
							
							 int nodeKey= Integer.parseInt(sub.trim(), 2);
									 
							node_Keys.put(nodeKey, node);				    
				    		k++;
				    	}
				    	
				    }
		
}
	        
			    clientSocket.close();
		} catch( IOException a) {
			System.err.println("IOError in reg: "+a);
		} catch (NoSuchAlgorithmException e) {			
			e.printStackTrace();
		}
		
	}
	
	public void unReg(InetAddress BS_ip, int BS_port, int Node_port)  {
		
		
		leave(Node_port);
		
		try {
			
			String unRegIP= InetAddress.getLocalHost().getHostAddress();
			
			String request =  "DEL IPADDRESS" + " " + unRegIP + " " + Node_port + " " + structuredpp.uname ;					
			int msg_len =  request.length() + 5;
			String del_msg = String.format("%04d", msg_len) + " " +request;
			
			byte[] del_request = del_msg.getBytes();
					
			// Sending the unregister request to the BS
			Socket client_Socket = new Socket(BS_ip,BS_port);
					
			DataOutputStream DEL_Packet = new DataOutputStream(client_Socket.getOutputStream());
			DEL_Packet.write(del_request);
					
			//receiving node socket address from BS
					
			byte[] BS_response = new byte[65000];
			DataInputStream BSResponse = new DataInputStream(client_Socket.getInputStream());
			BSResponse.read(BS_response); 
				    
			String BSR = new String(BS_response,0,BS_response.length).replaceAll("\\p{C}", "");
			String[] BS_Response = BSR.split(" ");
			
			
			int bsr= Integer.parseInt(BS_Response[BS_Response.length-1]);
				    
				    if (/*BS_Response[BS_Response.length-1].equals("9998")*/bsr==9998){
					    	System.err.println("Error: Not registered for the given user name" );
					    }else if(/*BS_Response[BS_Response.length-1].equals("-1")*/bsr==-1){
					    	System.out.println("Error: Error in DEL command ");
					    }else if(/*BS_Response[BS_Response.length-1].equals("1")*/bsr==1){
					    	System.out.println("Status: UNREGISTERED");
					    	
					    	clientcmds.fingerTable.clear();
					    	clientcmds.Keytable.clear();
					    	clientcmds.node_Keys.clear();
					    	clientcmds.Keytablepredcopy.clear();
					    	clientcmds.nodeResKeyTable.clear();
					    	
					    }else{
					    	System.out.println("The DEL response received from BS: "+ BSR);
					    }
				            
				    client_Socket.close();
		
		}catch (IOException b) {
			System.err.println("IOError in join: "+b);
		}
			
		}
	
	public void leave(int nodePort) {
		
		// sending the other nodes to update their fingertables
		finUpdataddemsg(nodePort,1);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		// sending the keys to the successor
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		Entry<Integer, String> successor ;
		
		String keyInfoToSend= new String();
		String filename= new String();
		int k=clientcmds.Keytable.size();
		
		for(int key: clientcmds.Keytable.keySet()) {
			
			for(int a=1;a<clientcmds.Keytable.get(key).size();a++)
			{
				filename=clientcmds.Keytable.get(key).get(0).replace(" ", "_");	
				keyInfoToSend= keyInfoToSend+" "+clientcmds.Keytable.get(key).get(a).split(":")[0]+" "+clientcmds.Keytable.get(key).get(a).split(":")[1]+" "+key+" "+filename;
				
			}
			
			//clientcmds.Keytable.remove(key);//removing the keys from this node
			
		}
		keyInfoToSend.trim();
		
		String getokmsg= " GIVEKY "+k+" "+keyInfoToSend;
		getokmsg.trim();
		String getOkMsglen= String.format("%04d",getokmsg.length());
		int totalLen= getOkMsglen.length()+getokmsg.length();
		String getOKMSG= String.format("%04d",totalLen)+getokmsg;
		
		
		int noOfNodesInNW=0;
		if(node_Keys.containsKey(structuredpp.nodeKey)) {
			 noOfNodesInNW=node_Keys.size()-1;
		}else {
			noOfNodesInNW=node_Keys.size();
		}
		
		/// fintable successors
		ConcurrentSkipListMap<Integer,String> finsucc = new ConcurrentSkipListMap<Integer,String>();
		for(int startpoint: clientcmds.fingerTable.keySet() ) 
	 	{
		
			finsucc.put(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1)),clientcmds.node_Keys.get(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1))));
		 			
		 }
		
		if(noOfNodesInNW>0)
		{
				try {
					 successor= finsucc.higherEntry(structuredpp.nodeKey);
					  Integer.parseInt(successor.getValue().split(":")[1]);
				}catch (NullPointerException e) {
					 successor= finsucc.firstEntry();
					 
				}
				
				//send the give key message
				try 
				{
						InetAddress ip = InetAddress.getByName(successor.getValue().split(":")[0]);
						int port = Integer.parseInt(successor.getValue().split(":")[1]);
					
						Socket sock = new Socket();
						// sending give message 
							sock.connect(new InetSocketAddress(ip, port));//, 100);			
						DataOutputStream out = new DataOutputStream(sock.getOutputStream());
						out.write(getOKMSG.getBytes());
						
						//givekey ok msg
						
						// input stream
						byte[] giveKyOk = new byte[65000];
						DataInputStream in = new DataInputStream(sock.getInputStream());
						in.read(giveKyOk);
						sock.close();
						
						String giveOKMsg= new String (giveKyOk,0,giveKyOk.length,"UTF-8").replaceAll("\\p{C}", "");
						
						if(giveOKMsg.split(" ")[2].equals("0")) {
							
							System.out.println("Status: Successfully keys have been added at the successor");
							
						}else if(giveOKMsg.split(" ")[2].equals("9999")) {
							
							System.err.println("Error: Error in GIVEKY command");
							
						}else if(giveOKMsg.split(" ")[2].equals("9998")) {
							System.err.println("Error: Some other error");
						}else {
							System.out.println("The GIVEKYOK response received : "+ giveOKMsg);
						}
						
						
		
				}
				catch (SocketTimeoutException e) {
					// TODO: handle exception
					System.out.println("Status: give keys failed , node unreachable ");
					
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		  		
		
		}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// deletes the resources that it has 
		
				for(int k1 : clientcmds.nodeResKeyTable.keySet())
				{
					String delEntrie = clientcmds.nodeResKeyTable.get(k1);
					delentrie(delEntrie);
				}		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	}
	
	public void fingertable() {
		
		
		node_Keys.put(structuredpp.nodeKey, structuredpp.peerSockAdd);
		
		// calculating the start points 
		
				int[] startPoint= new int[15];
				for (int i=1;i<=15;i++) 
					{	
						int a= (int) Math.pow(2, (i-1));			
						startPoint[i-1]= (a+structuredpp.nodeKey)%((int) Math.pow(2, 15));			
					}
				Arrays.sort(startPoint);
				
				// calculating the interval and successor 
				 
					 for(int k=0;k<startPoint.length;k++)
						{ 	String interval = new String();
							ArrayList<String> invalSucc = new ArrayList<String>();
							
							if(k==startPoint.length-1) 
								{ interval ="["+startPoint[k]+","+structuredpp.nodeKey+")";	}
								else 
									{ interval="["+startPoint[k]+","+startPoint[k+1]+")";}
							
							invalSucc.add(interval);	
							
							int succ=0;
							String successor= new String();
							if(!node_Keys.isEmpty()) 
								{ 
									try {
										
										succ = node_Keys.ceilingKey(startPoint[k]);
										successor=Integer.toString(succ);
										
										}catch (NullPointerException e) 
										{ 
											succ = node_Keys.firstKey();
											successor=Integer.toString(succ);
										}	
									}
							else {
								
								successor=Integer.toString(structuredpp.nodeKey);
							  }
							invalSucc.add(successor);
							
							// Adding the interval and successor to the finger table
							fingerTable.put(startPoint[k], invalSucc);				
		
					}	
					 
	}	
	
	public void finUpdataddemsg(int nodePort, int type) {
		try {
			
		String updateFTable = "UPFIN "+ type +" "+InetAddress.getLocalHost().getHostAddress()+" "+nodePort+" "+structuredpp.nodeKey;
					
		int updatemsg_len =  updateFTable.length() + 5;
		String updateFinTable = String.format("%04d", updatemsg_len) + " " +updateFTable;
		byte[] updateFingerTable = updateFinTable.getBytes();				
		
		Collection<String> sockadd= node_Keys.values();
		
		String[] sockadds = new String[sockadd.size()];	
		sockadd.toArray(sockadds);
		
		 
		for(int j =0;j<sockadds.length;j++)
		{	
			if(!sockadds[j].equals(structuredpp.peerSockAdd)) {				
			String nodeIpAdd=sockadds[j].split(":")[0];
			int nodePortNum=Integer.parseInt(sockadds[j].split(":")[1]);
			
			Socket clientSocket = new Socket();
			try {
			clientSocket.connect(new InetSocketAddress(nodeIpAdd, nodePortNum));//, 100);
				
				DataOutputStream update_out = new DataOutputStream(clientSocket.getOutputStream());					
				update_out.write(updateFingerTable);
				
				byte[] updateRecevMsg = new byte[65000];
				DataInputStream recev = new DataInputStream(clientSocket.getInputStream());
				recev.read(updateRecevMsg);			    
				String updateOkMsg = new String(updateRecevMsg,0,updateRecevMsg.length,"UTF-8").replaceAll("\\p{C}", "");
				
						
				if(updateOkMsg.split(" ")[2].equals("0")) {
					System.out.println("Status: From node "+clientSocket.getRemoteSocketAddress()+" update successful ");
				}else {
					System.out.println("Status: From node "+clientSocket.getRemoteSocketAddress()+" update failed ");
				}
			}catch(SocketTimeoutException e) {
				System.out.println("Status: Update failed, node unreachable " );
			}
				
				clientSocket.close();
			}
			}
					} catch (IOException e) {							
						e.printStackTrace();
						System.err.println("ERROR: IOException while senind the update message");
						}	
				
	}
	
	public void resToNode () {
		
		try {
			File file = new File("resources_sp2p.txt");
			FileInputStream fis = new FileInputStream(file);
			byte[] bfis = new byte[(int) file.length()]; 
			fis.read(bfis); fis.close();
			String s1 = new String(bfis);
			String[] ss = s1.split("#");
			String[] s = new String[ss.length];
			
			for(int i=0;i<ss.length;i++) 
				{ 
					s[i]= new String(ss[i]).trim();
				}
			String[] s4=s[4].substring(21).trim().split("\n");
			String[] s5=s[5].substring(45).trim().split("\n");
			
			String[] res = new String[160];
			for(int i=0;i<s4.length;i++) 
				{
					res[i]=s4[i].trim();
				}
			int k=0;int i=s4.length;
			while(k<s5.length) 
				{
					res[i]=s5[k].trim();
					i++;
					k++;
				}
			
			int rnd = 0;
			 for(int b=0;b<10;b++) {
				 rnd = new Random().nextInt(res.length);
				 MessageDigest hashing = MessageDigest.getInstance("SHA-1");
				 byte[] key = hashing.digest(res[rnd].toLowerCase().getBytes());
				 
				 
				 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
							String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
				 
				 
				 int resKey=Integer.parseInt(sub.trim(), 2);
				 
				 
				 nodeResKeyTable.put(resKey, res[rnd]);
			 }
			
			 
			 
		
		}catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void keyTable() {
		
		int fileResNodeKey =0;
		for(int filekeys: nodeResKeyTable.keySet() )
		{
			try {
				fileResNodeKey =node_Keys.ceilingKey(filekeys);
				
			}catch (NullPointerException e) {
				fileResNodeKey= node_Keys.firstKey();
			}
			if(fileResNodeKey==structuredpp.nodeKey)
			{	
				ArrayList<String> peersResp= new ArrayList<String>();
				peersResp.add(nodeResKeyTable.get(filekeys));
				peersResp.add(structuredpp.peerSockAdd);
				Keytable.put(filekeys,peersResp );}
			else 
			{ 
				try {
				String addKey= " ADD "+structuredpp.peerSockAdd.split(":")[0]+" "+structuredpp.peerSockAdd.split(":")[1]+" "+filekeys+" "+nodeResKeyTable.get(filekeys).replaceAll(" ", "_");
				String addKeymsg= String.format("%04d", (addKey.length()+4))+addKey;
				
				
				InetAddress nodeIp =InetAddress.getByName(node_Keys.get(fileResNodeKey).split(":")[0]);
				int nodePort=Integer.parseInt(node_Keys.get(fileResNodeKey).split(":")[1]);
				
				Socket sock= new Socket(nodeIp, nodePort);
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.write(addKeymsg.getBytes());
				
				sock.close();
				
			}catch (IOException e) {
				// TODO: handle exception
				System.out.println(e);
			}
			}
		}
		
		
		
	}

	public void addentrie(String addEntri) {	
		
		String addEntrie= new String(addEntri).replaceAll("\\p{C}", "");
 		int filekey=0;
 		
 		// hashing the entrie name 
 		 
 		try {
 		 MessageDigest hashing = MessageDigest.getInstance("SHA-1");
		 byte[] key = hashing.digest(addEntrie.toLowerCase().getBytes());		 
		 
		 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
					String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
		 
		 
		  filekey =Integer.parseInt(sub.trim(), 2);
 		}catch(NoSuchAlgorithmException e) 
			{
				System.err.println("NoSuchAlgorithmException when hasing ");
				System.err.println("File key is not hashed properly in addentrie method ");
				System.err.println(e);
			}
 		
 		
		 if(nodeResKeyTable.containsKey(filekey)) 
		 {
			 
			 System.err.println("Status: Error has occured while adding "+addEntrie
					 +" because another file with similar name already exists "+nodeResKeyTable.get(filekey));
			 
			 
		 }else {
		
		 nodeResKeyTable.put(filekey, addEntrie);
 		
 		int fileResNodeKey =0;
 		
 		try {
			fileResNodeKey =node_Keys.ceilingKey(filekey);
			
		}catch (NullPointerException e) {
			fileResNodeKey= node_Keys.firstKey();
		}
		if(fileResNodeKey==structuredpp.nodeKey)
		{	
			if(clientcmds.Keytable.containsKey(filekey)) {
				
				ArrayList<String> peersResp= clientcmds.Keytable.get(filekey);
				peersResp.add(structuredpp.peerSockAdd);
				Keytable.put(filekey,peersResp );
				System.out.println("Status: Successfully added");
			}else {
				ArrayList<String> peersResp= new ArrayList<String>();
				peersResp.add(nodeResKeyTable.get(filekey));
				peersResp.add(structuredpp.peerSockAdd);
				Keytable.put(filekey,peersResp );
				System.out.println("Status: Successfully added");
			}
			
		 }else 
				{ 
					try {
					String addKey= " ADD "+structuredpp.peerSockAdd.split(":")[0]+" "+structuredpp.peerSockAdd.split(":")[1]+" "+filekey+" "+nodeResKeyTable.get(filekey).replaceAll(" ", "_");
					String addKeymsg= String.format("%04d", (addKey.length()+4))+addKey;
					
					
					InetAddress nodeIp =InetAddress.getByName(node_Keys.get(fileResNodeKey).split(":")[0]);
					int nodePort=Integer.parseInt(node_Keys.get(fileResNodeKey).split(":")[1]);
					
					Socket sock= new Socket(nodeIp, nodePort);
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					out.write(addKeymsg.getBytes());
					
					sock.close();
					
				}catch (IOException e) {
					// TODO: handle exception
					System.out.println(e);
				}
				}
		 }
 		
	}
	
	public void delentrie(String delEntrie) {
 		
 		int filekey=0;
 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
 // hashing the entrie name 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 		 
 		try {
 		 MessageDigest hashing = MessageDigest.getInstance("SHA-1");
		 byte[] key = hashing.digest(delEntrie.toLowerCase().getBytes());		 
		 
		 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
					String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
		 
		 
		  filekey =Integer.parseInt(sub.trim(), 2);
 		}catch(NoSuchAlgorithmException e) 
			{
				System.err.println("NoSuchAlgorithmException when hasing ");
				System.err.println("File key is not hashed properly in addentrie method ");
				System.err.println(e);
			}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//Checking for the entrie in the node
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		
 		if(clientcmds.nodeResKeyTable.containsKey(filekey)) 
	 		{
	 			if(delEntrie.equals(clientcmds.nodeResKeyTable.get(filekey))) 
		 			{
		 				// del message pampu
	 				
	 				int fileResNodeKey =0;
	 		 		
	 		 		try {
	 					fileResNodeKey =node_Keys.ceilingKey(filekey);
	 					
	 				}catch (NullPointerException e) {
	 					fileResNodeKey= node_Keys.firstKey();
	 				}
	 				if(fileResNodeKey==structuredpp.nodeKey)
	 				{	
	 					clientcmds.Keytable.get(filekey).remove(structuredpp.peerSockAdd);
	 					System.out.println("Status: Key is successfully deleted");
	 					
	 					}
	 				else 
	 				{ 
	 					try {
	 					String addKey= " DELKEY "+structuredpp.peerSockAdd.split(":")[0]+" "+structuredpp.peerSockAdd.split(":")[1]+" "+filekey+" "+nodeResKeyTable.get(filekey).replaceAll(" ", "_");
	 					String addKeymsg= String.format("%04d", (addKey.length()+4))+addKey;
	 					
	 					
	 					InetAddress nodeIp =InetAddress.getByName(node_Keys.get(fileResNodeKey).split(":")[0]);
	 					int nodePort=Integer.parseInt(node_Keys.get(fileResNodeKey).split(":")[1]);
	 					
	 					Socket sock= new Socket(nodeIp, nodePort);
	 					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
	 					out.write(addKeymsg.getBytes());
	 					System.out.println("Status: Delete message sent");
	 					sock.close();
	 					
	 				}catch (IOException e) {
	 					// TODO: handle exception
	 					System.out.println(e);
	 				}
	 				}
	 				
	 				clientcmds.nodeResKeyTable.remove(filekey);
	 				System.out.println("Status: Resource Deleted in the current node ");
	 				
		 			}else {
		 				System.out.println("Status: file "+delEntrie+" dose not exist in this node but there is a similiar file with name "+clientcmds.nodeResKeyTable.get(filekey));
		 				System.out.println("Do you want to delete "+clientcmds.nodeResKeyTable.get(filekey)+ "? [y/n]");
		 				String s = System.console().readLine();
		 				String ss= new String(s).replaceAll("\\p{C}", "");
		 				
		 				if(ss.contains("y")) {
		 					
		 					// del message pampu
		 					
		 					int fileResNodeKey =0;
			 		 		
			 		 		try {
			 					fileResNodeKey =node_Keys.ceilingKey(filekey);
			 					
			 				}catch (NullPointerException e) {
			 					fileResNodeKey= node_Keys.firstKey();
			 				}
			 				if(fileResNodeKey==structuredpp.nodeKey)
			 				{	
			 					clientcmds.Keytable.get(filekey).remove(structuredpp.peerSockAdd);
			 					System.out.println("Status: Key is successfully deleted");}
			 				else 
			 				{ 
			 					try {
			 					String delkey= " DELKEY "+structuredpp.peerSockAdd.split(":")[0]+" "+structuredpp.peerSockAdd.split(":")[1]+" "+filekey+" "+nodeResKeyTable.get(filekey).replaceAll(" ", "_");
			 					String delKeymsg= String.format("%04d", (delkey.length()+4))+delkey;
			 					
			 					
			 					InetAddress nodeIp =InetAddress.getByName(node_Keys.get(fileResNodeKey).split(":")[0]);
			 					int nodePort=Integer.parseInt(node_Keys.get(fileResNodeKey).split(":")[1]);
			 					
			 					Socket sock= new Socket(nodeIp, nodePort);
			 					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			 					out.write(delKeymsg.getBytes());
			 					System.out.println("Status: Delete message sent");
			 					sock.close();
			 					
			 				}catch (IOException e) {
			 					// TODO: handle exception
			 					System.out.println(e);
			 				}
			 				}
			 				
			 				clientcmds.nodeResKeyTable.remove(filekey);
			 				System.out.println("Status: Resource Deleted in the current node ");
		 					
			 				}else if(ss.contains("n")){
			 					System.out.println("Status: file not deleted");
				 				}else {
				 					System.out.println(ss+" unrecognized command give [y/n]");
				 				}
		 				
		 			}
	 		}else {
	 			System.out.println("Status: "+delEntrie+ "is not found in this node to delete");
	 		}
 		
 		
 		
	}

	public void details(int NP) {
		
		try {
			System.out.println("Node IP is "+InetAddress.getLocalHost().getHostAddress());
			System.out.println("Node port is "+NP);
			System.out.println("Node Key is "+structuredpp.nodeKey);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();}
		
	}
	
	public void fingertableDisplay() {
		
		System.out.println("Finger Table is");
 		for(int startpoint: clientcmds.fingerTable.keySet() ) 
 		{
 			String interval = clientcmds.fingerTable.get(startpoint).get(0);
 			String succesor = clientcmds.fingerTable.get(startpoint).get(1);
 			System.out.println("Start point is "+startpoint+" interval is "+interval+" successor is "+succesor);
 		}
		
	}

	public void keytableDisplay() {
		
		System.out.println("Key Table is");
 		
 		NavigableSet<Integer> keys=clientcmds.Keytable.descendingKeySet();
 		for(int key : keys.descendingSet())
 		{  
 			ArrayList<String> peer= new ArrayList<String>();
 			for(int i=1;i<clientcmds.Keytable.get(key).size();i++) {
 				peer.add(clientcmds.Keytable.get(key).get(i));
 			}
 			System.out.println("key of file "+clientcmds.Keytable.get(key).get(0)+" is #"+key+" responsable peers are "+peer);
 		}
		
	}

	public void entries() {
		
		System.out.println(" Node entries are:");
 		for (int i: clientcmds.nodeResKeyTable.keySet())
 		{
 			System.out.println(clientcmds.nodeResKeyTable.get(i)+" key is "+i);
 		}
		
	}

	public void findentrie() {
		
		System.out.println("Give the entrie name that is to be searched in this node");
 		String findentrie = System.console().readLine();
 		String findEntrie= new String(findentrie).replaceAll("\\p{C}", "");
 		
 		if(clientcmds.nodeResKeyTable.containsValue(findEntrie)) {
 			System.out.println(findEntrie+" is found in this node");
 		}else {
 			System.out.println(findEntrie+" is not found in this node");
 		}
		
	}

	public void keytablecopy() {
		
		System.out.println("Keys sent to the predecessor are ");
 		
 		NavigableSet<Integer> keycopy=clientcmds.Keytablepredcopy.descendingKeySet();
 		for(int key : keycopy.descendingSet())
 		{  
 			ArrayList<String> peer= new ArrayList<String>();
 			for(int i=1;i<clientcmds.Keytablepredcopy.get(key).size();i++) {
 				peer.add(clientcmds.Keytablepredcopy.get(key).get(i));
 			}
 			System.out.println("key of file "+clientcmds.Keytablepredcopy.get(key).get(0)+" is #"+key+" responsable peers are "+peer);
 		}
		
	}
	
	public void getKeys() {
		
		Entry<Integer, String> successor ;
		String getkey= " GETKY "+structuredpp.nodeKey;
		String getKy= String.format("%04d", (getkey.length()+4))+getkey; 
		
		
		int noOfNodesInNW=0;
		if(node_Keys.containsKey(structuredpp.nodeKey)) {
			 noOfNodesInNW=node_Keys.size()-1;
		}else {
			noOfNodesInNW=node_Keys.size();
		}
		
		/// fintable successors
		ConcurrentSkipListMap<Integer,String> finsucc = new ConcurrentSkipListMap<Integer,String>();
		for(int startpoint: clientcmds.fingerTable.keySet() ) 
	 	{
		
			finsucc.put(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1)),clientcmds.node_Keys.get(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1))));
		 			
		 }
		
		if(noOfNodesInNW>0)
		{
		try {
			 successor= finsucc.higherEntry(structuredpp.nodeKey);
			  Integer.parseInt(successor.getValue().split(":")[1]);
		}catch (NullPointerException e) {
			 successor= finsucc.firstEntry();
			 
		}
		
		try {
			
			InetAddress ip = InetAddress.getByName(successor.getValue().split(":")[0]);
			int port = Integer.parseInt(successor.getValue().split(":")[1]);
		
			Socket sock = new Socket();
			//try{
				sock.connect(new InetSocketAddress(ip, port));//, 100);			
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			out.write(getKy.getBytes());
			
			// input stream
			byte[] getKyOk = new byte[6500000];
			DataInputStream in = new DataInputStream(sock.getInputStream());
			in.read(getKyOk);
			sock.close();
			
			String getOKMsg= new String (getKyOk,0,getKyOk.length,"UTF-8").replaceAll("\\p{C}", "");
//			System.out.println("getOKMsg "+getOKMsg);
			
			int noKeys= Integer.parseInt(getOKMsg.split(" ")[2]);
			if(noKeys>0) 
			{
				// adding the keys to the key table
				int offset=4;
				for(int b=0;b<noKeys;b++) 
					{
						ArrayList<String> keyinfo= new ArrayList<String>();						
						String  keysock = getOKMsg.split(" ")[offset]+":"+getOKMsg.split(" ")[offset+1];						
						int key= Integer.parseInt(getOKMsg.split(" ")[offset+2]);
						String filename = getOKMsg.split(" ")[offset+3].replaceAll("_", " ");
//						System.out.println(key+" "+filename+" "+keysock);
						
						if(Keytable.containsKey(key)) {
							keyinfo=Keytable.get(key);
							if(!keyinfo.contains(keysock)) 
							{keyinfo.add(keysock);	}
							}else {
								
								keyinfo.add(filename);
								keyinfo.add(keysock);
								Keytable.put(key, keyinfo);
							}
						offset= offset+4;
					}
				System.out.println("Status: Successfully added keys from successor");
			}else {
				System.out.println("Status: No keys to add from successor");
			}
			
		} 
		catch (SocketTimeoutException e) {
			// TODO: handle exception
			System.out.println("Status: Get keys failed , node unreachable ");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}

	public void exitall(InetAddress BS_ip, int BS_port) {
		
		String request =  "GET IPLIST" +" "+ structuredpp.uname ;
		int msg_len =  request.length() + 5;
		String getIplist_msg = String.format("%04d", msg_len) + " " +request;
		
		byte[] getIplist_request = new byte[65000];
		try {
			Socket socket= new Socket(BS_ip, BS_port);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.write(getIplist_msg.getBytes());
			
			DataInputStream in = new DataInputStream(socket.getInputStream());
			in.read(getIplist_request);
			socket.close();
			
			String ipList= new String(getIplist_request,0,getIplist_request.length,"UTF-8").replaceAll("\\p{C}", "");
			int noNodes=Integer.parseInt(ipList.split(" ")[5]);
			
			if(noNodes!=9999) 
			{
			
				ArrayList<String> ipsockadd= new ArrayList<String>();
				int d=6;
				
				String b = "0012 exitallnodes";
				for(int k=0;k<noNodes;k++) 
					{
						ipsockadd.add(ipList.split(" ")[d]+":"+ipList.split(" ")[d+1]);
						d=d+2;
					}
				if(ipsockadd.contains(structuredpp.peerSockAdd))
					{
						ipsockadd.remove(structuredpp.peerSockAdd);
						
					}
				for(int n=0;n<ipsockadd.size();n++)
					{
						InetAddress ip = InetAddress.getByName(ipsockadd.get(n).split(":")[0]);
						int port = Integer.parseInt(ipsockadd.get(n).split(":")[1]);
						
						Socket sock = new Socket(ip, port);
						DataOutputStream out1 = new DataOutputStream(sock.getOutputStream());
						out1.write(b.getBytes());
						sock.close();
					}
			}
		} catch (IOException e) {			
			
		}
		
		System.exit(1);
		
	}

	public void query(int nodePort)  {
		
		System.out.println("Give the value of s for Zip'f distribution(0.6/0.7/0.8/0.9): ");
		String zipf_s = System.console().readLine();
		
		String[] res = new String[160];
		
		try {
		File file = new File("resources_sp2p.txt");
		FileInputStream fis = new FileInputStream(file);
		byte[] bfis = new byte[(int) file.length()]; 
		fis.read(bfis); fis.close();
		String s1 = new String(bfis);
		String[] ss = s1.split("#");
		String[] s = new String[ss.length];
		
		for(int i=0;i<ss.length;i++) 
			{ 
				s[i]= new String(ss[i]).trim();
			}
		String[] s4=s[4].substring(21).trim().split("\n");
		String[] s5=s[5].substring(45).trim().split("\n");
		
		
		for(int i=0;i<s4.length;i++) 
			{
				res[i]=s4[i].trim();
			}
		int k=0;int i=s4.length;
		while(k<s5.length) 
			{
				res[i]=s5[k].trim();
				i++;
				k++;
			}
		}catch (FileNotFoundException e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Resource distribution and query generation using Zipf's Law
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		int keyIndex=0;
		String searchKey = new String();
		int searchHashKey=0;
		
		ZipfDistribution zf = new ZipfDistribution(res.length,Double.parseDouble(zipf_s));
		int noQueries = 350;
		
		for(int i=0;i<noQueries;i++){
			keyIndex = zf.sample() - 1;
			
			if(keyIndex < 0){
				keyIndex = 0;
			}
			if(keyIndex > res.length){
				keyIndex = res.length-1;
			}
			
			searchKey = new String(res[keyIndex]).replace("\\p{C}", "");
			searchKey.trim();
			Timestamp genTime = new Timestamp(System.currentTimeMillis());
			long qGenTime =genTime.getTime();
			String queryGenTime= Long.toString(qGenTime);
			
			
			
			// hashing the search key
			try
			{
				MessageDigest hashing = MessageDigest.getInstance("SHA-1");
				byte[] key = hashing.digest(searchKey.toLowerCase().getBytes());
				
				
				 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
							String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
				 
				 
				 searchHashKey=Integer.parseInt(sub.trim(), 2);
				
				//searchHashKey=Byte.toUnsignedInt(key[0]);
				
			}catch(NoSuchAlgorithmException e) 
				{
					System.err.println("NoSuchAlgorithmException when hasing ");
					System.err.println(e);
				}
			
			generatedQueries = generatedQueries + 1;
			System.out.println("Queries generated: #"+generatedQueries);
			
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Searching for the key in the current node
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			
			
			Timestamp searchfoundTime = new Timestamp(System.currentTimeMillis());
			
			int resNodekey=0; 
			String resNodeSockAdd= new String();
			boolean range= false;
			
			ConcurrentSkipListMap<Integer,String> finsucc = new ConcurrentSkipListMap<Integer,String>();
			for(int startpoint: clientcmds.fingerTable.keySet() ) 
		 	{
			
				finsucc.put(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1)),node_Keys.get(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1))));
			 			
			 }
			
			if(servercmds.predecessorkey==0) {
				try {
				servercmds.predecessorkey=clientcmds.node_Keys.lowerKey(structuredpp.nodeKey);
				}catch (NullPointerException e) {
					// TODO: handle exception
					servercmds.predecessorkey=clientcmds.node_Keys.lastKey();
				}
				
			}
			
				if(servercmds.predecessorkey>structuredpp.nodeKey)
				{
					range = searchHashKey<=servercmds.predecessorkey && searchHashKey>structuredpp.nodeKey;
				}else {
					range = searchHashKey<=servercmds.predecessorkey || searchHashKey>structuredpp.nodeKey;
				}
			
			
			
			
				if(!range) {
					resNodekey=structuredpp.nodeKey;
					resNodeSockAdd=structuredpp.peerSockAdd;
				}else {
					try {
						resNodekey=finsucc.ceilingKey(searchHashKey);
						resNodeSockAdd=finsucc.get(resNodekey);
					}catch (NullPointerException e) {
						resNodekey=finsucc.firstKey();
						resNodeSockAdd=finsucc.get(resNodekey);
					}
				
				}
			
			
				
			if(resNodekey==structuredpp.nodeKey||clientcmds.Keytable.containsKey(searchHashKey)) 
			{	
				int rFound=0;
				System.out.println("Status: Responsable node for the Search of file "+searchKey+" with key "+searchHashKey+ " is the current node ");
				for(int keys: Keytable.keySet()) {
					if(keys==searchHashKey) {
						
						long qeryFound =searchfoundTime.getTime();
						long latancy =qeryFound-qGenTime;	
						ArrayList<String> peersRes = new ArrayList<String>();
						for(int k=1;k<clientcmds.Keytable.get(searchHashKey).size();k++) {
							peersRes.add(clientcmds.Keytable.get(searchHashKey).get(k));
						}
						rFound=rFound+1;
						servercmds.serok= servercmds.serok+1;
						servercmds.serokFound=servercmds.serokFound+1;
						System.out.println();
						System.out.println("Number of the Searches completed #"+servercmds.serok);
						System.out.println("Status: Search completed and successfully found the peers for file "+searchKey +" and the filekey is located in the current node ");								
						System.out.println("The peers responsible are"+ peersRes);
						System.out.println("With Latancy is "+Math.abs(latancy)+"ms"+" in "+0+" hops");
						System.out.println();
						
						String data="Hops:"+"\t"+0+"\t"+"Latancy:"+"\t"+Math.abs(latancy)+"\t"+"Filename:"+"\t"+searchKey
										+"\t"+"Filekey Found at:"+"\t"+structuredpp.peerSockAdd+"\t"+"File is located at:"+"\t"+peersRes+"\n";
						BufferedWriter out = null;
						try {
							FileWriter File = new FileWriter("Results.txt", true);
							out = new BufferedWriter(File);
							out.write(data);
							System.out.println("Writing data into file.");
							out.close();
						} catch (IOException e) {
							System.out.println("IOException occured while writing results into a file.");
							e.printStackTrace();
						}
						
						
						
					}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////					
				} if(rFound<=0) {
					
					long qeryNotFound =searchfoundTime.getTime();
					long latancy =qeryNotFound-qGenTime;
					servercmds.serok= servercmds.serok+1;
					servercmds.serokNotFound=servercmds.serokNotFound+1;
					System.out.println();
					System.out.println("Nunber of Searches completed #"+servercmds.serok);
					System.out.println("Status: Search completed and peers not found for file "+searchKey +" and the node responsible for this filekey is the current node");					
					System.out.println("With Latancy is "+Math.abs(latancy)+"ms"+" in "+0+" hops");
					System.out.println();
					
					String data="Hops:"+"\t"+0+"\t"+"Latancy:"+"\t"+latancy+"\t"+"Search:"+"\t"+searchKey+
								"\t"+"Filekey is supposed to be at:"+"\t"+structuredpp.peerSockAdd+"\n";
					BufferedWriter out = null;
					try {
						FileWriter File = new FileWriter("Results.txt", true);
						out = new BufferedWriter(File);
						out.write(data);
						System.out.println("Writing data into file.");
						out.close();
					} catch (IOException e) {
						System.out.println("IOException occured while writing results into a file.");
						e.printStackTrace();
					}
				}
				
			}else {
				
				
				
//				System.out.println("Status: Search key not found in this node so forwarding the search");
//     			System.out.println("search hash is "+searchHashKey+" filename is "+searchKey);
				String sendSockAdd=resNodeSockAdd;
				
				int hops = 0;	
				int timeToLive = 20;
				try {
				String serIp=InetAddress.getLocalHost().getHostAddress();
				
				String searchRequest = "SER"+" " +serIp + " " + nodePort + " " +searchHashKey + " " +String.format("%02d", hops)+ " " + String.format("%02d",timeToLive)+" "+queryGenTime;
				String sRequest = String.format("%04d", searchRequest.length() + 5)+ " " + searchRequest;
				
				
				Socket sock = new Socket(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1]));
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.write(sRequest.getBytes());
				sock.close();
				}catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				
			}
			
		}
		
	}
	
	public void findfile(int nodePort) {
		
		System.out.println("Give the file name to be found");
		 String searchKey=System.console().readLine();
		 String in = new String(searchKey).replaceAll("\\p{C}", "");
		 
		 Timestamp genTime = new Timestamp(System.currentTimeMillis());
			long qGenTime =genTime.getTime();
			String queryGenTime= Long.toString(qGenTime);
			
			int searchHashKey=0;
			
			generatedQueries=generatedQueries+1;
			
		 try
			{
				MessageDigest hashing = MessageDigest.getInstance("SHA-1");
				byte[] key = hashing.digest(in.toLowerCase().getBytes());
				
				
				 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
							String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
				 
				 
				  searchHashKey=Integer.parseInt(sub.trim(), 2);
									
			}catch(NoSuchAlgorithmException e) 
				{
					System.err.println("NoSuchAlgorithmException when hasing ");
					System.err.println(e);
				}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Searching for the key in the current node
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 Timestamp searchfoundTime = new Timestamp(System.currentTimeMillis());
			
			int resNodekey=0; 
			String resNodeSockAdd= new String();
			boolean range= false;
			
			ConcurrentSkipListMap<Integer,String> finsucc = new ConcurrentSkipListMap<Integer,String>();
			for(int startpoint: clientcmds.fingerTable.keySet() ) 
		 	{
			
				finsucc.put(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1)),node_Keys.get(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1))));
			 			
			 }
			
			if(servercmds.predecessorkey==0) {
				try {
				servercmds.predecessorkey=clientcmds.node_Keys.lowerKey(structuredpp.nodeKey);
				}catch (NullPointerException e) {
					// TODO: handle exception
					servercmds.predecessorkey=clientcmds.node_Keys.lastKey();
				}
				
			}
			
			
			
				if(servercmds.predecessorkey>structuredpp.nodeKey)
				{
					range = searchHashKey<=servercmds.predecessorkey && searchHashKey>structuredpp.nodeKey;
				}else {
					range = searchHashKey<=servercmds.predecessorkey || searchHashKey>structuredpp.nodeKey;
				}


				if(!range) {
					resNodekey=structuredpp.nodeKey;
					resNodeSockAdd=structuredpp.peerSockAdd;
				}else {
					try {
						resNodekey=finsucc.ceilingKey(searchHashKey);
						resNodeSockAdd=finsucc.get(resNodekey);
					}catch (NullPointerException e) {
						resNodekey=finsucc.firstKey();
						resNodeSockAdd=finsucc.get(resNodekey);
					}
				
				}
			
			
				
			if(resNodekey==structuredpp.nodeKey||clientcmds.Keytable.containsKey(searchHashKey)) 
			{	
				int rFound=0;
				//System.out.println("Status: Responsable node for the Search of file "+searchKey+" with key "+searchHashKey+ " is the current node ");
				for(int keys: Keytable.keySet()) {
					if(keys==searchHashKey) {
						
						long qeryFound =searchfoundTime.getTime();
						long latancy =qeryFound-qGenTime;	
						ArrayList<String> peersRes = new ArrayList<String>();
						for(int k=1;k<clientcmds.Keytable.get(searchHashKey).size();k++) {
							peersRes.add(clientcmds.Keytable.get(searchHashKey).get(k));
						}
						rFound=rFound+1;
						servercmds.serok= servercmds.serok+1;
						System.out.println();
						System.out.println("Number of the Searche found #"+servercmds.serok);
						System.out.println("Status: Search successful and found the current node for file "+searchKey);								
						System.out.println("The peers responsible are"+ peersRes);
						System.out.println("With Latancy is "+Math.abs(latancy)+"ms"+" in "+0+" hops");
						System.out.println();
						
						
						String data="Hops:"+"\t"+0+"\t"+"Latancy:"+"\t"+Math.abs(latancy)+"\t"+"Filename:"+"\t"+searchKey+"\n";
						BufferedWriter out = null;
						try {
							FileWriter File = new FileWriter("Results.txt", true);
							out = new BufferedWriter(File);
							out.write(data);
							System.out.println("Writing data into file.");
							out.close();
						} catch (IOException e) {
							System.out.println("IOException occured while writing results into a file.");
							e.printStackTrace();
						}
					}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////					
				} if(rFound<=0) {
					
					long qeryNotFound =searchfoundTime.getTime();
					long latancy =qeryNotFound-qGenTime;
					servercmds.serok= servercmds.serok+1;
					System.out.println();
					System.out.println("Nunber of Searches found #"+servercmds.serok);
					System.out.println("Status: The search for file "+searchKey +"  not found in the current node, latancy is "+Math.abs(latancy) +"ms in 0 hops" );
					System.out.println();
					
					String data="Hops:"+"\t"+0+"\t"+"Latancy:"+"\t"+Math.abs(latancy)+"\t"+"Search:"+"\t"+searchKey+"\n";
					BufferedWriter out = null;
					try {
						FileWriter File = new FileWriter("Results.txt", true);
						out = new BufferedWriter(File);
						out.write(data);
						System.out.println("Writing data into file.");
						out.close();
					} catch (IOException e) {
						System.out.println("IOException occured while writing results into a file.");
						e.printStackTrace();
					}
					
				}
				
			}else {
				
				
				
//				System.out.println("Status: Search key not found in this node so forwarding the search");
//     			System.out.println("search hash is "+searchHashKey+" filename is "+searchKey);
				String sendSockAdd=resNodeSockAdd;
				
				int hops = 0;	
				int timeToLive = 20;
				try {
				String serIp=InetAddress.getLocalHost().getHostAddress();
				
				String searchRequest = "SER"+" " +serIp + " " + nodePort + " " +searchHashKey + " " +String.format("%02d", hops)+ " " + String.format("%02d",timeToLive)+" "+queryGenTime;
				String sRequest = String.format("%04d", searchRequest.length() + 5)+ " " + searchRequest;
				
				
				Socket sock = new Socket(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1]));
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.write(sRequest.getBytes());
				sock.close();
				}catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
						
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			}
		 
		
	}

	
	public void resAll() {
		
		// TODO Auto-generated method stub
		System.out.println("Give <total_number_of_nodes> <node_number> ");
 		String numbers = System.console().readLine();
 		String no= new String(numbers).replaceAll("\\p{C}", "");
 		
 		int nodeNum= Integer.parseInt(no.split(" ")[1]);
 		int totalNodes= Integer.parseInt(no.split(" ")[0]);
 		
 		try {
			File file = new File("resources_sp2p.txt");
			FileInputStream fis = new FileInputStream(file);
			byte[] bfis = new byte[(int) file.length()]; 
			fis.read(bfis); fis.close();
			String s1 = new String(bfis);
			String[] ss = s1.split("#");
			String[] s = new String[ss.length];
			
			for(int i=0;i<ss.length;i++) 
				{ 
					s[i]= new String(ss[i]).trim();
				}
			String[] s4=s[4].substring(21).trim().split("\n");
			String[] s5=s[5].substring(45).trim().split("\n");
			
			String[] res = new String[160];
			for(int i=0;i<s4.length;i++) 
				{
					res[i]=s4[i].trim();
				}
			int k=0;int i=s4.length;
			while(k<s5.length) 
				{
					res[i]=s5[k].trim();
					i++;
					k++;
				}
			
			int b=0; int startIndex=(nodeNum-1)*(160/totalNodes);
			while(b<(160/totalNodes)){
				
				addentrie(res[startIndex]);
				
				startIndex++;
				b++;
			}
			 
		
		}catch (FileNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
 		
		
	}
	
}
