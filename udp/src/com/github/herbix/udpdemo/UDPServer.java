package com.github.herbix.udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	public static void main(String[] args){
	try{	
		DatagramSocket udpSocket = new DatagramSocket(30000);
		byte[] buffer = new byte[4096];
		String[] str = new String[50000];
		
		
		DatagramPacket packet = new DatagramPacket(buffer, 4096);
		int i = 0;
		while(true) {
			udpSocket.receive(packet);
			String str_t = new String(packet.getData(), packet.getOffset(), packet.getLength());
			try{
				int l = Integer.parseInt(str_t);
				if(l >= 0 && l <= 50){
					int s  = 0;
					byte[] data = (i+"").getBytes();
					DatagramPacket pack = new DatagramPacket(data, data.length);
					pack.setAddress(packet.getAddress());
					pack.setPort(20000);
					udpSocket.send(pack);
					while(s < i){
						data = str[s + 1].getBytes();
						DatagramPacket packs = new DatagramPacket(data, data.length);
						packs.setAddress(packet.getAddress());
						packs.setPort(20000);
						udpSocket.send(packs);
						s++;
					}
					continue;
				}
				}catch(Exception e){
					
				}
			i++;
			str[i] = i + ": " + new String(packet.getData(), packet.getOffset(), packet.getLength());
			byte[] dat = str[i].getBytes();
			DatagramPacket packs = new DatagramPacket(dat, dat.length);
			packs.setAddress(packet.getAddress());
			packs.setPort(20000);
			udpSocket.send(packs);
			System.out.println(str[i]);
		}
	}catch(Exception e){
		System.out.print(e.getMessage());
	}
	}

}
