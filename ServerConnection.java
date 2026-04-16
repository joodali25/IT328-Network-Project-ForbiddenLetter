import javax.swing.*;
import java.io.*;
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

        try {
            String msg;

            while ((msg = in.readLine()) != null) {
                handleMessage(msg);
            }

        } catch (IOException ex) {
            gui.appendStatus("Connection error: " + ex.getMessage());
        }
    }

    private void handleMessage(String message) {

        if (message.startsWith("PLAYERS:")) {
            List<String> players = gui.parseNames(message.substring(8));

            SwingUtilities.invokeLater(() ->
                    gui.updateConnectedPlayers(players)
            );
        }

        else if (message.startsWith("WAITING:")) {
            List<String> players = gui.parseNames(message.substring(8));

            SwingUtilities.invokeLater(() ->
                    gui.updateWaitingPlayers(players)
            );
        }

        else if (message.startsWith("ERROR:")) {
            SwingUtilities.invokeLater(() ->
                    gui.showErrorDialog(message.substring(6))
            );
        }
    }
}
