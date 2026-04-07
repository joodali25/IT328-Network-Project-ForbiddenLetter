
package com.mycompany.networkprojectphase1;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static ArrayList<NewClient> clients = new ArrayList<>();
    private static ArrayList<String> connectedPlayers = new ArrayList<>();
    private static ArrayList<String> waitingRoom = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9090);

        while (true) {
            System.out.println("Waiting for client connection");
            Socket client = serverSocket.accept();
            System.out.println("Connected to client");

            NewClient clientThread = new NewClient(client, clients);
            clients.add(clientThread);

            new Thread(clientThread).start();
        }
    }

    public static synchronized void addToWaitingRoom(String playerName) {
        if (!waitingRoom.contains(playerName)) {
            waitingRoom.add(playerName);
            System.out.println(playerName + " joined waiting room");
            updateAllClients();
        }
    }

    public static synchronized void addConnectedPlayer(String playerName) {
        if (!connectedPlayers.contains(playerName)) {
            connectedPlayers.add(playerName);
            System.out.println("CONNECTED: " + playerName);
            updateAllClients();
        }
    }

    public static synchronized boolean isNameTaken(String playerName) {
        return connectedPlayers.contains(playerName);
    }

    public static synchronized void updateAllClients() {
        String players = "WAITING:" + String.join(",", waitingRoom);
        String connected = "PLAYERS:" + String.join(",", connectedPlayers);

        System.out.println(players);
        System.out.println(connected);

        for (NewClient client : clients) {
            client.sendMessage(connected);
            client.sendMessage(players);
        }
    }
}