package com.example.typeracers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Room {
    private final String roomId;
    private final Map<WebSocketServer.ClientHandler, Boolean> clients = Collections.synchronizedMap(new HashMap<>());
    private final Map<WebSocketServer.ClientHandler, Boolean> finishedClients = Collections.synchronizedMap(new HashMap<>());
    private final String textContent;
    private boolean gameStarted = false;
    

    public Room(String roomId, String textContent) {
        this.roomId = roomId;
        this.textContent = textContent;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTextContent() {
        return textContent;
    }

    public void addClient(WebSocketServer.ClientHandler client) throws IOException {
        if (gameStarted) {
            client.sendMessage("ROOM_IN_GAME");
            return;
        }
        clients.put(client, false);
        finishedClients.put(client, false);
        client.sendMessage("TEXT:" + textContent);
    }

    public void removeClient(WebSocketServer.ClientHandler client) {
        clients.remove(client);
        finishedClients.remove(client);
    }

    public void setClientReady(WebSocketServer.ClientHandler client) {
        clients.put(client, true);
    }

    public boolean checkAllClientsReady() {
        for (boolean b : clients.values()) {
            if (!b) {
                return false;
            }
        }
        startGame();
        return true;
    }

    public void setClientFinished(WebSocketServer.ClientHandler client) {
        finishedClients.put(client, true);
    }

    public boolean areAllClientsFinished() {
        for (Boolean finished : finishedClients.values()) {
            if (!finished) {
                return false;
            }
        }
        return true;
    }

    public void startGame() {
        gameStarted = true;
    }

    public void resetGame() {
        gameStarted = false;
        for (WebSocketServer.ClientHandler client : clients.keySet()) {
            clients.put(client, false);
            finishedClients.put(client, false);
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
    
    

    public void broadcastProgress(String progress) throws IOException {
        broadcastMessage("PROGRESS:" + progress);
    }

    public void broadcastMessage(String message) throws IOException {
        synchronized (clients) {
            for (WebSocketServer.ClientHandler client : clients.keySet()) {
                if (!client.socket.isClosed()) {
                    client.sendMessage(message);
                }
            }
        }
    }
}


