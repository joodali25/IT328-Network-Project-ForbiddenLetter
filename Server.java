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

    public static synchronized void AddToWaitingRoom(String playerName) {
        // منع دخول أي شخص إضافي إذا وصلنا لـ 4
        if (waitingRoom.size() >= 4) {
            updateAllClients(); // لإرسال حالة "ممتلئ" للجميع
            return;
        }

        if (!waitingRoom.contains(playerName)) {
            waitingRoom.add(playerName);
            updateAllClients();

            if (waitingRoom.size() == 4) {
                startGame();
            }
        }
    }
    private static void startGame() {
        for (NewClient client : clients) {
            client.sendMessage("GAME_START");
        }
    }
    
    public static synchronized int getWaitingCount() {
        return waitingRoom.size();
    }

    public static synchronized void AddConnectedPlayer(String playerName) {
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
