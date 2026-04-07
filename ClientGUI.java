
package com.mycompany.networkprojectphase1;

/**
 *
 * @author Hanan Alghamdi
 */

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientGUI extends JFrame {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private PrintWriter out;
    private ServerConnection serverConnection;
    private Thread connectionThread;

    private JTextField usernameField;
    private JButton connectButton;
    private JButton playButton;

    private DefaultListModel<String> connectedPlayersModel;
    private DefaultListModel<String> waitingPlayersModel;

    private JList<String> connectedPlayersList;
    private JList<String> waitingPlayersList;

    private JTextArea statusArea;

    private boolean connected = false;

    public ClientGUI() {
        setTitle("Forbidden Letter - Client");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        registerActions();
    }

    private void initComponents() {
        usernameField = new JTextField(15);
        connectButton = new JButton("Connect");
        playButton = new JButton("Play");
        playButton.setEnabled(false);

        connectedPlayersModel = new DefaultListModel<>();
        waitingPlayersModel = new DefaultListModel<>();

        connectedPlayersList = new JList<>(connectedPlayersModel);
        waitingPlayersList = new JList<>(waitingPlayersModel);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(connectButton);
        topPanel.add(playButton);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel connectedPanel = new JPanel(new BorderLayout());
        connectedPanel.setBorder(BorderFactory.createTitledBorder("Connected Players"));
        connectedPanel.add(new JScrollPane(connectedPlayersList), BorderLayout.CENTER);

        JPanel waitingPanel = new JPanel(new BorderLayout());
        waitingPanel.setBorder(BorderFactory.createTitledBorder("Waiting Room"));
        waitingPanel.add(new JScrollPane(waitingPlayersList), BorderLayout.CENTER);

        centerPanel.add(connectedPanel);
        centerPanel.add(waitingPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void registerActions() {
        connectButton.addActionListener(e -> connectToServer());
        playButton.addActionListener(e -> sendPlayRequest());
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            appendStatus("Please enter a username first.");
            return;
        }

        if (connected) {
            appendStatus("You are already connected.");
            return;
        }

        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            serverConnection = new ServerConnection(socket, this);
            connectionThread = new Thread(serverConnection);
            connectionThread.start();

            out.println("CONNECT:" + username);

            connected = true;
            connectButton.setEnabled(false);
            usernameField.setEditable(false);
            playButton.setEnabled(true);

            appendStatus("Connected to server successfully.");

        } catch (IOException ex) {
            appendStatus("Failed to connect to server: " + ex.getMessage());
        }
    }

    private void sendPlayRequest() {
        if (!connected || out == null) {
            appendStatus("You must connect first.");
            return;
        }

        out.println("Play:");
        appendStatus("Play request sent.");
    }

    public void updateConnectedPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            connectedPlayersModel.clear();
            for (String player : players) {
                if (!player.trim().isEmpty()) {
                    connectedPlayersModel.addElement(player.trim());
                }
            }
        });
    }

    public void updateWaitingPlayers(List<String> players) {
        SwingUtilities.invokeLater(() -> {
            waitingPlayersModel.clear();
            for (String player : players) {
                if (!player.trim().isEmpty()) {
                    waitingPlayersModel.addElement(player.trim());
                }
            }
        });
    }

    public void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
        });
    }

    public List<String> parseNames(String data) {
        if (data == null || data.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(data.split(","));
    }
}
