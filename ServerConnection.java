
package com.mycompany.networkprojectphase1;

/**
 *
 * @author Hanan Alghamdi
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class ServerConnection implements Runnable {

    private Socket server;
    private BufferedReader in;
    private ClientGUI gui;

    public ServerConnection(Socket s, ClientGUI gui) throws IOException {
        this.server = s;
        this.gui = gui;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    @Override
    public void run() {
        String serverResponse;

        try {
            while (true) {
                serverResponse = in.readLine();
                if (serverResponse == null) {
                    gui.appendStatus("Disconnected from server.");
                    break;
                }

                handleMessage(serverResponse);
            }

        } catch (IOException ex) {
            gui.appendStatus("Connection error: " + ex.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleMessage(String message) {
        if (message.startsWith("PLAYERS:")) {
            String data = message.substring(8);
            List<String> players = gui.parseNames(data);
            gui.updateConnectedPlayers(players);
            gui.appendStatus("Connected players list updated.");
        }
        else if (message.startsWith("WAITING:")) {
            String data = message.substring(8);
            List<String> players = gui.parseNames(data);
            gui.updateWaitingPlayers(players);
            gui.appendStatus("Waiting room list updated.");
        }
        else if (message.startsWith("ERROR:")) {
            gui.appendStatus("Server error: " + message.substring(6));
        }
        else {
            gui.appendStatus("Server says: " + message);
        }
    }
}
