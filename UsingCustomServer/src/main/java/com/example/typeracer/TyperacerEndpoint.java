package com.example.typeracer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TyperacerEndpoint {

	private static final int PORT = 8002;
	public static final Map<String, Room> rooms = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("WebSocket server started on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				//new Thread(new WebSocketClientHandler(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
