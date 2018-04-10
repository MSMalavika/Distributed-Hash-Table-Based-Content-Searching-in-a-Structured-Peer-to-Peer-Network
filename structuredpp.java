import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class structuredpp {
	
	static String uname = "swathi";
	static int nodeKey=0;
	static String peerSockAdd=new String();

	public static void main(String[] args) {
		
		try{			
			String NP = args[0];
			String BSIP = args[1];
			String Boot_port =args[2];		
			
			if (args.length==3) {
				
				int Node_port = Integer.parseInt(NP);		
				InetAddress BS_ip = InetAddress.getByName(BSIP);		
				int BS_port = Integer.parseInt(Boot_port);
				
				if ((BS_port<5000 || BS_port>65535) || (Node_port < 5000 || Node_port > 65535))
					{
						System.out.println("Error: Port range, Range: 5000-65535");
						System.exit(1);
					}
				
				peerSockAdd= InetAddress.getLocalHost().getHostAddress()+":"+NP;
				// Generating key for this peer
					try
					{
						MessageDigest hashing = MessageDigest.getInstance("SHA-1");
						byte[] key = hashing.digest(peerSockAdd.getBytes());
						
						
						 String sub = String.format("%8s", Integer.toBinaryString(key[0] & 0xFF)).replace(' ', '0').substring(0,7) +
									String.format("%8s", Integer.toBinaryString(key[1] & 0xFF)).replace(' ', '0').substring(0,7);
						 
						 
						 nodeKey=Integer.parseInt(sub.trim(), 2);
																		
						//nodeKey=Byte.toUnsignedInt(key[0]);
						
					}catch(NoSuchAlgorithmException e) 
						{
							System.err.println("NoSuchAlgorithmException when hasing ");
							System.err.println(e);
						}
				
				
				// Server thread 					
					serverThread(Node_port);
				
				// Client thread				      
					clientThread(Node_port,BS_ip, BS_port);
					
			} else if(args.length==4) {
				
				String option = args[3];
				
				if(option.equals("h")) {
					System.out.println("help");
				} else {
					System.out.println(" Wrong commad try using help : unstructpp <Node port> <bootstrap ip> <bootstrap port> h");
				}
				
			}else {
				System.out.println("Oops something went wrong");
			}
				
			      
	}catch(ArrayIndexOutOfBoundsException e)
		{	System.err.println("Error: Missing arguments, See help for correct instructions  ");				
			System.out.println("Usage: unstructpp <Node port> <bootstrap ip> <bootstrap port> h");				
		}
	catch(NumberFormatException e)
		{
			System.err.println("Error: Non-integer port number");
		}
	catch(UnknownHostException e)
		{
			System.err.println("Error: Check IP Address format");
		}		

	}

	public int NP, Boot_port;
	public InetAddress BSIP;
	
	
	public static void clientThread(int NP, InetAddress BSIP, int Boot_port) {
		Thread T= (new Thread() {
        	public void run() {
        		
        		client clientMode = new client(NP,BSIP,Boot_port);        		
					clientMode.run();				
        			   
        		   }
        	
        });T.setDaemon(true);T.start();
	}
        
    public static void serverThread(int Nodeport) {
        (new Thread() {   
        	public void run(){
    			
        		server serverMode = new server(Nodeport);
        		serverMode.run();
    		}
        	
        }).start();
    }

   

}
