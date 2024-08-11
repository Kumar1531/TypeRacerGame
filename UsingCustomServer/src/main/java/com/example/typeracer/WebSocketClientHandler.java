package com.example.typeracer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class WebSocketClientHandler {
	private final Socket clientSocket;
	private Room room;
	private String clientId;
	public Map<String, Queue<WebSocketClientHandler>> waiting_list = new HashMap<>();

	public WebSocketClientHandler(Socket clientSocket, Room room, String clientId) {
		this.clientSocket = clientSocket;
		this.room = room;
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientSocket.getRemoteSocketAddress().toString();
	}

	public void run() {
		try (InputStream input = clientSocket.getInputStream();
				OutputStream output = clientSocket.getOutputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			handleWebSocketCommunication(input, output);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (room != null) {
				room.removeClient(this);
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleWebSocketCommunication(InputStream input, OutputStream output) throws IOException {
		while (true) {
			String message = readMessage(input);
			if (message != null) {
				handleMessage(message);
			} else {
				break;
			}
		}
	}

	private String readMessage(InputStream input) throws IOException {
		int b1 = input.read();
		if (b1 == -1) {
			return null;
		}

		int opcode = b1 & 0x0F;
		if (opcode == 8) {
			return null;
		}

		int b2 = input.read();
		int payloadLength = b2 & 0x7F;
		if (payloadLength == 126) {
			payloadLength = (input.read() << 8) | input.read();
		} else if (payloadLength == 127) {

		}

		byte[] maskingKey = new byte[4];
		input.read(maskingKey);

		byte[] payload = new byte[payloadLength];
		input.read(payload);

		for (int i = 0; i < payload.length; i++) {
			payload[i] ^= maskingKey[i % 4];
		}

		return new String(payload, StandardCharsets.UTF_8);
	}

	private void handleMessage(String message) throws IOException {
		if (message.startsWith("JOIN:")) {
			String roomId = message.substring(5);
			room = TyperacerEndpoint.rooms.computeIfAbsent(roomId, Room::new);
			clientId = getClientId();
			room.addClient(this);
			broadcast("JOINED:" + clientId);
			System.out.println("Inside handle message JOIN");
		} else if (message.startsWith("READY")) {
			room.setClientReady(clientId);
			if (room.checkAllClientsReady()) {
				broadcast("START");
			}
		} else if (message.startsWith("RESULT")) {
			room.setClientFinished(clientId);
			String name = GetName.get(message);
			InsertDAO in = new InsertDAO();
			in.addHistory(name, message);
			broadcast(message);
			if (room.areAllClientsFinished()) {
				room.resetGame();
				broadcast("REMATCH");
				processWaitingList(room.roomId);
			}
		} else if (message.startsWith("PROGRESS:")) {
			broadcast(message);
		} else {
			broadcast(clientId + ": " + message);
		}
	}

	public void processWaitingList(String roomId) throws IOException {
		Queue<WebSocketClientHandler> queue = waiting_list.get(roomId);
		Room room = TyperacerEndpoint.rooms.get(roomId);
		if (queue != null && room != null) {
			while (!queue.isEmpty()) {
				WebSocketClientHandler client = queue.poll();
				room.addClient(client);
				System.out.println("Yes process is called");
				sendMessage("ENTER");
			}
		}
	}

	public void broadcast(String message) throws IOException {
		room.broadcastMessage(message);
	}

	public void sendMessage(String message) throws IOException {
		OutputStream output = clientSocket.getOutputStream();
		output.write(encodeMessage(message));
		output.flush();
	}

	private byte[] encodeMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		int frameLength = messageBytes.length + 2;
		byte[] frame = new byte[frameLength];

		frame[0] = (byte) 0x81;
		frame[1] = (byte) messageBytes.length;
		System.arraycopy(messageBytes, 0, frame, 2, messageBytes.length);

		return frame;
	}
}
