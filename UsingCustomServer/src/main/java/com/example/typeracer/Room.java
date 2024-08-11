package com.example.typeracer;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Room {
    public final String roomId;
    private final Map<String, WebSocketClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Boolean> readyClients = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Boolean> finishedClients = Collections.synchronizedMap(new HashMap<>());
    private boolean gameStarted = false;
    private String textContent = null;

    public Room(String roomId) {
        this.roomId = roomId;
        textContent = GenerateText.get();
    }

    public void addClient(WebSocketClientHandler clientHandler) throws IOException {
        if (gameStarted) {
        	//WebSocketClientHandler.
            clientHandler.sendMessage("WAIT");
            return;
        }
        String clientId = clientHandler.getClientId();
        clients.put(clientId, clientHandler);
        readyClients.put(clientId, false);
        finishedClients.put(clientId, false);
        clientHandler.sendMessage("JOINED_ROOM:" + roomId);
        System.out.println("Client joined in "+roomId+" Client name "+clientId);
        
        clientHandler.sendMessage("TEXT:"+textContent);
    }

    public void removeClient(WebSocketClientHandler clientHandler) {
        String clientId = clientHandler.getClientId();
        clients.remove(clientId);
        readyClients.remove(clientId);
        finishedClients.remove(clientId);
    }

    public void setClientReady(String clientId) {
        readyClients.put(clientId, true);
    }

    public boolean checkAllClientsReady() {
        for (boolean ready : readyClients.values()) {
            if (!ready) {
                return false;
            }
        }
        startGame();
        return true;
    }

    public void setClientFinished(String clientId) {
        finishedClients.put(clientId, true);
    }

    public boolean areAllClientsFinished() {
        for (boolean finished : finishedClients.values()) {
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
        for (String clientId : clients.keySet()) {
            readyClients.put(clientId, false);
            finishedClients.put(clientId, false);
        }
    }

    public void broadcastMessage(String message) throws IOException {
        synchronized (clients) {
            for (WebSocketClientHandler clientHandler : clients.values()) {
                clientHandler.sendMessage(message);
            }
        }
    }
}

