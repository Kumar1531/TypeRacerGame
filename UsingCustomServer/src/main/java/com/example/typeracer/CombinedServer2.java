package com.example.typeracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class CombinedServer2 {
	private static final int PORT = 8000;
	private static final String WEB_ROOT = "M:\\Kumar Work Area\\JAVA\\UsingCustomServer\\src\\main\\webapp";
	public static final Map<String, Room> rooms = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				new Thread(new ClientHandler(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private Room room;
		private String clientId;

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try (InputStream input = clientSocket.getInputStream();
					OutputStream output = clientSocket.getOutputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

				String line;
				StringBuilder request = new StringBuilder();
				while (!(line = reader.readLine()).isEmpty()) {
					request.append(line).append("\r\n");
				}

				if (request.toString().contains("Upgrade: websocket")) {
					handleWebSocketHandshake(request.toString(), output);
					new WebSocketClientHandler(clientSocket, room, clientId).run();
				} else {
					handleHttpRequest(request.toString(), output);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleHttpRequest(String request, OutputStream out) throws IOException {
			StringTokenizer tokens = new StringTokenizer(request);
			String method = tokens.nextToken();
			String requestedFile = tokens.nextToken();

			if (!"GET".equals(method)) {
				sendHttpResponse(out, 405, "Method Not Allowed", "Only GET method is supported.");
				return;
			}

			if (requestedFile.endsWith("/")) {
				requestedFile += "login.html";
			}
			File file = new File(WEB_ROOT, URLDecoder.decode(requestedFile, "UTF-8")).getCanonicalFile();

			if (!file.exists() || !file.getPath().startsWith(new File(WEB_ROOT).getCanonicalPath())) {
				sendHttpResponse(out, 404, "Not Found", "The requested file was not found on this server.");
				return;
			}

			String mimeType = Files.probeContentType(file.toPath());
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}

			byte[] fileContent = Files.readAllBytes(file.toPath());
			sendHttpResponse(out, 200, "OK", fileContent, mimeType);	
		}

		private void sendHttpResponse(OutputStream out, int statusCode, String statusText, String responseText)
				throws IOException {
			String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" + "Content-Type: text/plain\r\n"
					+ "Content-Length: " + responseText.length() + "\r\n" + "\r\n" + responseText;
			out.write(response.getBytes());
			out.flush();
		}

		private void sendHttpResponse(OutputStream out, int statusCode, String statusText, byte[] responseBody,
				String mimeType) throws IOException {
			String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" + "Content-Type: " + mimeType
					+ "\r\n" + "Content-Length: " + responseBody.length + "\r\n" + "\r\n";
			out.write(response.getBytes());
			out.write(responseBody);
			out.flush();
		}

		private void handleWebSocketHandshake(String request, OutputStream output) throws IOException {
			String webSocketKey = getWebSocketKey(request);
			String acceptKey = generateAcceptKey(webSocketKey);

			String response = "HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n"
					+ "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

			output.write(response.getBytes(StandardCharsets.UTF_8));
			output.flush();
		}

		private String getWebSocketKey(String request) {
			for (String line : request.split("\r\n")) {
				if (line.startsWith("Sec-WebSocket-Key: ")) {
					return line.substring(19);
				}
			}
			return null;
		}

		private String generateAcceptKey(String webSocketKey) throws IOException {
			try {
				String magicString = webSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				byte[] hash = md.digest(magicString.getBytes(StandardCharsets.UTF_8));
				return Base64.getEncoder().encodeToString(hash);
			} catch (Exception e) {
				throw new IOException("Failed to generate accept key", e);
			}
		}
	}
}
