/*
 * Author by Qiao
 * 2014 -11-14
 * Any Questions are Welcome
 */
package servercom;

import java.io.File;
import java.net.*;

public class Server{	
	public static void main(String[] args){
		int j;
	    String  root_path = null;
	    for(j=0; j< args.length; j++) {
	        try{
		    	if(args[j].substring(0, 2).equals("-d")) {
		            root_path = args[j + 1] + File.separator;	            
		        }
	        }catch(Exception e){
	        	System.out.print("-d command failed");
	        }
	    }
		int MaxClientNum = 100;
		try{
			ServerSocket server = new ServerSocket(21);
			for(int i =0; i< MaxClientNum;i++){
				Socket socket = server.accept();
				//create a thread connected to client
				try{
					Thread t = new Thread(new Threads(socket,root_path));
					t.start();
				} catch (Throwable e) {
				}				
			}
			server.close();
		}catch(Exception e){
			System.out.println("Error"+e);
		}
	}
}