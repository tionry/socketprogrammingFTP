/*
 * Author by Qiao
 * 2014 -11-14
 * Any Questions are Welcome
 */
package Clientcom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client{
	public static BufferedReader stdin;
	public static BufferedReader in;
	public static String serverip,pasvfileip,portfileserverip,serverdirectory,savedirectory;
	public static PrintWriter out;
	public static int pasvfileserverport,portfileserverport;
	public static Socket socket,pasvfilesocket,portfilesocket;
	public static ServerSocket portserver;
	public static Boolean portflag,receiveflag,sendflag,pasvflag,loginflag;
	
	//Send messages or commands to Server and download or upload files.
	public Client(String serverip,String serverport){
		try{
			try{
				socket = new Socket(serverip,Integer.parseInt(serverport));
			}catch(Exception e){
				System.out.println("fail to connect");
				return;
			}
			stdin = new BufferedReader(new InputStreamReader(System.in));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			
			String send_str,receive_str;
			receive_str = messageread();
			System.out.print(receive_str);
			portflag = false;
			receiveflag  = true;
			sendflag = true;
			pasvflag = false;
			portfileserverport = 20;
			//this is the server's root directory
			serverdirectory =  "/home/qiaocy/ftp_shareserver";
			//this is the client's root directory
			savedirectory = "/home/qiaocy/ftp_shareclient";
			loginflag = false;
			
			while(true){
				receiveflag  = true;
				sendflag = true;
				System.out.print("---> ");
				send_str = stdin.readLine();
				if(loginflag){
					//PWD
					if(send_str.equals("PWD")){
						if(sendflag){
							out.println(send_str);
							out.flush();			
						}
						if(receiveflag){
							receive_str = messageread();		
							System.out.print(receive_str);
						}	
						continue;
					}
					//CWD
					if(send_str.equals("CWD")){
						if(sendflag){
							out.println(send_str);
							out.flush();			
						}
						if(receiveflag){
							receive_str = messageread();		
							System.out.print(receive_str);
						}	
						continue;
					}
				}
				
				if(send_str.length()<4){
					if(sendflag){
						out.println(send_str);
						out.flush();			
					}
					if(receiveflag){
						receive_str = messageread();		
						System.out.print(receive_str);
					}	
					continue;
				}
				if(loginflag){
					//RETR
					if(send_str.length() > 4){
						if(send_str.substring(0, 4).equals("RETR") && (portflag || pasvflag)){
						    out.println(send_str);
							out.flush();										
							receive_str = messageread();
							System.out.print(receive_str);
							if(!receive_str.subSequence(0, 3).equals("150")){					
								continue;
							}
							getfile(send_str);
							receive_str = messageread();
							System.out.print(receive_str);
							pasvflag = false;
							portflag = false;
							}				
					}
					
					//STOR
					if(send_str.length() > 4){
						if(send_str.substring(0, 4).equals("STOR") &&  (portflag || pasvflag)){
							String filename = send_str.substring(5);
							String filePath = savedirectory+File.separator+filename;
							BufferedInputStream dis = null;
							try{
								dis = new BufferedInputStream(new FileInputStream(filePath));			
							}catch(Exception e){
								System.out.print("550 Can't open " +getfilename(filename)+ ' ' + e.getMessage()+'\n');
								continue;
							}
							dis.close();
							out.println(send_str);
							out.flush();	
							receive_str = messageread();
							System.out.print(receive_str);
							if(!receive_str.subSequence(0, 3).equals("150")){					
								continue;
							}
							putfile(send_str);	
							receive_str = messageread();
							System.out.print(receive_str);
							pasvflag = false;
						}
					}
					
					//PASV MODE
					if(send_str.equals("PASV")){
						portflag = false;
						out.println(send_str);
						out.flush();
						receive_str = messageread();
						receiveflag = false;
						sendflag = false;
						System.out.print(receive_str);
						pasvmode(receive_str);	
						pasvflag = true;
					}
					
					//PORT MODE
					if(send_str.subSequence(0, 4).equals("PORT") && send_str.length() > 4){
						pasvflag = false;
						out.println(send_str);
						out.flush();
						if(portflag){
							portfilesocket.close();
							portserver.close();
						}
						try{
							portserver = new ServerSocket(portfileserverport);				
						}catch(Exception e){
							System.out.print("500 "+e.getMessage());
							receive_str = messageread();		
							System.out.print(receive_str);
							break;
						}						
						receive_str = messageread();		
						System.out.print(receive_str);
						if(!receive_str.subSequence(0, 3).equals("200"))	continue;
						portfilesocket = portserver.accept();
						PORT(send_str.substring(5));
						portflag = true;				
						sendflag = false;
						receiveflag = false;
					}
				}
				if(sendflag){
					out.println(send_str);
					out.flush();			
				}
				if(receiveflag){
					receive_str = messageread();		
					System.out.print(receive_str);
				}			
				
				if(receive_str.subSequence(0, 3).equals("230")){
					loginflag = true;
				}
				
				//QUIT
				if(send_str.equals("QUIT")||send_str.equals("ABOR"))	break;
			}		
		}catch(Exception e){
			
		}
	}
	public static void main(String[] args){
		//new Client("localhost","8888");
		//new Client("ftp.ntu.edu.tw","21");
		//new Client("localhost","21");
		String ip;
		ip = null;
		stdin = new BufferedReader(new InputStreamReader(System.in));
		try{
			System.out.println("please input ip address");
			ip = stdin.readLine();
		}catch(Exception e){
			
		}
		try{
			new Client(ip,"21");
		}catch (Throwable e) {
			System.out.print("425 " +"Sorry,you are disconnected for some unproper operations");
		}
	}
	
	//read messages in socket connection throughput
	public static String messageread(){
		String message,meline;
		Boolean flag1;
		flag1 = false;
		message = "";
		try{
			while( !flag1){
				meline = in.readLine();
				if(judgeend(meline)){
					flag1 = true;
				}			
				message = message + meline + '\n';
			}
		}catch(Exception e){
			
		}
		return message;
	}
	
	//judge last message
	public static Boolean judgeend(String str){
		if(str.charAt(3) == ' ')	return true;
		return false;
	}
	
	//try to connect to Server in passive mode
	public static void pasvmode(String receive_str){
		try{
			if(pasvfilesocket != null){
				pasvfilesocket.close();			
			}
			if(portserver != null){
				portserver.close();
				portflag = false;
			}
			PASV(receive_str);
			pasvfilesocket = new Socket(pasvfileip.substring(1),pasvfileserverport);		
		}catch(Exception e){
			
		}
	}
	
	//upload file to Server
	public static void putfile(String send_str){
		try{
			String filename = send_str.substring(5);
			String filePath = savedirectory+File.separator+filename;
			DataOutputStream dos = null;
			BufferedInputStream dis = null;
			if(portflag){
				dos = new DataOutputStream(new BufferedOutputStream(portfilesocket.getOutputStream()));
			}
			if(pasvflag){			
				dos = new DataOutputStream(new BufferedOutputStream(pasvfilesocket.getOutputStream()));	
			}		
			try{
				dis = new BufferedInputStream(new FileInputStream(filePath));			
			}catch(Exception e){
				System.out.print("550 Can't open " +getfilename(filename)+ ' ' + e.getMessage() +'\n');
				dis.close();
				return;
			}
			int BUFSIZE = 8192;
			byte [] buf = new byte[BUFSIZE];
			
			while(true){
				int read = 0;
				if(dis != null){
					read = dis.read(buf);
				}else{
					System.out.println("no file founded!");
					break;
				}
				if (read == -1){
					break;
				}				
				dos.write(buf, 0, read);			
			}		
			sendflag = false;
			receiveflag = false;
			dis.close();
			dos.close();
			dos.flush();
			if(portflag){
				portflag = false;
				portfilesocket.close();
				portserver.close();
			}
			if(pasvflag){			
				pasvfilesocket.close();
			}
		}catch(Exception e){
			
		}
	}
	
	//download file from Server
	public static void getfile(String send_str) {
		try{
			InputStream dis = null;
			OutputStream dos = null;
			if(portflag){			   
				sendflag = false;
				receiveflag = false;			
				dis = new DataInputStream(new BufferedInputStream(portfilesocket.getInputStream()));
				dos = new DataOutputStream(new BufferedOutputStream(portfilesocket.getOutputStream()));
			}
    
			if(pasvflag){
				dis = pasvfilesocket.getInputStream();
				dos = pasvfilesocket.getOutputStream();	
			}
			int bufferSize = 8192;   
		    
		    byte[] buf = new byte[bufferSize];       
		    String savePath = "/home/qiaocy/ftp_shareclient";
		    savePath =  savePath+File.separator + getfilename(send_str.substring(5));   
		    BufferedOutputStream fileOut =   new BufferedOutputStream( new FileOutputStream(savePath));   

		    while (true) {
		        int read = 0;   
		        if (dis != null) {   
		            read = dis.read(buf);   
		        }          
		        if (read == -1) {   
		            break;   
		        }        
		        fileOut.write(buf, 0, read);
		    }
		    fileOut.close();
			sendflag = false;
			receiveflag = false;
		
			dis.close();
			dos.close();
			if(portflag){
				portflag = false;
				portfilesocket.close();
				portserver.close();
			}
			if(pasvflag){
				pasvflag = false;
				pasvfilesocket.close();
			}
		}catch(Exception e){
			
		}		
	}
	
	//PORT messages parsing
	public static void PORT(String str){
		StringTokenizer st = new StringTokenizer(str,",");
		String[] strArray = new String[st.countTokens()];
		int i=0;
		while(st.hasMoreTokens()){			
			strArray[i] = st.nextToken();			
			i++;
		}		
		portfileserverip = strArray[0] + "."+strArray[1]+"."+strArray[2] +"."+strArray[3]  ;
		portfileserverport = 20; 
		portflag = true;
	}
	
	//get file name from a string
	public static String  getfilename(String str){
		int i = 0,pos = 0;
		while(true){
			if(str.charAt(i)  == '/'){
				pos = i;
			}
			i++;
			if(i == str.length())	break;
		}
		return	str.substring(pos);
	}
	
	//PASV parse messages
	public static void PASV(String str){
		int te = 0;
		while(true){
			if(str.charAt(te) != '('){
				te++;
				continue;
			}
			break;
		}
		int te2 = te;
		while(true){
			if(str.charAt(te2) != ')'){
				te2++;
				continue;
			}
			break;
		}
		String temp = str.substring(te+1, te2);
		StringTokenizer st = new StringTokenizer(temp,",");
		String[] strArray = new String[st.countTokens()];
		int i=0;
		while(st.hasMoreTokens()){			
			strArray[i] = st.nextToken();			
			i++;
		}		
		pasvfileip = "/"+strArray[0] + "."+strArray[1]+"."+strArray[2] +"."+strArray[3]  ;
		pasvfileserverport = Integer.parseInt(strArray[4]) * 256 + Integer.parseInt(strArray[5]); 
		}
	//to be continued
}