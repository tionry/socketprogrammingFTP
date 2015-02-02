/*
 * Author by Qiao
 * 2014 -11-14
 * Any Questions are Welcome
 */
package servercom;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Threads  implements Runnable{
	
	Socket socket,portsocket,pasvsocket;
	Boolean login, regis,quit,portflag,pasvflag,renameflag;
	//localdirectory is the root directory,sharedirectory is sharing directory for now
	String ip,pasvip,portip,localip,sharedirectory,localdirectory,renamefile;
	int pasvport,portport,fileclientport;
	BufferedReader in;
	PrintWriter out;
	ServerSocket pasvserver;
	
	//Thread
	public Threads(Socket s,String sharepath){
		socket = s;
		login = false;
		regis = false;
		pasvflag = false;
		portflag = false;
		renameflag = false;
		ip = socket.getInetAddress().toString();
		pasvport = 20;
		//get information from client by socket
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
			}
		}
		try {
			out = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
			}
		}
		out.println("220 "+ip+" FTP server ready.");
		out.flush();
		quit = false;
		if(sharepath != null){
			sharedirectory = sharepath;
			localdirectory = sharepath;
		}
		else{
			sharedirectory = "/home/qiaocy/ftp_shareserver/";
			localdirectory = "/home/qiaocy/ftp_shareserver/";			
		}
	}
	
	//CDUP
	public void CDUP(){
		if(sharedirectory .equals(localdirectory)||(sharedirectory + File.separator).equals(localdirectory)){
			out.println("550 permission denied");
			out.flush();
			return;
		}
		else{
			cdup(sharedirectory);			
			out.println("250 CWD command successful.");
			out.flush();
		}
	}
	public void cdup(String str){
		int i = 0,pos = 0;
		while(true){
			if(str.charAt(i)  == '/'){
				pos = i;
			}
			i++;
			if(i == str.length())	break;
		}
		sharedirectory	= str.substring(0,pos);
	}
	//CWD
	public void CWD(String str){
		String filepath = sharedirectory + str;
		try{
			FileInputStream dis = new FileInputStream(filepath);
			dis.close();
		}catch(Exception e){
			if(e.getMessage().equals(filepath+" (是一个目录)")){
				out.println("250 CWD command successful");
				out.flush();
				sharedirectory = filepath + File.separator;
				return;
			}
			out.println("550 "+ str +": No such file or directory.");
			out.flush();
		}
	}
	public void DELE(String str){
		String filepath = sharedirectory + str;
		File file = null;
		try{
			FileInputStream dis = new FileInputStream(filepath);
			file = new File(filepath);
			file.delete();
			out.println("250 DELE "+ str + " successfully");
			out.flush();
			dis.close();
		}catch(Exception e){
			if(e.getMessage().equals(filepath+" (是一个目录)")){
				out.println("250 DELE "+ str + " failed, cannot delete directory");
				out.flush();
				return;
			}
			out.println("550 "+ str +": No such file or directory.");
			out.flush();
		}
	}
	
	//LIST
	public void LIST(File f){
		File[] t = f.listFiles();
        for(int i=0;i<t.length;i++){
            if(t[i].isDirectory()){
                out.println("226-"+t[i].getName()+"   Directory");
            }
            else{
                out.println("226-"+t[i].getName()+"    File");
            }
        }
	}
	
	//MKD
	public void MKD(String str){
		String filepath = sharedirectory + str;
		File f = new File(filepath);
		if(f.mkdirs()){
			out.println("250 create a new directory "+ str + " successfull");
			out.flush();
		}
		else{
			out.println("451 "+ str +"fail to create");
			out.flush();
		}
	}
	
	//RNFR
	public void RNFR(String str){
		String filepath = sharedirectory + str;
		File file = null;
		try{
			FileInputStream dis = new FileInputStream(filepath);
			file = new File(filepath);
			renamefile = filepath;
			out.println("250 prepare to rename "+ str + ".");
			out.flush();
			dis.close();
		}catch(Exception e){
			if(e.getMessage().equals(filepath+" (是一个目录)")){
				renamefile = filepath;
				out.println("250 prepare to rename "+ str + ".");
				out.flush();
				return;
			}
			out.println("550 "+ str +": No such file or directory.");
			out.flush();
			return;
		}
		renameflag = true;
	}
	
	public void RNTO(String str){
		String filepath = sharedirectory + str;
		File oldfile = new File(renamefile);
		File newfile = new File(filepath);
		oldfile.renameTo(newfile);
		out.println("250 rename successfully");
		out.flush();
	}
	//deleteFile and deleteDirectory functions refer to http://kxjhlele.iteye.com/blog/323657
	public boolean deleteFile(String sPath) {  
	   Boolean flag;
	   File file;
		flag = false;  
	    file = new File(sPath);  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	} 
	
	public boolean deleteDirectory(String sPath) {  
	     Boolean flag;
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    
	    if (!dirFile.exists() || !dirFile.isDirectory()) { 	    	
	        return false;  
	    }  
	    flag = true;  
	     
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } 
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}
	
	//RMD
	public void RMD(String str){		
		String filepath = sharedirectory + str;
		try{
			FileInputStream dis = new FileInputStream(filepath);
			dis.close();
		}catch(Exception e){
			if(e.getMessage().equals(filepath+" (是一个目录)")){
				out.println("250 RWD command successful");
				out.flush();
				deleteDirectory(filepath);
				return;
			}
			out.println("550 "+ str +": No such directory.");
			out.flush();
		}
		
	}
	//email pattern judge
	public Boolean LogIn(String str){
		if(login) return true;
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(str.substring(5));
		if(matcher.matches()){
			return true;
		}
		return false;
	}
	
	//upload file
	public void putfile(String filename) {	
		try{
			InputStream dis = null;					
			if(portflag){
				dis = portsocket.getInputStream();
			}	
			if(pasvflag){
				dis = pasvsocket.getInputStream();	
			}
			
			String savePath = "/home/qiaocy/ftp_shareserver";
		    savePath =  savePath+File.separator + getfilename(filename);   
		    BufferedOutputStream fileOut =   new BufferedOutputStream( new FileOutputStream(savePath)); 
		    int bufferSize = 8192;   	    
		    byte[] buf = new byte[bufferSize];   
		    out.println("150 Opening BINARY mode data connection for "+ getfilename(filename));
			out.flush();
		   
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
			dis.close();
		    fileOut.close();
			if(pasvflag){
				pasvsocket.close();
				pasvserver.close();
				pasvflag = false;
			}
			if(portflag){
				portsocket.close();
				portflag = false;
			}
		    out.println("226 Transfer complete.");
			out.flush();
		}catch(Exception e){
		try {
			pasvsocket.close();			
			portsocket.close();
			pasvflag = false;
			portflag = false;
			pasvserver.close();
			} catch (IOException e1) {
			}
		}
	}
	
	//get file name 
	public  String  getfilename(String str){
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
	
	//PWD
	public void PWD(){
		out.println("257 "+sharedirectory);
		out.flush();
	}
	
	//download file
	public void getfile(String filename){
		try{
			String filePath = sharedirectory+filename;			
			OutputStream dos = null;
			if(portflag){				
				dos = portsocket.getOutputStream();
			}
	
			if(pasvflag){
				dos = pasvsocket.getOutputStream();	
			}
			FileInputStream dis;
			try {
				dis = new FileInputStream(filePath);
				out.println("150 Opening BINARY mode data connection for "+ getfilename(filename)+".");
				out.flush();
			} catch (FileNotFoundException e) {
				out.println("550 Can't open " +  getfilename(filename) + ": No such file or directory");
				out.flush();
				return;
			}
			int BUFSIZE = 8192;
			byte [] buf = new byte[BUFSIZE];
			while(true){
				int read = 0;
				if(dis != null){
					read = dis.read(buf);
				}else{
					break;
				}				
				if (read == -1){
					break;
				}				
				dos.write(buf, 0, read);
			}
			dos.close();
			dos.flush();
			dis.close();
			out.println("226 Transfer complete.");
			out.flush();	
			pasvflag = false;
			if(pasvflag){
				pasvsocket.close();
				pasvserver.close();
				pasvflag = false;
			}
			if(portflag){
				portsocket.close();
				portflag = false;
			}
		}catch(Exception e){
			try {
				pasvsocket.close();			
				portsocket.close();
				pasvflag = false;
				portflag = false;
				pasvserver.close();
			} catch (IOException e1) {
				
			}
		}
	}
	
	// get random port number under passive mode 
	public int randomport(){
		int max=65535;
        int min=20000;
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
		return s;
	}
	
	//SYST
	public void SYST(){
		out.println("215 UNIX Type: L8");
		out.flush();
	}
	
	//TYPE I
	public void TYPE(String str){
		if(str.equals(" I")){
			out.println("200 Type set to I.");
			out.flush();
		}
		else{
			out.println("500 Error Type");
			out.flush();
		}
	}
	
	//PORT
	public void PORT(String str){
		try{		
			StringTokenizer st = new StringTokenizer(str,",");
			String[] strArray = new String[st.countTokens()];
			int i=0;
			char[] tem;
			while(st.hasMoreTokens()){			
				strArray[i] = st.nextToken();
				tem = strArray[i].toCharArray();
				for(int j = 0;j < strArray[i].length();j++){
					if(!Character.isDigit(tem[j])){
						out.println("530 wrong format");
						out.flush();
						return;
					}
				}
				i++;
			}
			if(i != 6){			
				out.println("530 wrong format");
				out.flush();
			}
			else{
				portip = strArray[0] + "."+strArray[1]+"."+strArray[2] +"."+strArray[3]  ;
				portport = Integer.parseInt(strArray[4]) * 256 + Integer.parseInt(strArray[5]); 
				portport = 20;				
				if(pasvflag){		
					pasvsocket.close();
					pasvserver.close();
					pasvflag = false;
					}
				if(portflag){
					portsocket.close();
					portflag = false;
				}
				try{
					portsocket = new Socket(portip,portport);					
				}catch(Exception e){
					out.println("425 fail to start port mode");
					portflag = false;
					out.flush();
					portsocket.close();
					return;
				}
				out.println("200 PORT command successful.");
				out.flush();
				portflag = true;
			}
		}catch(Exception e){
			try {
				pasvsocket.close();			
				portsocket.close();
				pasvflag = false;
				portflag = false;
				pasvserver.close();
			} catch (IOException e1) {
			}
		}
	}
	
	//PASV
	public void PASV() {		
		if(pasvflag){
			try{
				pasvserver.close();				
			}catch(Exception e){
				
			}
		}
		pasvport = randomport();
			try{
				pasvserver = new ServerSocket(pasvport);				
			}catch(Exception e){
				
			}		
		localip = socket.getLocalAddress().toString();
		StringTokenizer st = new StringTokenizer(localip.substring(1),".");
		String[] strArray = new String[st.countTokens()];
		int i=0;
		while(st.hasMoreTokens()){			
			strArray[i] = st.nextToken();
			i++;
		}	
		out.println("227 Entering Passive Mode ("+strArray[0]+","+strArray[1]+","+strArray[2]+","+strArray[3]+","+pasvport/256+","+pasvport%256 + ")");
		out.flush();
		
		try {
			pasvsocket = pasvserver.accept();
		} catch (IOException e) {
			return;
		}	
		pasvflag = true;
	}
		
	//QUIT
	public void QUIT(){
		quit = true;
		out.println("221-Thank you for using the FTP service");
		out.println("221 GOOD BYE");
		out.flush();
		try {
			pasvsocket.close();			
			portsocket.close();
			pasvflag = false;
			portflag = false;
			pasvserver.close();
		} catch (IOException e1) {
		}	
	}
	
	public void run(){
		try{
			String str;
			while(true){
				str = in.readLine();				
				String str_temp = "";
				
				//command pipeline string length 4				
				if(str.length() >= 4){					
					str_temp = str.substring(0, 4);
				}
				
				//quit server
				if(str.equals("QUIT")||str.equals("ABOR")){
					QUIT();
					break;
				}
				
				//Register
				if(! regis){
					if(str_temp.equals("USER")){
						if(str.substring(5).equals("anonymous")){
							regis = true;
							out.println("331 Guest login ok, send your complete e-mail address as password.");
							out.flush();
						}
						else{
							out.println("530 wrong name format!");
							out.flush();
						}
					}
					else{
						out.println("530 You aren't logged in");
						out.flush();
					}
					continue;
				}
				
				//login
				if(! login){
					if(str_temp.equals("PASS")&&str.length()>4){	
						if(LogIn(str)){
							login = true;
						}
						if(regis && login){
							out.println("230-Welcom to Seven's ftp!");
							out.println("230 log in success!");
							out.flush();						
						}
						else{
							out.println("530 wrong password");
							out.flush();						
						}
					}
					else{
						out.println("530 wrong password");
						out.flush();						
					}
					continue;
				}
				
				//PWD
				if(str.equals("PWD")){
					PWD();
					continue;
				}
				
				//CWD
				if(str_temp.equals("CWD ")){
					CWD(str.substring(4));
					continue;
				}
											
				//CDUP
				if(str.equals("CDUP")){
					CDUP();
					continue;
				}
				
				
				//passive mode
				if(str.equals("PASV")){
					try{
						PASV();
						continue;
					}catch (Throwable e) {
					}
				}
				
				//system type
				if(str.equals("SYST")){
					SYST();
					continue;
				}				
				
				//LIST
				if(str.equals("LIST")){
					File f = new File(sharedirectory);
					LIST(f);		
					out.println("226 LIST success");
					out.flush();
					continue;
				}
				
				//download file
				if(str_temp.equals("RETR")){
					if(str.length() < 6){
						out.println("500 syntax error");
						out.flush();
						continue;
					}
					if(portflag ||pasvflag){
						getfile(str.substring(5));
					}				
					else{
						out.println("425 No data connection");
						out.flush();
					}
					pasvflag = false;
					portflag = false;
					continue;
				}
				
				//upload file
				if(str_temp.equals("STOR")){
					if(str.length() < 6){
						out.println("500 syntax error");
						out.flush();
						continue;
					}
					if(portflag ||pasvflag){
						putfile(str.substring(5));
					}				
					else{
						out.println("425 No data connection");
						out.flush();
					}
					pasvflag = false;
					portflag = false;
					continue;
				}
				
				//command pipeline string length longer than 4
				if(str.length() > 4){
					if(str_temp.equals("TYPE")){
						TYPE(str.substring(4));
						continue;
					}
					if(str_temp.equals("PORT")){
						PORT(str.substring(5));
						continue;
					}			
					//DELE
					if(str_temp.equals("DELE")){
						DELE(str.substring(5));
						continue;
					}
					//RMD 
					if(str_temp.equals("RMD ")){
						RMD(str.substring(4));
						continue;
					}
					//RNFR
					if(str_temp.equals("RNFR")){
						RNFR(str.substring(5));						
						continue;
					}
					//RNTO
					if(str_temp.equals("RNTO")){
						if(!renameflag){
							out.println("550 choose a file to rename first");
							out.flush();
							continue;
						}
						RNTO(str.substring(5));
						renameflag = false;
						continue;
					}
					//MKD
					if(str_temp.equals("MKD ")){
						MKD(str.substring(4));
						continue;
					}
				}
				
				//default handles
				switch(str_temp){
				case "USER":
				case "PASS":
					out.println("530 you already log in!");
					out.flush();
					break;								
						
					default:
						out.println("500 syntax error");
						out.flush();
				}
				if(quit)	break;
			}
			in.close();
			out.close();
			socket.close();
			try {
				pasvsocket.close();
				pasvserver.close();
			} catch (IOException e) {
				
			}
			try {
				portsocket.close();
			} catch (IOException e) {				
				
			}			
		}catch(Throwable e){
			
		}
	}
}
