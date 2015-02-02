package com.github.herbix.udpdemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	public static void main(String[] args)  {
	try{	
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String ip = in.readLine();
		String str;

		DatagramSocket udpSocket = new DatagramSocket(20000);
		while(true){
			byte[] data = in.readLine().getBytes();
			str  = new String(data);
			if(str.equals("QUIT"))	break;
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setAddress(InetAddress.getByName(ip));
			packet.setPort(30000);
			udpSocket.send(packet);			
			try{
				int n = Integer.parseInt(str);
				if(n>=0&&n<=50){
					byte[] buffer = new byte[4096];
					DatagramPacket pack = new DatagramPacket(buffer, 4096);
					udpSocket.receive(pack);
					str =  new String(pack.getData(), pack.getOffset(), pack.getLength());		
					
					int fl = 0;
					int message_num = Integer.parseInt(str);
					while(fl < message_num){
						DatagramPacket packs = new DatagramPacket(buffer, 4096);
						udpSocket.receive(packs);
						str =  new String(packs.getData(), packs.getOffset(), packs.getLength());
						System.out.println(str);
						fl++;
					}
				}
			}
			catch(Exception e){
			}
			byte[] buff = new byte[4096];
			DatagramPacket pacs = new DatagramPacket(buff, 4096);
			udpSocket.receive(pacs);
			str =  new String(pacs.getData(), pacs.getOffset(), pacs.getLength());
			System.out.println(str);
		}
		udpSocket.close();		
	}	catch(Exception e){
		System.out.print(e.getMessage());
	}
	}
}
