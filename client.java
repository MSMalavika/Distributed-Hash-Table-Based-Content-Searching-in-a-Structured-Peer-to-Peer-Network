import java.net.InetAddress;
import java.util.Scanner;

public class client {
	
	public int NP, Boot_port;
	public InetAddress BSIP;
	
	
	static clientcmds cmd = new clientcmds();
	
	 public client(int NP, InetAddress BSIP, int Boot_port)
	   {
	      this.NP = NP;
	      this.BSIP = BSIP;
	      this.Boot_port = Boot_port;
	     
	   }
	   
	 public void run()   {
		 
		 cmd.reg(BSIP, Boot_port, NP);
		 cmd.fingertable();
		 cmd.finUpdataddemsg(NP,0);
		 cmd.resToNode();
		 cmd.keyTable();
		 cmd.getKeys();
		 
		 while(true) {
			 
			Scanner scan = new Scanner(System.in);			
			 String option = scan.next();
			 
			 	switch(option) {
			 	
					 	case "exit":
							System.exit(1);										
							break;
							
					 	case "exitall":
					 		cmd.exitall(BSIP, Boot_port);
					 		break;
					 	
					 	case "details":
					 		cmd.details(NP);
					 		break;
					 		
					 	case "fingertable":
					 		cmd.fingertableDisplay();					 		
					 		break;					 	
					 		
					 	case "keytable":
					 		cmd.keytableDisplay();
					 		break;
					 		
					 	case "entries":	
					 		cmd.entries();
					 		break;
					 		
					 	case "findentrie":
					 		cmd.findentrie();					 		
					 		break;
					 		
					 	case "leave":
					 		cmd.unReg(BSIP, Boot_port, NP);
					 		break;
					 		
					 	case "join": 
					 		cmd.reg(BSIP, Boot_port, NP);
							cmd.fingertable();
							cmd.finUpdataddemsg(NP,0);
							cmd.resToNode();
							cmd.keyTable();
							cmd.getKeys();
							break;
							
					 	case "search":
					 		cmd.query(NP);
					 		break;
					 		
					 	case "findfile":
					 		cmd.findfile(NP);
							break;
							
					 	case "keytablecopy":
					 		cmd.keytablecopy();
					 		break;
					 		
						case "nodekeys":
					 		System.out.println(clientcmds.node_Keys);
					 		break;
					 		
						case "addentrie":
							System.out.println("Give the entrie name that is to be added in this node");
					 		String addentrie = System.console().readLine();					 		
					 		cmd.addentrie(addentrie);
					 		break;				 	
					 		
						case "delenterie":
							
							System.out.println("Give the entrie name that is to be deleted in this node");
					 		String delentrie = System.console().readLine();
					 		String delEntrie= new String(delentrie).replaceAll("\\p{C}", "");							
					 		cmd.delentrie(delEntrie);
					 		break;
					 		
						case "resAll":
							cmd.resAll();
							break;
						case "clear":
							System.out.print("\033[H\033[2J");
							break;
					 		
					 	default:
					 		System.out.println(option+" does not exist");
					 		System.out.println("Try one of the following:");
					 		System.out.println("delenterie");
					 		System.out.println("addentrie");
					 		System.out.println("leave");					 		
					 		System.out.println("search");
					 		System.out.println("join");
					 		System.out.println("findentrie");
					 		System.out.println("entries");
					 		System.out.println("keytable");
					 		System.out.println("fingertable");
					 		System.out.println("details");
					 		System.out.println("clear");
					 		System.out.println("exit");
					 		System.out.println("exitall");
					 		break;
			 	
			 	}
			 
		 }
		 
	 }

}
