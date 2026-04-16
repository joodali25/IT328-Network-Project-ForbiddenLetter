
public class Client {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}
