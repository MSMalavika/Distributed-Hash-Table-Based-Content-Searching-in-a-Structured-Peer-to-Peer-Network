import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

public class servercmds {
	
	static int predecessorkey= 0 ;
	static String predecessorSockADD= new String();
	int receivedQuery = 0;
	static int forwardedQuery=0;
	static int serok=0;
	static int serFail=0;
	static int serokFound=0;
	static int serokNotFound=0;
	
	ArrayList<String> copy = new ArrayList<String>();
	
	
	public void updateFTable(String updatemsg) {
		
		String type= updatemsg.split(" ")[0];
		
		
		switch (type) {
			
		
			case "0":
				// adding the node in to the network
				int keyToAdd=Integer.parseInt(updatemsg.split(" ")[3]);
				String sockAdd= updatemsg.split(" ")[1]+":"+updatemsg.split(" ")[2];
				clientcmds.node_Keys.put(keyToAdd, sockAdd);
				client.cmd.fingertable();
				
				String upOkmsg =new String();
				if(clientcmds.node_Keys.containsKey(keyToAdd)) {
					upOkmsg= "0014 UPFINOK 0";					
				}else {
					upOkmsg= "0017 UPFINOK 9998";
				}
				
				try {
				DataOutputStream out = new DataOutputStream(server.connection_socket.getOutputStream());
				out.write(upOkmsg.getBytes());
						} catch (IOException e) {
							// TODO: handle exception
							e.printStackTrace();}
				
				break;
				
			case "1":
				// deleting the node in the network
				int keyToDel=Integer.parseInt(updatemsg.split(" ")[3]);
				String sockAddDel= updatemsg.split(" ")[1]+":"+updatemsg.split(" ")[2];
				clientcmds.node_Keys.remove(keyToDel, sockAddDel);
				client.cmd.fingertable();
				
				String upOkmsg1 =new String();
				if(!clientcmds.node_Keys.containsKey(keyToDel)) {
					 upOkmsg1= "0014 UPFINOK 0";					
				}else {
					upOkmsg= "0017 UPFINOK 9998";
				}				
			 
			try {
				DataOutputStream out1 = new DataOutputStream(server.connection_socket.getOutputStream());
				out1.write(upOkmsg1.getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();}
				
				break;
		
				
		}
		
	}
	
	public void addKey(String addmsg, String msgSCK) {
		
		
		int fileResNodeKey =0;
		int filekeys= Integer.parseInt(addmsg.split(" ")[2]);
		
		String filename= addmsg.split(" ")[3].replaceAll("_"," ");		
		
		ArrayList<String> peersResp= new ArrayList<String>();
		
		if(msgSCK==structuredpp.peerSockAdd){
			System.out.println("Status: Add failed, node unrachable");
			
			
		///  adding the key in the same node	
			if(clientcmds.Keytable.containsKey(filekeys)) 
			{
						
				peersResp= clientcmds.Keytable.get(filekeys);
				if(!peersResp.contains(structuredpp.peerSockAdd)) 
				{
				peersResp.add(structuredpp.peerSockAdd);
				clientcmds.Keytable.put(filekeys, peersResp);
				System.out.println("Status: Successfully added");
				}
			}
			
			peersResp.add(filename);
			peersResp.add(structuredpp.peerSockAdd);
			clientcmds.Keytable.put(filekeys, peersResp);
			System.out.println("Status: Successfully added");
			
		}else {
		
		if(clientcmds.Keytable.containsKey(filekeys)) {
					
			peersResp= clientcmds.Keytable.get(filekeys);
			if(!peersResp.contains(msgSCK)) 
			{
			peersResp.add(msgSCK);
			clientcmds.Keytable.put(filekeys, peersResp);
			}
			
		}else {		
		
		try {	
			
			fileResNodeKey =clientcmds.node_Keys.ceilingKey(filekeys);
			
		}catch (NullPointerException e) {
			fileResNodeKey= clientcmds.node_Keys.firstKey();
		}
		
		if(fileResNodeKey==structuredpp.nodeKey)
			{		
					peersResp.add(filename);
					peersResp.add(msgSCK);
					clientcmds.Keytable.put(filekeys, peersResp);
			}
			else 
			{ 
				try {
				String addKey= " ADD "+addmsg;
				String addKeymsg= String.format("%04d", (addKey.length()+4))+addKey;
				
				InetAddress nodeIp =InetAddress.getByName(clientcmds.node_Keys.get(fileResNodeKey).split(":")[0]);
				int nodePort=Integer.parseInt(clientcmds.node_Keys.get(fileResNodeKey).split(":")[1]);
				
				Socket sock= new Socket();
				sock.connect(new InetSocketAddress(nodeIp, nodePort));//, 100);
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.write(addKeymsg.getBytes());
				sock.close();
				
	
			}catch (SocketTimeoutException e) {
					System.out.println("Status: Failded to send add message");
					// TODO: handle exception
				}catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
				
			}
		
			}
		}
		if (clientcmds.Keytable.containsKey(filekeys))
		{
			try {
				InetAddress ip = InetAddress.getByName(addmsg.split(" ")[0]);			
				int port = Integer.parseInt(addmsg.split(" ")[1]);
			
			String addokmsg = "0012 ADDOK 0";
			
			Socket sock = new Socket();
			sock.connect(new InetSocketAddress(ip, port));//, 100);
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			out.write(addokmsg.getBytes());
			sock.close();
			
			} catch(SocketTimeoutException e ) {
				System.err.println("Sending ADDOK message failed");
			}catch (ConnectException  e) {
				// TODO: handle exception
				System.err.println("Sending ADDOK message failed");
			}
			catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	 }
	}

	public void delkey(String delmsg, String msgSCK) {
		
		
		
		int fileResNodeKey =0;
		int filekey= Integer.parseInt(delmsg.split(" ")[2]);
		
		
		
		if(msgSCK==structuredpp.peerSockAdd){
			System.out.println("Status: Del unsuccessful, node unrachable");
				
			if(clientcmds.Keytable.get(filekey).contains(structuredpp.peerSockAdd)) {
			clientcmds.Keytable.get(filekey).remove(structuredpp.peerSockAdd);
			
			}
				
				
		}else {
			if(clientcmds.Keytable.containsKey(filekey)) {
				
				if(clientcmds.Keytable.get(filekey).contains(msgSCK)) {
				clientcmds.Keytable.get(filekey).remove(msgSCK);
				
				}

 				if (!clientcmds.Keytable.get(filekey).contains(msgSCK))
 				{
 					try {
 						InetAddress ip = InetAddress.getByName(msgSCK.split(":")[0]);			
 						int port = Integer.parseInt(msgSCK.split(":")[1]);
 					
 					String addokmsg = "0012 DELOK 0";
 					
 					Socket sock = new Socket();
 					sock.connect(new InetSocketAddress(ip, port));//, 100);
 					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
 					out.write(addokmsg.getBytes());
 					sock.close();
 					
 					} catch(SocketTimeoutException e ) {
 						System.err.println("Sending DELOK message failed");
 					}catch (ConnectException  e) {
 						// TODO: handle exception
 						System.err.println("Sending DELOK message failed");
 					}
 					catch (UnknownHostException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
				
				
			}else {
				
				
				// del message pampu
 				
 		 		
 		 		try {
 					fileResNodeKey =clientcmds.node_Keys.ceilingKey(filekey);
 					
 				}catch (NullPointerException e) {
 					fileResNodeKey= clientcmds.node_Keys.firstKey();
 				}
 		 		
 				if(fileResNodeKey==structuredpp.nodeKey)
 				{	
 					if(clientcmds.Keytable.get(filekey).contains(msgSCK)) {
 						clientcmds.Keytable.get(filekey).remove(msgSCK);
 						
 						}
 					

 	 				if (!clientcmds.Keytable.get(filekey).contains(msgSCK))
 	 				{
 	 					try {
 	 						InetAddress ip = InetAddress.getByName(msgSCK.split(":")[0]);			
 	 						int port = Integer.parseInt(msgSCK.split(":")[1]);
 	 					
 	 					String addokmsg = "0012 DELOK 0";
 	 					
 	 					Socket sock = new Socket();
 	 					sock.connect(new InetSocketAddress(ip, port));//, 100);
 	 					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
 	 					out.write(addokmsg.getBytes());
 	 					sock.close();
 	 					
 	 					} catch(SocketTimeoutException e ) {
 	 						System.err.println("Sending DELOK message failed");
 	 					}catch (ConnectException  e) {
 	 						// TODO: handle exception
 	 						System.err.println("Sending DELOK message failed");
 	 					}
 	 					catch (UnknownHostException e) {
 	 						// TODO Auto-generated catch block
 	 						e.printStackTrace();
 	 					} catch (IOException e) {
 	 						// TODO Auto-generated catch block
 	 						e.printStackTrace();
 	 					}
 	 				}
 					
 				}
 				
 				else 
 				{ 
 					try {
 					String delKey= " DELKEY "+delmsg;
 					String delKeymsg= String.format("%04d", (delKey.length()+4))+delKey;
 					
 					System.out.println(" delKeymsg sending from sever "+delKeymsg);
 					
 					InetAddress nodeIp =InetAddress.getByName(clientcmds.node_Keys.get(fileResNodeKey).split(":")[0]);
 					int nodePort=Integer.parseInt(clientcmds.node_Keys.get(fileResNodeKey).split(":")[1]);
 					
 					Socket sock= new Socket();
 					sock.connect(new InetSocketAddress(nodeIp, nodePort));//, 100);
 					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
 					out.write(delKeymsg.getBytes());
 					
 					sock.close();
 					
 				}catch (SocketTimeoutException e) {
					System.out.println("Status: Failded to send del message");
					// TODO: handle exception
				}
 					catch (IOException e) {
 					// TODO: handle exception
 					System.out.println(e);
 				}
 				}
 				
 				
 				
 				
			}
			
		}
		
		
	}
	
	public void sendKeys(String addmsg) {
		
		predecessorkey= Integer.parseInt(addmsg);
		predecessorSockADD=server.connection_socket.getLocalSocketAddress().toString().substring(1);
		
		
		String keyInfoToSend= new String();
		String filename= new String();
		int k=0;
		boolean range= false;
		
		
		for(int key: clientcmds.Keytable.keySet()) 
		{
			if(predecessorkey>structuredpp.nodeKey)
			{
				range = key<=predecessorkey && key>structuredpp.nodeKey;
			}else {
				range = key<=predecessorkey || key>structuredpp.nodeKey;
			}
			
			if(range) 
			{
				for(int a=1;a<clientcmds.Keytable.get(key).size();a++)
				{
					filename=clientcmds.Keytable.get(key).get(0).replace(" ", "_");	
					keyInfoToSend= keyInfoToSend+" "+clientcmds.Keytable.get(key).get(a).split(":")[0]+" "+clientcmds.Keytable.get(key).get(a).split(":")[1]+" "+key+" "+filename;
					
				}
				
				clientcmds.Keytablepredcopy.put(key,clientcmds.Keytable.get(key));
				clientcmds.Keytable.remove(key);//removing the keys from this node
				k++;
			}
		}
		keyInfoToSend.trim();
		
		String getokmsg= " GETKYOK "+k+" "+keyInfoToSend;
		getokmsg.trim();
		String getOkMsglen= String.format("%04d",getokmsg.length());
		int totalLen= getOkMsglen.length()+getokmsg.length();
		String getOKMSG= String.format("%04d",totalLen)+getokmsg;
		
		try {
			DataOutputStream out = new DataOutputStream(server.connection_socket.getOutputStream());		
			out.write(getOKMSG.getBytes());
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public void addokmsg(String addokmsg) {
		
		String type= addokmsg;
		
		switch(type)
		{
		  case "0":
			  System.out.println("Status: key is successfully added at node "+server.connection_socket.getRemoteSocketAddress().toString());
			  break;
		  case "9999":
			  System.out.println("9999");
			  break;
		 default:
			  System.out.println(addokmsg);
			  break;
		}
		
		
	}
	
	public void delokmsg(String delokmsg) {
		
		String type= delokmsg;
				
				switch(type)
				{
				  case "0":
					  System.out.println("Status: key is successfully deleted at node "+server.connection_socket.getRemoteSocketAddress().toString());
					  break;
				  case "9999":
					  System.out.println("9999");
					  break;
				 default:
					  System.out.println(delokmsg);
					  break;
				}
		
	}
	
	public void search(String sermsg) {
		
		try {
		String[] serMSG=sermsg.split(" ");
		
		InetAddress sIP= InetAddress.getByName(serMSG[2]);
		int sPort= Integer.parseInt(serMSG[3]);
		int searchHashKey= Integer.parseInt(serMSG[4]);
		int hops = Integer.parseInt(serMSG[5]);
		int TTL= Integer.parseInt(serMSG[6]);
		
		TTL=TTL-1;hops= hops+1;
		receivedQuery = receivedQuery + 1;
		System.out.println();
		System.out.println("query received is "+receivedQuery);
		System.out.println();
		
		
		String sockADD = sIP.getHostAddress() + ":" + sPort;
		
		String sermsgcopy=sermsg.split(" ")[0]+" "+sermsg.split(" ")[1]+" "+sermsg.split(" ")[2]+" "+sermsg.split(" ")[3]+" "+sermsg.split(" ")[4]+" "+sermsg.split(" ")[7];
		
		int resNodekey=0; String resNodeSockAdd= new String();
		ConcurrentSkipListMap<Integer,String> finsucc = new ConcurrentSkipListMap<Integer,String>();
		for(int startpoint: clientcmds.fingerTable.keySet() ) 
	 	{
		
			finsucc.put(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1)),clientcmds.node_Keys.get(Integer.parseInt(clientcmds.fingerTable.get(startpoint).get(1))));
			finsucc.remove(structuredpp.nodeKey);
		 			
		 }
		
		if(!copy.contains(sermsgcopy)) 
			{
				
				if(sockADD.equals(structuredpp.peerSockAdd)) {
					// send to lower
					
					try {
						resNodekey=finsucc.floorKey(searchHashKey);
						resNodeSockAdd=finsucc.get(resNodekey);
					}catch (NullPointerException e) {
						resNodekey=finsucc.lastKey();
						resNodeSockAdd=finsucc.get(resNodekey);
					}
					
					if(TTL>0) 
					{
						// Forwarding the request
						
						
						String serForwarding = sermsg.split(" ")[0]+" "+sermsg.split(" ")[1]+" "+sermsg.split(" ")[2]+" "+sermsg.split(" ")[3]+" "+sermsg.split(" ")[4]+" "+String.format("%02d",hops)+" "+ String.format("%02d", TTL)+" "+sermsg.split(" ")[7];
						String sendSockAdd=resNodeSockAdd;
						
						Socket sock = new Socket();
						try {
						sock.connect(new InetSocketAddress(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1])));//, 100);
						DataOutputStream out = new DataOutputStream(sock.getOutputStream());
						out.write(serForwarding.getBytes());
						forwardedQuery=forwardedQuery+1;
						System.out.println("Query Forwarded: "+forwardedQuery );
						System.out.println();
						}catch (SocketTimeoutException e) {
							// TODO: handle exception
							System.out.println("Status: Forwarding query failed due to connection timeout ");
						}
						sock.close();				
						
					}else if(!(TTL>0)){
						// As the the Time to live is 0 the packet is killed and not forwarded anymore
//						System.out.println("Query killed");
//						System.out.println("Query is: " +sermsg);
						
						String serfail=" SERFAIL "+searchHashKey;
						String serFailmsg= String.format("%04d", serfail.length()+4)+serfail;
						
						Socket sock = new Socket();
								
						try {
							sock.connect(new InetSocketAddress(sIP,sPort));//, 100);
							DataOutputStream out = new DataOutputStream(sock.getOutputStream());
							out.write(serFailmsg.getBytes());
							
							}catch (SocketTimeoutException e) {
								// TODO: handle exception
								System.out.println("Status: SerFail message sending failed due to connection timeout ");
								e.printStackTrace();
							}
							sock.close();	
						
					}
					
					// add in copy with an additional filed only for this
					 copy.add(sermsgcopy);
					
				}else {
					// send to higher , ippude emi chesavo adi
					
					
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Searching for the key in the current node
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
					
					
					Timestamp searchfoundTime = new Timestamp(System.currentTimeMillis());
					boolean range= false;
					
					
					
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
						System.out.println("pred "+ servercmds.predecessorkey );
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
						int noOFpeers=0;
						String keyinfoTosend= new String ();
						long qeryFound=0;
						
						for(int keys: clientcmds.Keytable.keySet()) {
							if(keys==searchHashKey) {
								
								qeryFound =searchfoundTime.getTime();
								String filename = clientcmds.Keytable.get(searchHashKey).get(0).replaceAll(" ", "_");
								
								noOFpeers= clientcmds.Keytable.get(searchHashKey).size()-1;
								for(int k=1;k<clientcmds.Keytable.get(searchHashKey).size();k++) {
									
									keyinfoTosend= keyinfoTosend+" "+clientcmds.Keytable.get(searchHashKey).get(k).split(":")[0]+" "+clientcmds.Keytable.get(searchHashKey).get(k).split(":")[1]+" "+filename;
								}keyinfoTosend.trim();
								
								rFound=rFound+1;
							}
							
						}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

								if(rFound>0) 
								{	
									String searchOKmsg = " SEROK" +" "+ String.format("%03d",noOFpeers) +" "+keyinfoTosend+" "+String.format("%02d",hops)+" "+qeryFound ;
									String serOK= String.format("%04d", searchOKmsg.length()+4)+searchOKmsg;
									
									Socket sock = new Socket(sIP,sPort);
									DataOutputStream out = new DataOutputStream(sock.getOutputStream());
									out.write(serOK.getBytes());				
									sock.close();
									
									}else if (rFound<=0) 
										{	qeryFound =searchfoundTime.getTime();
											String serOKmsg = " SEROK" +" "+ String.format("%03d",0)+" "+searchHashKey+" "+String.format("%02d",hops)+" "+qeryFound;
											String SerOKmsg= String.format("%04d",serOKmsg.length()+4)+serOKmsg;
											Socket sock = new Socket(sIP,sPort);
											DataOutputStream out = new DataOutputStream(sock.getOutputStream());
											out.write(SerOKmsg.getBytes());				
											sock.close();
											
										}
									
						}else if(TTL>0) 
							{
								// Forwarding the request
								
								
								String serForwarding = sermsg.split(" ")[0]+" "+sermsg.split(" ")[1]+" "+sermsg.split(" ")[2]+" "+sermsg.split(" ")[3]+" "+sermsg.split(" ")[4]+" "+String.format("%02d",hops)+" "+ String.format("%02d", TTL)+" "+sermsg.split(" ")[7];
								String sendSockAdd=resNodeSockAdd;
								
								Socket sock = new Socket();
								try {
								sock.connect(new InetSocketAddress(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1])));//, 100);
								DataOutputStream out = new DataOutputStream(sock.getOutputStream());
								out.write(serForwarding.getBytes());
								forwardedQuery=forwardedQuery+1;
								System.out.println("Query Forwarded: "+forwardedQuery );
								System.out.println();
								}catch (SocketTimeoutException e) {
									// TODO: handle exception
									System.out.println("Status: Forwarding query failed due to connection timeout ");
								}
								sock.close();				
								
							}else if(!(TTL>0)){
								// As the the Time to live is 0 the packet is killed and not forwarded anymore
//								System.out.println("Query killed");
//								System.out.println("Query is: " +sermsg);
								
								String serfail=" SERFAIL "+searchHashKey;
								String serFailmsg= String.format("%04d", serfail.length()+4)+serfail;
								
								Socket sock = new Socket();
										
								try {
									sock.connect(new InetSocketAddress(sIP,sPort));//, 100);
									DataOutputStream out = new DataOutputStream(sock.getOutputStream());
									out.write(serFailmsg.getBytes());
									
									}catch (SocketTimeoutException e) {
										// TODO: handle exception
										System.out.println("Status: SerFail message sending failed due to connection timeout ");
										e.printStackTrace();
									}
									sock.close();	
								
							}
					
					
					// add in copy 
					
					copy.add(sermsgcopy);
					
				}
			
				}else {
					
					if(sockADD.equals(structuredpp.peerSockAdd)) {
						
						
						
						// if the additional filed is 1 again send to lower and decrement it
						
						
						// if the additional filed is 0 failed
						
//						serfail=serfail+1;
//			    		System.out.println("Failed Search: #"+serfail);
//			    		System.out.println("Status: Search Failed, unbale to locate the peer responsable for "+searchHashKey);
			    		
			    		
			    		// send to lower
						
						try {
							resNodekey=finsucc.floorKey(searchHashKey);
							resNodeSockAdd=finsucc.get(resNodekey);
						}catch (NullPointerException e) {
							resNodekey=finsucc.lastKey();
							resNodeSockAdd=finsucc.get(resNodekey);
						}
						
						if(TTL>0) 
						{
							// Forwarding the request
							
							
							String serForwarding = sermsg.split(" ")[0]+" "+sermsg.split(" ")[1]+" "+sermsg.split(" ")[2]+" "+sermsg.split(" ")[3]+" "+sermsg.split(" ")[4]+" "+String.format("%02d",hops)+" "+ String.format("%02d", TTL)+" "+sermsg.split(" ")[7];
							String sendSockAdd=resNodeSockAdd;
							
							Socket sock = new Socket();
							try {
							sock.connect(new InetSocketAddress(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1])));//, 100);
							DataOutputStream out = new DataOutputStream(sock.getOutputStream());
							out.write(serForwarding.getBytes());
							forwardedQuery=forwardedQuery+1;
							System.out.println("Query Forwarded: "+forwardedQuery );
							System.out.println();
							}catch (SocketTimeoutException e) {
								// TODO: handle exception
								System.out.println("Status: Forwarding query failed due to connection timeout ");
							}
							sock.close();				
							
						}else if(!(TTL>0)){
							// As the the Time to live is 0 the packet is killed and not forwarded anymore
//							System.out.println("Query killed");
//							System.out.println("Query is: " +sermsg);
							
							String serfail=" SERFAIL "+searchHashKey;
							String serFailmsg= String.format("%04d", serfail.length()+4)+serfail;
							
							Socket sock = new Socket();
									
							try {
								sock.connect(new InetSocketAddress(sIP,sPort));//, 100);
								DataOutputStream out = new DataOutputStream(sock.getOutputStream());
								out.write(serFailmsg.getBytes());
								
								}catch (SocketTimeoutException e) {
									// TODO: handle exception
									System.out.println("Status: SerFail message sending failed due to connection timeout ");
									e.printStackTrace();
								}
								sock.close();	
							
						}
						
						
					}else {
						
						// send to lower
						
						try {
							resNodekey=finsucc.floorKey(searchHashKey);
							resNodeSockAdd=finsucc.get(resNodekey);
						}catch (NullPointerException e) {
							resNodekey=finsucc.lastKey();
							resNodeSockAdd=finsucc.get(resNodekey);
						}
						
						if(TTL>0) 
						{
							// Forwarding the request
							
							String serForwarding = sermsg.split(" ")[0]+" "+sermsg.split(" ")[1]+" "+sermsg.split(" ")[2]+" "+sermsg.split(" ")[3]+" "+sermsg.split(" ")[4]+" "+String.format("%02d",hops)+" "+ String.format("%02d", TTL)+" "+sermsg.split(" ")[7];
							String sendSockAdd=resNodeSockAdd;
							
							Socket sock = new Socket();
							try {
							sock.connect(new InetSocketAddress(InetAddress.getByName(sendSockAdd.split(":")[0]),Integer.parseInt(sendSockAdd.split(":")[1])));//, 100);
							DataOutputStream out = new DataOutputStream(sock.getOutputStream());
							out.write(serForwarding.getBytes());
							forwardedQuery=forwardedQuery+1;
							System.out.println("Query Forwarded: "+forwardedQuery );
							System.out.println();
							}catch (SocketTimeoutException e) {
								// TODO: handle exception
								System.out.println("Status: Forwarding query failed due to connection timeout ");
							}
							sock.close();				
							
						}else if(!(TTL>0)){
							// As the the Time to live is 0 the packet is killed and not forwarded anymore
//							System.out.println("Query killed");
//							System.out.println("Query is: " +sermsg);
							
							String serfail=" SERFAIL "+searchHashKey;
							String serFailmsg= String.format("%04d", serfail.length()+4)+serfail;
							
							Socket sock = new Socket();
									
							try {
								sock.connect(new InetSocketAddress(sIP,sPort));//, 100);
								DataOutputStream out = new DataOutputStream(sock.getOutputStream());
								out.write(serFailmsg.getBytes());
								
								}catch (SocketTimeoutException e) {
									// TODO: handle exception
									System.out.println("Status: SerFail message sending failed due to connection timeout ");
									e.printStackTrace();
								}
								sock.close();	
							
						}
						
					}
					
				}
		
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void serOK(String serokmsg) {
		
		serok= serok+1;
		System.out.println("Nunber of Searches found #"+serok);
		
		//System.out.println("Status: Search successful "+serokmsg+" from node "+server.connection_socket.getRemoteSocketAddress());
		
		String[] serOKMSG=serokmsg.split(" ");
		

		Timestamp genTime = new Timestamp(System.currentTimeMillis());
		long latancy= genTime.getTime()-Long.parseLong(serOKMSG[serOKMSG.length-1]);
		int hops= Integer.parseInt(serOKMSG[serOKMSG.length-2]);
		
		if(Integer.parseInt(serOKMSG[2])>0) {
			
			serokFound=serokFound+1;
			ArrayList<String>peers= new ArrayList<String>();
			
			for(int i=4;i<serOKMSG.length-2;i=i+3) {
				peers.add(serOKMSG[i]+":"+serOKMSG[i+1]);
			}
			
			System.out.println();
			System.out.println("Number of the Searches completed #"+servercmds.serok);
			System.out.println("Status: Search completed and successfully and found the peers responsible for file "+serOKMSG[6].replaceAll("_", " ")+ " and the file key is located at "+server.connection_socket.getRemoteSocketAddress() );
			System.out.println("The peers responsible are"+ peers);
			System.out.println("With Latancy is "+Math.abs(latancy)+"ms"+" in "+hops+" hops");
			System.out.println();
			
			
			String data="Hops:"+"\t"+hops+"\t"+"Latancy:"+"\t"+Math.abs(latancy)+"\t"+"Filename:"+"\t"+serOKMSG[6].replaceAll("_", " ")
					+"\t"+"Filekey Found at:"+"\t"+server.connection_socket.getRemoteSocketAddress()+"\t"+"File is located at:"+"\t"+peers+"\n";
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
			
		}else {
			
			serokNotFound=serokNotFound+1;
			System.out.println();
			System.out.println("Number of the Searches completed #"+servercmds.serok);
			System.out.println("Status: Search completed but file not found for key "+serOKMSG[3]+ " and the node responsible for this file key is "+server.connection_socket.getRemoteSocketAddress().toString().split(":")[0] );
			System.out.println("With Latancy is "+Math.abs(latancy)+"ms"+" in "+hops+" hops");
			System.out.println();
			
			String data="Hops:"+"\t"+hops+"\t"+"Latancy:"+"\t"+Math.abs(latancy)+"\t"+"Search:"+"\t"+serOKMSG[3]+
					"\t"+"Filekey is supposed to be at:"+"\t"+server.connection_socket.getRemoteSocketAddress()+"\n";
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
		
	System.out.println("Total number of quries:  failed: #" +serFail+" Completed: #"+serok );
	System.out.println("Total number of quries generated #"+clientcmds.generatedQueries);
	System.out.println("Total number of searchkeys: Found: #"+serokFound+ " Not Found: #"+serokNotFound);
	System.out.println();
		
	}

	public void giveaddKeys(String in_req) {
		
		// // adding the keys to the key table
		
		int noKeys= Integer.parseInt(in_req.split(" ")[2]);
		if(noKeys>0) 
		{
			int offset=4;
			for(int b=0;b<noKeys;b++) 
				{
					ArrayList<String> keyinfo= new ArrayList<String>();
					String  keysock = in_req.split(" ")[offset]+":"+in_req.split(" ")[offset+1];
					int key= Integer.parseInt(in_req.split(" ")[offset+2]);
					String filename = in_req.split(" ")[offset+3].replaceAll("_", " ");
					
					if(clientcmds.Keytable.containsKey(key)) {
						keyinfo=clientcmds.Keytable.get(key);
						if(!keyinfo.contains(keysock)) 
						{keyinfo.add(keysock);	}
						}else {
							
							keyinfo.add(filename);
							keyinfo.add(keysock);
							clientcmds.Keytable.put(key, keyinfo);
						}
					offset= offset+4;
				}
			System.out.println("Status: Successfully added keys from predecessor");
			servercmds.predecessorkey=0;
			
			try {
				
				String giveokmsg= "0015 GIVEKYOK 0";
				DataOutputStream out = new DataOutputStream(server.connection_socket.getOutputStream());
				out.write(giveokmsg.getBytes());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}else {
			System.out.println("Status: No keys to add from predecessor");
		try {			
				String giveokmsg= "0015 GIVEKYOK 0";
				DataOutputStream out = new DataOutputStream(server.connection_socket.getOutputStream());
				out.write(giveokmsg.getBytes());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}
	
	public void serchfailed(String serfailmsg) {
		// TODO Auto-generated method stub
		
		serFail=serFail+1;
		System.out.println("Failed Search: #"+serFail);
		System.out.println("Status: Search Failed, unbale to locate the peer responsable for "+serfailmsg.split(" ")[2]);
		
	}

	
}