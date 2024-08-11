package com.example.typeracers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketServer {
    private static final int PORT = 9874;
    private static final Map<String, Room> rooms = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Queue<ClientHandler>> waitingList = new ConcurrentHashMap<>();
    public static final Map<String, String> textForRoom = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("WebSocket server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String roomId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String line = in.readLine();

                if (line != null && line.startsWith("GET")) {
                    if (line.contains("Upgrade: websocket")) {
                        handleHandshake(line);
                    } else {
                        handleHttpRequest(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                try {
                    if (roomId != null) {
                        Room room = rooms.get(roomId);
                        if (room != null) {
                            room.removeClient(this);
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleHandshake(String request) throws IOException, NoSuchAlgorithmException {
            String key = null;
            while (!(request = in.readLine()).isEmpty()) {
                if (request.startsWith("Sec-WebSocket-Key: ")) {
                    key = request.split(" ")[1];
                }
                if (request.startsWith("GET /typeracer/")) {
                    roomId = request.split(" ")[1].split("/")[2];
                }
            }

            if (key != null) {
                String acceptKey = generateAcceptKey(key);
                out.print("HTTP/1.1 101 Switching Protocols\r\n");
                out.print("Upgrade: websocket\r\n");
                out.print("Connection: Upgrade\r\n");
                out.print("Sec-WebSocket-Accept: " + acceptKey + "\r\n");
                out.print("\r\n");
                out.flush();

                String text = textForRoom.getOrDefault(roomId, GenerateText.get());
                Room room = rooms.computeIfAbsent(roomId, id -> new Room(id, text));
                if (room.isGameStarted()) {
                    waitingList.computeIfAbsent(roomId, k -> new LinkedBlockingQueue<>()).add(this);
                    sendMessage("WAIT");
                } else {
                    room.addClient(this);
                }
            }
        }

        private void handleHttpRequest(String request) throws IOException {
            String resource = request.split(" ")[1];
            if (resource.equals("/")) {
                resource = "/index.html"; // default page
            }
            Path filePath = Paths.get("webapp", resource);
            if (Files.exists(filePath)) {
                String mimeType = Files.probeContentType(filePath);
                byte[] fileContent = Files.readAllBytes(filePath);
                sendHttpResponse(200, mimeType, fileContent);
            } else {
                sendHttpResponse(404, "text/plain", "404 Not Found".getBytes());
            }
        }

        private void sendHttpResponse(int statusCode, String contentType, byte[] content) throws IOException {
            out.print("HTTP/1.1 " + statusCode + "\r\n");
            out.print("Content-Type: " + contentType + "\r\n");
            out.print("Content-Length: " + content.length + "\r\n");
            out.print("\r\n");
            out.flush();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(content);
            outputStream.flush();
        }

        private String generateAcceptKey(String key) throws IOException, NoSuchAlgorithmException {
            key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            return Base64.getEncoder().encodeToString(sha1.digest(key.getBytes("UTF-8")));
        }

        public void sendMessage(String message) {
            out.print(encodeMessage(message));
            out.flush();
        }

        private String encodeMessage(String message) {
            byte[] rawData = message.getBytes();
            int frameCount = 0;
            byte[] frame = new byte[10];
            frame[0] = (byte) 129;
            if (rawData.length <= 125) {
                frame[1] = (byte) rawData.length;
                frameCount = 2;
            } else if (rawData.length >= 126 && rawData.length <= 65535) {
                frame[1] = (byte) 126;
                int len = rawData.length;
                frame[2] = (byte) ((len >> 8) & (byte) 255);
                frame[3] = (byte) (len & (byte) 255);
                frameCount = 4;
            }

            int bLength = frameCount + rawData.length;
            byte[] reply = new byte[bLength];
            int bLim = 0;
            for (int i = 0; i < frameCount; i++) {
                reply[bLim] = frame[i];
                bLim++;
            }
            for (int i = 0; i < rawData.length; i++) {
                reply[bLim] = rawData[i];
                bLim++;
            }

            return new String(reply);
        }
    }
}
